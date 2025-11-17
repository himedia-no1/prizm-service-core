# PRIZM API 명세서

**최종 업데이트**: 2025-02-14  
**Base URL**: `http://localhost:8080` (개발)

---

## 공통 규칙

### 인증 모델
- **Access Token**: `JwtService`가 발급하는 JWT. `role`, `id`, `exp` 클레임을 포함하며 만료 시간은 환경 변수 `prizm.auth.jwt.access-token-expiration`(밀리초)로 제어된다. 모든 보호된 API 요청은 `Authorization: Bearer {accessToken}` 헤더로 전송한다.
- **Refresh Token**: HttpOnly 쿠키 `refresh_token`. 만료 시간은 `prizm.auth.jwt.refresh-token-expiration`(밀리초)로 제어되고, `RefreshTokenCacheRepository`(Redis)에 `{token -> (userId, role)}` 형태로 저장된다. OAuth2 로그인 성공 시에는 Refresh Token만 발급되므로, 클라이언트는 `/api/auth/refresh`를 호출해 Access Token을 새로 받아야 한다.
- **쿠키 설정**: 쿠키 Path는 `/api`, `SameSite`/`Secure`/`Domain`은 `prizm.auth.cookie.*` 설정을 따른다. `NEXT_LOCALE` 쿠키는 HttpOnly=false로 내려가며, 사용자 언어(소문자)를 1년 동안 보관한다.

### 헤더 및 전송 규칙
- 본문이 있는 요청은 기본적으로 `Content-Type: application/json; charset=UTF-8`을 사용한다. `@ModelAttribute`를 받는 엔드포인트(`PATCH /api/users/profile`, `PATCH /api/workspaces/{id}`, `PATCH /api/workspaces/{id}/profile`)는 `multipart/form-data`로 전송해야 하며, 필드 이름은 DTO 필드와 동일하다.
- Refresh Token을 전송하려면 브라우저 fetch/axios 요청에 `credentials: 'include'`를 설정해야 한다.
- 모든 응답은 UTF-8 JSON이며, 타임스탬프는 ISO-8601 UTC 문자열(`Instant`)이다.

### 권한과 역할
`@RequireWorkspaceRole` 로직은 컨트롤러 수준에서 워크스페이스 멤버십을 검사한다. 아래 표는 서버에서 사용하는 역할이다.

| 역할 | 설명 |
| --- | --- |
| `OWNER` | 워크스페이스 전체 관리. 삭제/역할 위임 가능 |
| `MANAGER` | 멤버·채널·카테고리·그룹 관리 가능 |
| `MEMBER` | 일반 멤버. 초대 코드 생성 가능 (일부 제약) |
| `GUEST` | 특정 채널만 접근 |

### 주요 Enum 요약
- `Language`: `KO`, `EN`, `JA`, `FR`
- `UserAuthProvider`: `GITHUB`, `GOOGLE`
- `WorkspaceUserState`: `ONLINE`, `AWAY`, `BUSY`, `OFFLINE`
- `WorkspaceUserNotify`: `ON`, `MENTION`, `OFF`
- `ChannelWorkspaceUserNotify`: `ON`, `MENTION`, `OFF`
- `ChannelType`: `CHAT`, `DM`, `WEBHOOK`, `ASSISTANT`
- `ChannelPermission`: `NONE`, `READ`, `WRITE`, `MANAGE`
- `GroupChannelPermission`: `READ`, `WRITE`, `MANAGE`
- `UserNotifyType`: `SYSTEM`, `INVITE`, `MESSAGE`

### 에러 응답
모든 예외는 `GlobalExceptionHandler`를 통해 아래 JSON 형태로 내려간다.

```json
{
  "code": "W001",
  "message": "Workspace not found",
  "timestamp": "2025-02-14T09:30:00Z"
}
```

사용 가능한 에러 코드는 다음과 같다.

| Code | HTTP | Message |
| --- | --- | --- |
| `C001` | 400 | Invalid input value |
| `C002` | 500 | Internal server error |
| `A001` | 401 | Unauthorized |
| `A002` | 403 | Forbidden |
| `A003` | 401 | Invalid token |
| `A004` | 401 | Token expired |
| `U001` | 404 | User not found |
| `U002` | 409 | User already exists |
| `U003` | 403 | User is banned |
| `W001` | 404 | Workspace not found |
| `W002` | 404 | Workspace user not found |
| `W003` | 409 | Workspace user already exists |
| `W004` | 403 | Insufficient permission |
| `W005` | 400 | Owner cannot leave workspace |
| `CT001` | 404 | Category not found |
| `CH001` | 404 | Channel not found |
| `CH002` | 403 | Channel access denied |
| `G001` | 404 | Group not found |
| `G002` | 400 | Cannot assign GUEST users to groups |
| `I001` | 404 | Invite not found |
| `I002` | 400 | Invite expired |
| `I003` | 400 | Invite usage limit reached |
| `I004` | 403 | Invite restricted to specific users |
| `F001` | 404 | File not found |
| `F002` | 500 | File upload failed |

### CORS 및 공개 엔드포인트
- 허용 Origin: `http://localhost:3000`
- 허용 메서드: `GET, POST, PUT, DELETE, PATCH, OPTIONS`
- 공개 경로: `/api/auth/**` 와 `/error`. 그 외 모든 `/api/**` 경로는 Access Token이 필요하다.

---

## OAuth2 로그인 흐름
1. **Authorization 요청**  
   - 엔드포인트: `GET /api/auth/oauth2/{provider}` (`provider`: `google`, `github`)  
   - 선택 쿼리: `lang`, `invite`, `redirect_uri`. 값이 존재하면 `HttpCookieOAuth2AuthorizationRequestRepository`가 HttpOnly 쿠키(`language`, `invite_code`, `redirect_uri`)로 보관한다.
2. **리디렉션 커스터마이징**  
   - `CustomAuthorizationRequestResolver`가 `redirect_uri`를 백엔드 → 프론트엔드 도메인(`prizm.frontend.url`)으로 치환한다.
3. **성공 처리**  
   - `OAuth2SuccessHandler`가 Refresh Token을 생성해 쿠키로 내려주고 Redis에도 저장한다.  
   - `invite_code` 쿠키가 있으면 `/invite/{code}`로 리디렉트하고, 없으면 Redis `user:last_path:{userId}` 값 또는 `/workspace`로 리디렉트한다.  
   - 신규 가입자는 `NEXT_LOCALE` 쿠키 값을 바탕으로 언어를 설정하며, 모든 사용자의 언어 값은 `NEXT_LOCALE`(HttpOnly=false) 에 다시 기록된다.
4. **실패 처리**  
   - `OAuth2FailureHandler`는 `invite_code` 쿠키가 있으면 `/invite/{code}`, 없으면 `/login`으로 리디렉트한다.

---

## Auth API

### POST /api/auth/refresh
- **설명**: Refresh Token 검증 후 새로운 Access Token을 발급한다. Rotation 없이 Access Token만 교체한다.
- **권한**: Refresh Token 쿠키 필수.
- **응답 200**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### POST /api/auth/logout
- **설명**: Refresh Token을 Redis와 쿠키 양쪽에서 제거한다.
- **권한**: Access Token + Refresh Token.
- **응답**: `204 No Content`

### DELETE /api/auth/withdraw
- **설명**: 현재 사용자 계정을 `deletedAt`으로 소프트 삭제하고 Refresh Token을 모두 폐기한다.
- **권한**: Access Token + Refresh Token.
- **응답**: `204 No Content`

---

## 사용자 API

### DTO 요약
- `UserProfileResponse`: `profileImage`, `name`, `email`, `authProvider`, `language`, `createdAt`
- `UserProfileUpdateRequest`(multipart): `profileImage`(file), `name`
- `UserLanguageUpdateRequest`: `language`
- `UserLastPathRequest`: `path`
- `UserLastPathResponse`: `path`

### GET /api/users/profile
- **설명**: 현재 사용자 프로필 조회
- **응답 200**: `UserProfileResponse`

### PATCH /api/users/profile
- **설명**: 닉네임·프로필 이미지를 수정한다. 이미지가 존재하면 기존 이미지를 삭제한 뒤 MinIO에 재업로드한다.
- **요청**: `multipart/form-data` (`profileImage`, `name`)
- **응답 200**: `UserProfileResponse`

### PATCH /api/users/language
- **설명**: 선호 언어 업데이트
- **요청 본문**
```json
{
  "language": "EN"
}
```
- **응답 200**: `UserProfileResponse`

### POST /api/users/last-path
- **설명**: 사용자의 마지막 방문 경로를 Redis(`user:last_path:{userId}`)에 저장한다.
- **요청 본문**
```json
{
  "path": "/workspace/1/channel/5"
}
```
- **응답**: `204 No Content`

### GET /api/users/last-path
- **설명**: Redis에 저장된 마지막 경로 조회
- **응답 200**
```json
{
  "path": "/workspace/1/channel/5"
}
```

---

## 사용자 알림 API

### GET /api/notifications
- **설명**: 로그인 사용자의 알림 목록을 최신순으로 리턴한다.
- **응답 200**
```json
{
  "notifications": [
    {
      "id": 10,
      "type": "INVITE",
      "senderId": 3,
      "content": "워크스페이스 초대",
      "locationId": 42,
      "important": false,
      "read": false,
      "createdAt": "2025-02-14T09:30:00Z"
    }
  ]
}
```

---

## 워크스페이스 API

### DTO 요약
- `WorkspaceCreateRequest`: `name`(필수), `imageId` *(현재 서버에서는 사용하지 않음)*  
- `WorkspaceUpdateRequest`(multipart): `image`, `name`
- `WorkspaceResponse`: `id`, `name`, `imageUrl`, `createdAt`
- `WorkspaceListItemResponse`: `id`, `name`, `image`

### GET /api/workspaces
- **설명**: 사용자가 속한 활성 워크스페이스 목록
- **응답 200**: `WorkspaceListItemResponse[]`

### POST /api/workspaces
- **설명**: 새 워크스페이스 생성, 호출 사용자는 `OWNER`로 등록
- **요청 본문**
```json
{
  "name": "PRIZM Core",
  "imageId": null
}
```
- **응답 200**: `WorkspaceResponse`

### GET /api/workspaces/{workspaceId}
- **설명**: 워크스페이스 단건 조회
- **응답 200**: `WorkspaceResponse`

### PATCH /api/workspaces/{workspaceId}
- **권한**: `OWNER` 또는 `MANAGER`
- **설명**: 이름 또는 이미지를 수정 (`multipart/form-data`)
- **응답 200**: `WorkspaceResponse`

### DELETE /api/workspaces/{workspaceId}
- **권한**: `OWNER`
- **설명**: 워크스페이스를 소프트 삭제
- **응답**: `204 No Content`

---

## 워크스페이스 초대 API

### DTO 요약
- `WorkspaceInviteCreateRequest`: `expiresInSeconds`, `maxUses`, `allowedUserIds`, `autoJoinGroupIds`, `channelId` (`channelId`가 존재하면 게스트 초대)
- `WorkspaceInviteCreateResponse`: `code`, `expiresAt`, `maxUses`, `channelId`
- `WorkspaceInviteInfoResponse`: `code`, `createdAt`, `expiresAt`, `usedCount`, `maxCount`, `location`
- `WorkspaceInviteJoinResponse`: `workspaceId`, `userId`(워크스페이스 사용자 ID), `role`

### POST /api/workspaces/{workspaceId}/invites
- **권한**: `OWNER`, `MANAGER`, `MEMBER`
- **설명**: 초대 코드 생성  
  - `channelId`가 `null`: 멤버 초대 (`WorkspaceUserRole.MEMBER`). `autoJoinGroupIds`로 가입 즉시 편성할 그룹을 지정할 수 있다.  
  - `channelId`가 존재: 게스트 초대 (`WorkspaceUserRole.GUEST`). `allowedUserIds` 필수이며, `MEMBER`는 해당 채널에 `MANAGE` 권한이 있어야 생성 가능하다.
- **응답 200**: `WorkspaceInviteCreateResponse`

### GET /api/workspaces/{workspaceId}/invites
- **권한**: `OWNER`, `MANAGER`
- **설명**: 만료되지 않았고 사용 한도를 넘지 않은 초대 리스트 조회
- **응답 200**: `WorkspaceInviteInfoResponse[]`

### DELETE /api/workspaces/{workspaceId}/invites/{code}
- **권한**: `OWNER`, `MANAGER`
- **응답**: `204 No Content`

### POST /api/invites/{code}/join
- **설명**: 초대 코드로 워크스페이스 가입.  
  - 이미 가입했거나 밴 상태면 오류 발생  
  - 멤버 초대의 경우 `autoJoinGroupIds` 그룹에 자동 편성  
  - 게스트 초대의 경우 대상 채널에 바로 추가
- **응답 200**
```json
{
  "workspaceId": 1,
  "userId": 55,
  "role": "MEMBER"
}
```

### GET /api/invites/{code}
- **설명**: 유효한 초대 코드가 가리키는 워크스페이스 정보 조회
- **응답 200**: `WorkspaceResponse`

---

## 워크스페이스 사용자 API

### DTO 요약
- `WorkspaceUserListResponse`: `users[] { workspaceUserId, state, image, name, email }`
- `WorkspaceUserSimpleProfileResponse`: `notifyType`, `state`, `image`, `name`
- `WorkspaceUserFullProfileResponse`: 역할/상태/이미지/이름/이메일/전화/소개/생성일/`groups[] { id, name }`
- `WorkspaceUserProfileUpdateRequest`(multipart): `image`, `name`, `phone`, `introduction`
- `WorkspaceUserNotifyUpdateRequest`: `notifyType`
- `WorkspaceUserStateUpdateRequest`: `state`
- `WorkspaceUserRoleUpdateRequest`: `role`

### GET /api/workspaces/{workspaceId}/users
- **권한**: `OWNER`, `MANAGER`, `MEMBER`
- **설명**: `role` 쿼리 파라미터로 필터 가능
- **응답 200**: `WorkspaceUserListResponse`

### GET /api/workspaces/{workspaceId}/profile
- **권한**: `OWNER`, `MANAGER`, `MEMBER`, `GUEST`
- **설명**: 현재 로그인 사용자의 워크스페이스 프로필
- **응답 200**: `WorkspaceUserSimpleProfileResponse`

### GET /api/workspaces/{workspaceId}/users/{targetUserId}/profile
- **권한**: `OWNER`, `MANAGER`, `MEMBER`, `GUEST`
- **응답 200**: `WorkspaceUserFullProfileResponse`

### PATCH /api/workspaces/{workspaceId}/profile
- **권한**: `OWNER`, `MANAGER`, `MEMBER`, `GUEST`
- **설명**: 워크스페이스 내 프로필 이미지·이름·연락처·소개 수정 (`multipart/form-data`)
- **응답**: `204 No Content`

### PATCH /api/workspaces/{workspaceId}/notify
- **권한**: `OWNER`, `MANAGER`, `MEMBER`, `GUEST`
- **요청 본문**
```json
{
  "notifyType": "MENTION"
}
```
- **응답**: `204 No Content`

### PATCH /api/workspaces/{workspaceId}/state
- **권한**: `OWNER`, `MANAGER`, `MEMBER`, `GUEST`
- **요청 본문**
```json
{
  "state": "AWAY"
}
```
- **응답**: `204 No Content`

### PATCH /api/workspaces/{workspaceId}/users/{targetUserId}/role
- **권한**: `OWNER`, `MANAGER`  
  - `OWNER`로 승격 시 기존 OWNER는 자동으로 `MANAGER`로 변경된다.
- **요청 본문**
```json
{
  "role": "MANAGER"
}
```
- **응답**: `204 No Content`

### DELETE /api/workspaces/{workspaceId}/users/{targetUserId}
- **권한**: `OWNER`, `MANAGER`
- **설명**: 멤버 강제 퇴장 (soft delete)
- **응답**: `204 No Content`

### POST /api/workspaces/{workspaceId}/users/{targetUserId}/ban
- **권한**: `OWNER`, `MANAGER`
- **설명**: 사용자를 밴하고 `deletedAt`을 설정
- **응답**: `204 No Content`

### DELETE /api/workspaces/{workspaceId}/users/{targetUserId}/ban
- **권한**: `OWNER`, `MANAGER`
- **설명**: 밴 해제 (`banned=false`)
- **응답**: `204 No Content`

### DELETE /api/workspaces/{workspaceId}/leave
- **권한**: `MANAGER`, `MEMBER`, `GUEST`
- **설명**: 사용자가 자발적으로 워크스페이스를 떠난다. `OWNER`는 호출할 수 없다.
- **응답**: `204 No Content`

---

## 카테고리 API

### DTO 요약
- `CategoryCreateRequest`: `name`
- `CategoryUpdateRequest`: `name`
- `CategoryZIndexUpdateRequest`: `position`(`FIRST`, `LAST`, `BETWEEN`), `beforeId`, `afterId`
- `CategoryResponse`: `id`, `workspaceId`, `name`, `zIndex`, `createdAt`

### POST /api/workspaces/{workspaceId}/categories
- **권한**: `OWNER`, `MANAGER`
- **응답 200**: `CategoryResponse`

### PATCH /api/workspaces/{workspaceId}/categories/{categoryId}
- **권한**: `OWNER`, `MANAGER`
- **응답 200**: `CategoryResponse`

### PATCH /api/workspaces/{workspaceId}/categories/{categoryId}/z-index
- **권한**: `OWNER`, `MANAGER`
- **설명**: 위치 조정. `position=BETWEEN`일 경우 `beforeId`/`afterId` 필수.
- **응답**: `204 No Content`

### DELETE /api/workspaces/{workspaceId}/categories/{categoryId}
- **권한**: `OWNER`, `MANAGER`
- **응답**: `204 No Content`

---

## 채널 API

### DTO 요약
- `ChannelCreateRequest`: `name`, `description`, `type`
- `ChannelUpdateRequest`: `name`, `description`
- `ChannelZIndexUpdateRequest`: `position`, `beforeId`, `afterId`
- `ChannelNotifyUpdateRequest`: `notifyType`
- `ChannelResponse`: `id`, `workspaceId`, `categoryId`, `type`, `name`, `description`, `zIndex`, `createdAt`
- `ChannelInfoResponse`: `id`, `name`, `description`, `myNotify`
- `AccessibleChannelListResponse`: `categories[] { id, name, channels[] { id, name, permission } }`
- `ChannelUserListResponse`: `regularUsers[]`, `guestUsers[]` (`UserItem`는 `id`, `state`, `image`, `name`)

### POST /api/workspaces/{workspaceId}/categories/{categoryId}/channels
- **권한**: `OWNER`, `MANAGER`
- **응답 200**: `ChannelResponse`

### GET /api/workspaces/{workspaceId}/channels/{channelId}
- **권한**: `OWNER`, `MANAGER`, `MEMBER`, `GUEST`
- **설명**: 채널 기본 정보와 내 알림 설정
- **응답 200**: `ChannelInfoResponse`

### PATCH /api/workspaces/{workspaceId}/channels/{channelId}
- **권한**: `OWNER`, `MANAGER`
- **응답 200**: `ChannelResponse`

### PATCH /api/workspaces/{workspaceId}/channels/{channelId}/z-index
- **권한**: `OWNER`, `MANAGER`
- **설명**: 채널 정렬 순서 조정
- **응답**: `204 No Content`

### PATCH /api/workspaces/{workspaceId}/channels/{channelId}/notify
- **권한**: `OWNER`, `MANAGER`, `MEMBER`, `GUEST`
- **설명**: 개인별 채널 알림 설정 변경 (`ChannelWorkspaceUserNotify`)
- **응답**: `204 No Content`

### DELETE /api/workspaces/{workspaceId}/channels/{channelId}
- **권한**: `OWNER`, `MANAGER`
- **응답**: `204 No Content`

### GET /api/workspaces/{workspaceId}/channels/accessible
- **권한**: `OWNER`, `MANAGER`, `MEMBER`, `GUEST`
- **설명**: 사용자가 접근 가능한 채널 목록을 카테고리별로 리턴. `permission` 값은 `ChannelPermission` 문자열이다.
- **응답 200**: `AccessibleChannelListResponse`

### GET /api/workspaces/{workspaceId}/channels/{channelId}/users
- **권한**: `OWNER`, `MANAGER`, `MEMBER`, `GUEST`
- **설명**: 채널 멤버(정규/게스트) 목록. 이름 기준으로 정렬되어 있다.
- **응답 200**: `ChannelUserListResponse`

---

## 그룹 API

### DTO 요약
- `GroupCreateRequest`: `name`
- `GroupResponse`: `id`, `workspaceId`, `name`, `createdAt`
- `GroupListResponse`: `groups[] { id, name }`
- `GroupDetailResponse`: `id`, `name`, `users[] { id, name }`, `categories[] { id, name, channels[] { id, name, permission } }`
- `GroupUpdateRequest`: `name`, `userIds`(워크스페이스 사용자 ID 목록), `channels[] { channelId, permission }`

### POST /api/workspaces/{workspaceId}/groups
- **권한**: `OWNER`, `MANAGER`
- **응답 200**: `GroupResponse`

### GET /api/workspaces/{workspaceId}/groups
- **권한**: `OWNER`, `MANAGER`
- **응답 200**: `GroupListResponse`

### GET /api/workspaces/{workspaceId}/groups/{groupId}
- **권한**: `OWNER`, `MANAGER`
- **응답 200**: `GroupDetailResponse`

### PATCH /api/workspaces/{workspaceId}/groups/{groupId}
- **권한**: `OWNER`, `MANAGER`
- **설명**: 전달된 `userIds`와 `channels` 전체로 그룹 구성이 대체된다. `GUEST`는 추가할 수 없다.
- **응답 200**: `GroupResponse`

### DELETE /api/workspaces/{workspaceId}/groups/{groupId}
- **권한**: `OWNER`, `MANAGER`
- **응답**: `204 No Content`

---

## 추가 참고 사항
- 모든 이미지 URL은 `ImageUploadHelper`/`MinioService`가 생성한 공개 URL이며, null이면 이미지가 없는 상태이다.
- 채널 접근 목록은 `channelAccess` 캐시(SPRING Cache)를 활용하며, `ChannelService`에서 채널/카테고리 정보를 갱신할 때 `ChannelAccessService.invalidateWorkspaceCache`로 무효화한다.
- `WorkspaceInviteCreateRequest`의 legacy 필드 `allowedUserId`는 서버에서 사용하지 않는다. 동일한 기능은 `allowedUserIds` 배열로만 처리된다.
- `@CurrentUser Long` 파라미터는 JWT `id` 클레임(사용자 ID)을 그대로 주입한다. 워크스페이스 엔드포인트는 해당 값을 바탕으로 `workspaceId` + `userId` 조합으로 멤버십을 조회한다.