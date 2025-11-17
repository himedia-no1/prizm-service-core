# PRIZM API 명세서

**최종 업데이트**: 2025-11-17
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
| `A005` | 401 | Refresh token not found |
| `A006` | 401 | Invalid or expired refresh token |
| `A007` | 401 | Refresh token not found in storage |
| `A008` | 401 | Invalid authentication |
| `U001` | 404 | User not found |
| `U002` | 409 | User already exists |
| `U003` | 403 | User is banned |
| `U004` | 403 | User is deleted |
| `W001` | 404 | Workspace not found |
| `W002` | 404 | Workspace user not found |
| `W003` | 409 | Workspace user already exists |
| `W004` | 403 | Insufficient permission |
| `W005` | 400 | Owner cannot leave workspace |
| `W006` | 403 | Only OWNER can delegate OWNER role |
| `W007` | 400 | Channel not in workspace |
| `W008` | 403 | User is banned from this workspace |
| `W009` | 409 | User already joined workspace |
| `W010` | 403 | User not allowed to create invite |
| `W011` | 404 | Workspace is deleted |
| `CT001` | 404 | Category not found |
| `CH001` | 404 | Channel not found |
| `CH002` | 403 | Channel access denied |
| `G001` | 404 | Group not found |
| `G002` | 400 | Cannot assign GUEST users to groups |
| `I001` | 404 | Invite not found |
| `I002` | 400 | Invite expired |
| `I003` | 400 | Invite usage limit reached |
| `I004` | 403 | Invite restricted to specific users |
| `I005` | 400 | Guest invite requires allowed user IDs |
| `I006` | 403 | Only OWNER, MANAGER, or MEMBER with MANAGE permission can create guest invite |
| `I007` | 403 | MEMBER requires MANAGE permission on this channel to create guest invite |
| `I008` | 400 | Invite not for this workspace |
| `I009` | 403 | User not allowed to use this invite |
| `I010` | 404 | Allowed user not found |
| `F001` | 404 | File not found |
| `F002` | 500 | File upload failed |
| `F003` | 500 | File download failed |
| `F004` | 500 | File delete failed |
| `T001` | 400 | Invalid language code |
| `T002` | 500 | Translation failed |
| `R001` | 500 | Cache operation failed |
| `P001` | 400 | Invalid position |

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
**설명**: Refresh Token 검증 후 새로운 Access Token을 발급한다. Rotation 없이 Access Token만 교체한다.

**요청**
- Headers: Cookie `refresh_token` 필수
- Credentials: `include` 설정 필요

**응답 200**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**가능한 에러**
- `A005`: Refresh token not found
- `A006`: Invalid or expired refresh token
- `A007`: Refresh token not found in storage

---

### POST /api/auth/logout
**설명**: Refresh Token을 Redis와 쿠키 양쪽에서 제거한다.

**요청**
- Headers:
  - `Authorization: Bearer {accessToken}`
  - Cookie `refresh_token`
- Credentials: `include` 설정 필요

**응답**: `204 No Content`

**가능한 에러**
- `A001`: Unauthorized
- `A003`: Invalid token
- `A004`: Token expired

---

### DELETE /api/auth/withdraw
**설명**: 현재 사용자 계정을 `deletedAt`으로 소프트 삭제하고 Refresh Token을 모두 폐기한다.

**요청**
- Headers:
  - `Authorization: Bearer {accessToken}`
  - Cookie `refresh_token`
- Credentials: `include` 설정 필요

**응답**: `204 No Content`

**가능한 에러**
- `A001`: Unauthorized
- `A003`: Invalid token
- `A004`: Token expired
- `U001`: User not found

---

## 사용자 API

### GET /api/users/profile
**설명**: 현재 사용자 프로필 조회

**요청**
- Headers: `Authorization: Bearer {accessToken}`

**응답 200** (`UserProfileResponse`)
```json
{
  "profileImage": "https://minio.example.com/users/profile-123.jpg",
  "name": "홍길동",
  "email": "user@example.com",
  "authProvider": "GOOGLE",
  "language": "KO",
  "createdAt": "2025-01-15T08:30:00Z"
}
```

**필드 설명**
- `profileImage` (string, nullable): 프로필 이미지 URL
- `name` (string): 사용자 이름
- `email` (string): 이메일 주소
- `authProvider` (enum): `GITHUB`, `GOOGLE`
- `language` (enum): `KO`, `EN`, `JA`, `FR`
- `createdAt` (string): ISO-8601 UTC 형식

**가능한 에러**
- `A001`: Unauthorized
- `A003`: Invalid token
- `A004`: Token expired
- `U001`: User not found

---

### PATCH /api/users/profile
**설명**: 닉네임·프로필 이미지를 수정한다. 이미지가 존재하면 기존 이미지를 삭제한 뒤 MinIO에 재업로드한다.

**요청**
- Headers:
  - `Authorization: Bearer {accessToken}`
  - `Content-Type: multipart/form-data`
- Body (multipart):
  - `profileImage` (file, 선택): 프로필 이미지 파일
  - `name` (string, 선택): 사용자 이름

**응답 200**: `UserProfileResponse` (위와 동일)

**가능한 에러**
- `A001`: Unauthorized
- `U001`: User not found
- `F002`: File upload failed
- `F004`: File delete failed

---

### PATCH /api/users/language
**설명**: 선호 언어 업데이트. `NEXT_LOCALE` 쿠키도 함께 업데이트된다.

**요청**
- Headers:
  - `Authorization: Bearer {accessToken}`
  - `Content-Type: application/json`
- Body:
```json
{
  "language": "EN"
}
```

**필드 설명**
- `language` (enum, 필수): `KO`, `EN`, `JA`, `FR`

**Validation**
- `@NotNull`: language는 필수

**응답 200**: `UserProfileResponse`

**가능한 에러**
- `A001`: Unauthorized
- `C001`: Invalid input value (잘못된 language 값)
- `U001`: User not found
- `T001`: Invalid language code

---

### POST /api/users/last-path
**설명**: 사용자의 마지막 방문 경로를 Redis(`user:last_path:{userId}`)에 저장한다.

**요청**
- Headers:
  - `Authorization: Bearer {accessToken}`
  - `Content-Type: application/json`
- Body:
```json
{
  "path": "/workspace/1/channel/5"
}
```

**필드 설명**
- `path` (string, 필수): 마지막 방문 경로

**Validation**
- `@NotBlank`: path는 필수이며 공백 불가

**응답**: `204 No Content`

**가능한 에러**
- `A001`: Unauthorized
- `C001`: Invalid input value
- `R001`: Cache operation failed

---

### GET /api/users/last-path
**설명**: Redis에 저장된 마지막 경로 조회

**요청**
- Headers: `Authorization: Bearer {accessToken}`

**응답 200** (`UserLastPathResponse`)
```json
{
  "path": "/workspace/1/channel/5"
}
```

**필드 설명**
- `path` (string, nullable): 저장된 마지막 경로. 없으면 null

**가능한 에러**
- `A001`: Unauthorized
- `R001`: Cache operation failed

---

## 사용자 알림 API

### GET /api/notifications
**설명**: 로그인 사용자의 알림 목록을 최신순으로 리턴한다.

**요청**
- Headers: `Authorization: Bearer {accessToken}`

**응답 200** (`UserNotifyListResponse`)
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

**필드 설명**
- `notifications` (array): 알림 목록
  - `id` (number): 알림 ID
  - `type` (enum): `SYSTEM`, `INVITE`, `MESSAGE`
  - `senderId` (number, nullable): 발신자 ID
  - `content` (string): 알림 내용
  - `locationId` (number, nullable): 관련 위치 ID
  - `important` (boolean): 중요 알림 여부
  - `read` (boolean): 읽음 여부
  - `createdAt` (string): ISO-8601 UTC 형식

**가능한 에러**
- `A001`: Unauthorized
- `A003`: Invalid token
- `A004`: Token expired

---

## 워크스페이스 API

### GET /api/workspaces
**설명**: 사용자가 속한 활성 워크스페이스 목록

**요청**
- Headers: `Authorization: Bearer {accessToken}`

**응답 200** (`WorkspaceListItemResponse[]`)
```json
[
  {
    "id": 1,
    "name": "PRIZM Core",
    "image": "https://minio.example.com/workspaces/ws-123.jpg"
  }
]
```

**필드 설명**
- `id` (number): 워크스페이스 ID
- `name` (string): 워크스페이스 이름
- `image` (string, nullable): 워크스페이스 이미지 URL

**가능한 에러**
- `A001`: Unauthorized
- `A003`: Invalid token
- `A004`: Token expired

---

### POST /api/workspaces
**설명**: 새 워크스페이스 생성, 호출 사용자는 `OWNER`로 등록

**요청**
- Headers:
  - `Authorization: Bearer {accessToken}`
  - `Content-Type: application/json`
- Body:
```json
{
  "name": "PRIZM Core",
  "imageId": null
}
```

**필드 설명**
- `name` (string, 필수): 워크스페이스 이름
- `imageId` (number, 선택): 현재 사용되지 않음

**Validation**
- `@NotBlank`: name은 필수이며 공백 불가

**응답 200** (`WorkspaceResponse`)
```json
{
  "id": 1,
  "name": "PRIZM Core",
  "imageUrl": null,
  "createdAt": "2025-02-14T09:30:00Z"
}
```

**필드 설명**
- `id` (number): 워크스페이스 ID
- `name` (string): 워크스페이스 이름
- `imageUrl` (string, nullable): 워크스페이스 이미지 URL
- `createdAt` (string): ISO-8601 UTC 형식

**가능한 에러**
- `A001`: Unauthorized
- `C001`: Invalid input value

---

### GET /api/workspaces/{workspaceId}
**설명**: 워크스페이스 단건 조회

**요청**
- Headers: `Authorization: Bearer {accessToken}`
- Path Parameters:
  - `workspaceId` (number): 워크스페이스 ID

**응답 200**: `WorkspaceResponse`

**가능한 에러**
- `A001`: Unauthorized
- `W001`: Workspace not found
- `W002`: Workspace user not found (멤버십 없음)
- `W011`: Workspace is deleted

---

### PATCH /api/workspaces/{workspaceId}
**설명**: 이름 또는 이미지를 수정

**권한**: `OWNER` 또는 `MANAGER`

**요청**
- Headers:
  - `Authorization: Bearer {accessToken}`
  - `Content-Type: multipart/form-data`
- Path Parameters:
  - `workspaceId` (number): 워크스페이스 ID
- Body (multipart):
  - `image` (file, 선택): 워크스페이스 이미지 파일
  - `name` (string, 선택): 워크스페이스 이름

**응답 200**: `WorkspaceResponse`

**가능한 에러**
- `A001`: Unauthorized
- `W001`: Workspace not found
- `W002`: Workspace user not found
- `W004`: Insufficient permission
- `F002`: File upload failed
- `F004`: File delete failed

---

### DELETE /api/workspaces/{workspaceId}
**설명**: 워크스페이스를 소프트 삭제

**권한**: `OWNER`

**요청**
- Headers: `Authorization: Bearer {accessToken}`
- Path Parameters:
  - `workspaceId` (number): 워크스페이스 ID

**응답**: `204 No Content`

**가능한 에러**
- `A001`: Unauthorized
- `W001`: Workspace not found
- `W002`: Workspace user not found
- `W004`: Insufficient permission

---

## 워크스페이스 초대 API

### POST /api/workspaces/{workspaceId}/invites
**설명**: 초대 코드 생성
- `channelId`가 `null`: 멤버 초대 (`WorkspaceUserRole.MEMBER`). `autoJoinGroupIds`로 가입 즉시 편성할 그룹을 지정할 수 있다.
- `channelId`가 존재: 게스트 초대 (`WorkspaceUserRole.GUEST`). `allowedUserIds` 필수이며, `MEMBER`는 해당 채널에 `MANAGE` 권한이 있어야 생성 가능하다.

**권한**: `OWNER`, `MANAGER`, `MEMBER`

**요청**
- Headers:
  - `Authorization: Bearer {accessToken}`
  - `Content-Type: application/json`
- Path Parameters:
  - `workspaceId` (number): 워크스페이스 ID
- Body:
```json
{
  "expiresInSeconds": 86400,
  "maxUses": 10,
  "allowedUserIds": [5, 10, 15],
  "autoJoinGroupIds": [1, 2],
  "channelId": null
}
```

**필드 설명**
- `expiresInSeconds` (number, 선택): 초대 만료 시간 (초 단위). 최소 1
- `maxUses` (number, 선택): 최대 사용 횟수. 최소 1
- `allowedUserIds` (array, 선택): 특정 사용자만 초대 허용 (User ID 목록). 게스트 초대 시 필수
- `autoJoinGroupIds` (array, 선택): 가입 즉시 편성할 그룹 ID 목록
- `channelId` (number, 선택): 게스트 초대 대상 채널 ID. null이면 멤버 초대

**Validation**
- `@Min(1)`: expiresInSeconds, maxUses는 최소 1

**응답 200** (`WorkspaceInviteCreateResponse`)
```json
{
  "code": "abc123xyz",
  "expiresAt": "2025-02-15T09:30:00Z",
  "maxUses": 10,
  "channelId": null
}
```

**필드 설명**
- `code` (string): 초대 코드
- `expiresAt` (string, nullable): 만료 시각 (ISO-8601 UTC)
- `maxUses` (number, nullable): 최대 사용 횟수
- `channelId` (number, nullable): 게스트 초대 채널 ID

**가능한 에러**
- `A001`: Unauthorized
- `W001`: Workspace not found
- `W002`: Workspace user not found
- `W004`: Insufficient permission
- `W007`: Channel not in workspace
- `W010`: User not allowed to create invite
- `CH001`: Channel not found
- `I005`: Guest invite requires allowed user IDs
- `I006`: Guest invite permission denied
- `I007`: MEMBER requires MANAGE permission on this channel to create guest invite
- `I010`: Allowed user not found

---

### GET /api/workspaces/{workspaceId}/invites
**설명**: 만료되지 않았고 사용 한도를 넘지 않은 초대 리스트 조회

**권한**: `OWNER`, `MANAGER`

**요청**
- Headers: `Authorization: Bearer {accessToken}`
- Path Parameters:
  - `workspaceId` (number): 워크스페이스 ID

**응답 200** (`WorkspaceInviteInfoResponse[]`)
```json
[
  {
    "code": "abc123xyz",
    "createdAt": "2025-02-14T09:30:00Z",
    "expiresAt": "2025-02-15T09:30:00Z",
    "usedCount": 3,
    "maxCount": 10,
    "location": "전체 워크스페이스"
  }
]
```

**필드 설명**
- `code` (string): 초대 코드
- `createdAt` (string): 생성 시각 (ISO-8601 UTC)
- `expiresAt` (string, nullable): 만료 시각 (ISO-8601 UTC)
- `usedCount` (number): 사용 횟수
- `maxCount` (number, nullable): 최대 사용 횟수
- `location` (string): 초대 위치 정보

**가능한 에러**
- `A001`: Unauthorized
- `W001`: Workspace not found
- `W002`: Workspace user not found
- `W004`: Insufficient permission

---

### DELETE /api/workspaces/{workspaceId}/invites/{code}
**설명**: 초대 코드 삭제

**권한**: `OWNER`, `MANAGER`

**요청**
- Headers: `Authorization: Bearer {accessToken}`
- Path Parameters:
  - `workspaceId` (number): 워크스페이스 ID
  - `code` (string): 초대 코드

**응답**: `204 No Content`

**가능한 에러**
- `A001`: Unauthorized
- `W001`: Workspace not found
- `W002`: Workspace user not found
- `W004`: Insufficient permission
- `I001`: Invite not found
- `I008`: Invite not for this workspace

---

### POST /api/invites/{code}/join
**설명**: 초대 코드로 워크스페이스 가입
- 이미 가입했거나 밴 상태면 오류 발생
- 멤버 초대의 경우 `autoJoinGroupIds` 그룹에 자동 편성
- 게스트 초대의 경우 대상 채널에 바로 추가

**요청**
- Headers: `Authorization: Bearer {accessToken}`
- Path Parameters:
  - `code` (string): 초대 코드

**응답 200** (`WorkspaceInviteJoinResponse`)
```json
{
  "workspaceId": 1,
  "userId": 55,
  "role": "MEMBER"
}
```

**필드 설명**
- `workspaceId` (number): 워크스페이스 ID
- `userId` (number): 워크스페이스 사용자 ID (워크스페이스 멤버십 ID)
- `role` (enum): `OWNER`, `MANAGER`, `MEMBER`, `GUEST`

**가능한 에러**
- `A001`: Unauthorized
- `W001`: Workspace not found
- `W008`: User is banned from this workspace
- `W009`: User already joined workspace
- `I001`: Invite not found
- `I002`: Invite expired
- `I003`: Invite usage limit reached
- `I004`: Invite restricted to specific users
- `I009`: User not allowed to use this invite

---

### GET /api/invites/{code}
**설명**: 유효한 초대 코드가 가리키는 워크스페이스 정보 조회

**요청**
- Headers: `Authorization: Bearer {accessToken}`
- Path Parameters:
  - `code` (string): 초대 코드

**응답 200**: `WorkspaceResponse`
```json
{
  "id": 1,
  "name": "PRIZM Core",
  "imageUrl": "https://minio.example.com/workspaces/ws-123.jpg",
  "createdAt": "2025-02-14T09:30:00Z"
}
```

**가능한 에러**
- `A001`: Unauthorized
- `I001`: Invite not found
- `W001`: Workspace not found

---

## 워크스페이스 사용자 API

### GET /api/workspaces/{workspaceId}/users
**설명**: 워크스페이스 사용자 목록 조회. `role` 쿼리 파라미터로 필터 가능

**권한**: `OWNER`, `MANAGER`, `MEMBER`

**요청**
- Headers: `Authorization: Bearer {accessToken}`
- Path Parameters:
  - `workspaceId` (number): 워크스페이스 ID
- Query Parameters:
  - `role` (string, 선택): 역할 필터 (`OWNER`, `MANAGER`, `MEMBER`, `GUEST`)

**응답 200** (`WorkspaceUserListResponse`)
```json
{
  "users": [
    {
      "workspaceUserId": 10,
      "state": "ONLINE",
      "image": "https://minio.example.com/users/profile-123.jpg",
      "name": "홍길동",
      "email": "user@example.com"
    }
  ]
}
```

**필드 설명**
- `users` (array): 사용자 목록
  - `workspaceUserId` (number): 워크스페이스 사용자 ID (멤버십 ID)
  - `state` (enum): `ONLINE`, `AWAY`, `BUSY`, `OFFLINE`
  - `image` (string, nullable): 프로필 이미지 URL
  - `name` (string): 사용자 이름
  - `email` (string): 이메일

**가능한 에러**
- `A001`: Unauthorized
- `W001`: Workspace not found
- `W002`: Workspace user not found
- `W004`: Insufficient permission

---

### GET /api/workspaces/{workspaceId}/profile
**설명**: 현재 로그인 사용자의 워크스페이스 프로필

**권한**: `OWNER`, `MANAGER`, `MEMBER`, `GUEST`

**요청**
- Headers: `Authorization: Bearer {accessToken}`
- Path Parameters:
  - `workspaceId` (number): 워크스페이스 ID

**응답 200** (`WorkspaceUserSimpleProfileResponse`)
```json
{
  "notifyType": "ON",
  "state": "ONLINE",
  "image": "https://minio.example.com/workspaces/profile-123.jpg",
  "name": "홍길동"
}
```

**필드 설명**
- `notifyType` (enum): `ON`, `MENTION`, `OFF`
- `state` (enum): `ONLINE`, `AWAY`, `BUSY`, `OFFLINE`
- `image` (string, nullable): 워크스페이스 프로필 이미지 URL
- `name` (string): 워크스페이스 내 이름

**가능한 에러**
- `A001`: Unauthorized
- `W001`: Workspace not found
- `W002`: Workspace user not found

---

### GET /api/workspaces/{workspaceId}/users/{targetUserId}/profile
**설명**: 특정 사용자의 워크스페이스 프로필 조회

**권한**: `OWNER`, `MANAGER`, `MEMBER`, `GUEST`

**요청**
- Headers: `Authorization: Bearer {accessToken}`
- Path Parameters:
  - `workspaceId` (number): 워크스페이스 ID
  - `targetUserId` (number): 대상 워크스페이스 사용자 ID (멤버십 ID)

**응답 200** (`WorkspaceUserFullProfileResponse`)
```json
{
  "role": "MEMBER",
  "state": "ONLINE",
  "image": "https://minio.example.com/workspaces/profile-123.jpg",
  "name": "홍길동",
  "email": "user@example.com",
  "phone": "010-1234-5678",
  "introduction": "안녕하세요",
  "createdAt": "2025-02-14T09:30:00Z",
  "groups": [
    {
      "id": 1,
      "name": "개발팀"
    }
  ]
}
```

**필드 설명**
- `role` (enum): `OWNER`, `MANAGER`, `MEMBER`, `GUEST`
- `state` (enum): `ONLINE`, `AWAY`, `BUSY`, `OFFLINE`
- `image` (string, nullable): 워크스페이스 프로필 이미지 URL
- `name` (string): 워크스페이스 내 이름
- `email` (string): 이메일
- `phone` (string, nullable): 전화번호
- `introduction` (string, nullable): 소개
- `createdAt` (string): ISO-8601 UTC 형식
- `groups` (array): 소속 그룹 목록
  - `id` (number): 그룹 ID
  - `name` (string): 그룹 이름

**가능한 에러**
- `A001`: Unauthorized
- `W001`: Workspace not found
- `W002`: Workspace user not found

---

### PATCH /api/workspaces/{workspaceId}/profile
**설명**: 워크스페이스 내 프로필 이미지·이름·연락처·소개 수정

**권한**: `OWNER`, `MANAGER`, `MEMBER`, `GUEST`

**요청**
- Headers:
  - `Authorization: Bearer {accessToken}`
  - `Content-Type: multipart/form-data`
- Path Parameters:
  - `workspaceId` (number): 워크스페이스 ID
- Body (multipart):
  - `image` (file, 선택): 프로필 이미지 파일
  - `name` (string, 선택): 워크스페이스 내 이름
  - `phone` (string, 선택): 전화번호
  - `introduction` (string, 선택): 소개

**응답**: `204 No Content`

**가능한 에러**
- `A001`: Unauthorized
- `W001`: Workspace not found
- `W002`: Workspace user not found
- `F002`: File upload failed
- `F004`: File delete failed

---

### PATCH /api/workspaces/{workspaceId}/notify
**설명**: 워크스페이스 알림 설정 변경

**권한**: `OWNER`, `MANAGER`, `MEMBER`, `GUEST`

**요청**
- Headers:
  - `Authorization: Bearer {accessToken}`
  - `Content-Type: application/json`
- Path Parameters:
  - `workspaceId` (number): 워크스페이스 ID
- Body:
```json
{
  "notifyType": "MENTION"
}
```

**필드 설명**
- `notifyType` (enum, 필수): `ON`, `MENTION`, `OFF`

**Validation**
- `@NotNull`: notifyType은 필수

**응답**: `204 No Content`

**가능한 에러**
- `A001`: Unauthorized
- `C001`: Invalid input value
- `W001`: Workspace not found
- `W002`: Workspace user not found

---

### PATCH /api/workspaces/{workspaceId}/state
**설명**: 워크스페이스 내 상태 변경

**권한**: `OWNER`, `MANAGER`, `MEMBER`, `GUEST`

**요청**
- Headers:
  - `Authorization: Bearer {accessToken}`
  - `Content-Type: application/json`
- Path Parameters:
  - `workspaceId` (number): 워크스페이스 ID
- Body:
```json
{
  "state": "AWAY"
}
```

**필드 설명**
- `state` (enum, 필수): `ONLINE`, `AWAY`, `BUSY`, `OFFLINE`

**Validation**
- `@NotNull`: state는 필수

**응답**: `204 No Content`

**가능한 에러**
- `A001`: Unauthorized
- `C001`: Invalid input value
- `W001`: Workspace not found
- `W002`: Workspace user not found

---

### PATCH /api/workspaces/{workspaceId}/users/{targetUserId}/role
**설명**: 사용자 역할 변경. `OWNER`로 승격 시 기존 OWNER는 자동으로 `MANAGER`로 변경된다.

**권한**: `OWNER`, `MANAGER`

**요청**
- Headers:
  - `Authorization: Bearer {accessToken}`
  - `Content-Type: application/json`
- Path Parameters:
  - `workspaceId` (number): 워크스페이스 ID
  - `targetUserId` (number): 대상 워크스페이스 사용자 ID
- Body:
```json
{
  "role": "MANAGER"
}
```

**필드 설명**
- `role` (enum, 필수): `OWNER`, `MANAGER`, `MEMBER`, `GUEST`

**Validation**
- `@NotNull`: role은 필수

**응답**: `204 No Content`

**가능한 에러**
- `A001`: Unauthorized
- `C001`: Invalid input value
- `W001`: Workspace not found
- `W002`: Workspace user not found
- `W004`: Insufficient permission
- `W006`: Only OWNER can delegate OWNER role

---

### DELETE /api/workspaces/{workspaceId}/users/{targetUserId}
**설명**: 멤버 강제 퇴장 (soft delete)

**권한**: `OWNER`, `MANAGER`

**요청**
- Headers: `Authorization: Bearer {accessToken}`
- Path Parameters:
  - `workspaceId` (number): 워크스페이스 ID
  - `targetUserId` (number): 대상 워크스페이스 사용자 ID

**응답**: `204 No Content`

**가능한 에러**
- `A001`: Unauthorized
- `W001`: Workspace not found
- `W002`: Workspace user not found
- `W004`: Insufficient permission

---

### POST /api/workspaces/{workspaceId}/users/{targetUserId}/ban
**설명**: 사용자를 밴하고 `deletedAt`을 설정

**권한**: `OWNER`, `MANAGER`

**요청**
- Headers: `Authorization: Bearer {accessToken}`
- Path Parameters:
  - `workspaceId` (number): 워크스페이스 ID
  - `targetUserId` (number): 대상 워크스페이스 사용자 ID

**응답**: `204 No Content`

**가능한 에러**
- `A001`: Unauthorized
- `W001`: Workspace not found
- `W002`: Workspace user not found
- `W004`: Insufficient permission

---

### DELETE /api/workspaces/{workspaceId}/users/{targetUserId}/ban
**설명**: 밴 해제 (`banned=false`)

**권한**: `OWNER`, `MANAGER`

**요청**
- Headers: `Authorization: Bearer {accessToken}`
- Path Parameters:
  - `workspaceId` (number): 워크스페이스 ID
  - `targetUserId` (number): 대상 워크스페이스 사용자 ID

**응답**: `204 No Content`

**가능한 에러**
- `A001`: Unauthorized
- `W001`: Workspace not found
- `W002`: Workspace user not found
- `W004`: Insufficient permission

---

### DELETE /api/workspaces/{workspaceId}/leave
**설명**: 사용자가 자발적으로 워크스페이스를 떠난다. `OWNER`는 호출할 수 없다.

**권한**: `MANAGER`, `MEMBER`, `GUEST`

**요청**
- Headers: `Authorization: Bearer {accessToken}`
- Path Parameters:
  - `workspaceId` (number): 워크스페이스 ID

**응답**: `204 No Content`

**가능한 에러**
- `A001`: Unauthorized
- `W001`: Workspace not found
- `W002`: Workspace user not found
- `W005`: Owner cannot leave workspace

---

## 카테고리 API

### POST /api/workspaces/{workspaceId}/categories
**설명**: 새 카테고리 생성

**권한**: `OWNER`, `MANAGER`

**요청**
- Headers:
  - `Authorization: Bearer {accessToken}`
  - `Content-Type: application/json`
- Path Parameters:
  - `workspaceId` (number): 워크스페이스 ID
- Body:
```json
{
  "name": "개발"
}
```

**필드 설명**
- `name` (string, 필수): 카테고리 이름

**Validation**
- `@NotBlank`: name은 필수이며 공백 불가

**응답 200** (`CategoryResponse`)
```json
{
  "id": 1,
  "workspaceId": 1,
  "name": "개발",
  "zIndex": 1,
  "createdAt": "2025-02-14T09:30:00Z"
}
```

**필드 설명**
- `id` (number): 카테고리 ID
- `workspaceId` (number): 워크스페이스 ID
- `name` (string): 카테고리 이름
- `zIndex` (number): 정렬 순서
- `createdAt` (string): ISO-8601 UTC 형식

**가능한 에러**
- `A001`: Unauthorized
- `C001`: Invalid input value
- `W001`: Workspace not found
- `W002`: Workspace user not found
- `W004`: Insufficient permission

---

### PATCH /api/workspaces/{workspaceId}/categories/{categoryId}
**설명**: 카테고리 이름 수정

**권한**: `OWNER`, `MANAGER`

**요청**
- Headers:
  - `Authorization: Bearer {accessToken}`
  - `Content-Type: application/json`
- Path Parameters:
  - `workspaceId` (number): 워크스페이스 ID
  - `categoryId` (number): 카테고리 ID
- Body:
```json
{
  "name": "개발팀"
}
```

**필드 설명**
- `name` (string, 필수): 카테고리 이름

**Validation**
- `@NotBlank`: name은 필수이며 공백 불가

**응답 200**: `CategoryResponse`

**가능한 에러**
- `A001`: Unauthorized
- `C001`: Invalid input value
- `W001`: Workspace not found
- `W002`: Workspace user not found
- `W004`: Insufficient permission
- `CT001`: Category not found

---

### PATCH /api/workspaces/{workspaceId}/categories/{categoryId}/z-index
**설명**: 카테고리 정렬 순서 조정. `position=BETWEEN`일 경우 `beforeId`/`afterId` 필수.

**권한**: `OWNER`, `MANAGER`

**요청**
- Headers:
  - `Authorization: Bearer {accessToken}`
  - `Content-Type: application/json`
- Path Parameters:
  - `workspaceId` (number): 워크스페이스 ID
  - `categoryId` (number): 카테고리 ID
- Body:
```json
{
  "position": "BETWEEN",
  "beforeId": 5,
  "afterId": 10
}
```

**필드 설명**
- `position` (string, 필수): `FIRST`, `LAST`, `BETWEEN`
- `beforeId` (number, 선택): position=BETWEEN일 때 앞 카테고리 ID
- `afterId` (number, 선택): position=BETWEEN일 때 뒤 카테고리 ID

**Validation**
- `@NotBlank`: position은 필수
- position=BETWEEN일 경우 beforeId 또는 afterId 중 하나는 필수

**응답**: `204 No Content`

**가능한 에러**
- `A001`: Unauthorized
- `C001`: Invalid input value
- `W001`: Workspace not found
- `W002`: Workspace user not found
- `W004`: Insufficient permission
- `CT001`: Category not found
- `P001`: Invalid position

---

### DELETE /api/workspaces/{workspaceId}/categories/{categoryId}
**설명**: 카테고리 삭제 (soft delete)

**권한**: `OWNER`, `MANAGER`

**요청**
- Headers: `Authorization: Bearer {accessToken}`
- Path Parameters:
  - `workspaceId` (number): 워크스페이스 ID
  - `categoryId` (number): 카테고리 ID

**응답**: `204 No Content`

**가능한 에러**
- `A001`: Unauthorized
- `W001`: Workspace not found
- `W002`: Workspace user not found
- `W004`: Insufficient permission
- `CT001`: Category not found

---

## 채널 API

### POST /api/workspaces/{workspaceId}/categories/{categoryId}/channels
**설명**: 새 채널 생성

**권한**: `OWNER`, `MANAGER`

**요청**
- Headers:
  - `Authorization: Bearer {accessToken}`
  - `Content-Type: application/json`
- Path Parameters:
  - `workspaceId` (number): 워크스페이스 ID
  - `categoryId` (number): 카테고리 ID
- Body:
```json
{
  "name": "일반",
  "description": "일반 채팅 채널",
  "type": "CHAT"
}
```

**필드 설명**
- `name` (string, 필수): 채널 이름
- `description` (string, 선택): 채널 설명
- `type` (enum, 필수): `CHAT`, `DM`, `WEBHOOK`, `ASSISTANT`

**Validation**
- `@NotBlank`: name은 필수이며 공백 불가
- `@NotNull`: type은 필수

**응답 200** (`ChannelResponse`)
```json
{
  "id": 1,
  "workspaceId": 1,
  "categoryId": 1,
  "type": "CHAT",
  "name": "일반",
  "description": "일반 채팅 채널",
  "zIndex": 1,
  "createdAt": "2025-02-14T09:30:00Z"
}
```

**필드 설명**
- `id` (number): 채널 ID
- `workspaceId` (number): 워크스페이스 ID
- `categoryId` (number): 카테고리 ID
- `type` (enum): `CHAT`, `DM`, `WEBHOOK`, `ASSISTANT`
- `name` (string): 채널 이름
- `description` (string, nullable): 채널 설명
- `zIndex` (number): 정렬 순서
- `createdAt` (string): ISO-8601 UTC 형식

**가능한 에러**
- `A001`: Unauthorized
- `C001`: Invalid input value
- `W001`: Workspace not found
- `W002`: Workspace user not found
- `W004`: Insufficient permission
- `CT001`: Category not found

---

### GET /api/workspaces/{workspaceId}/channels/{channelId}
**설명**: 채널 기본 정보와 내 알림 설정 조회

**권한**: `OWNER`, `MANAGER`, `MEMBER`, `GUEST`

**요청**
- Headers: `Authorization: Bearer {accessToken}`
- Path Parameters:
  - `workspaceId` (number): 워크스페이스 ID
  - `channelId` (number): 채널 ID

**응답 200** (`ChannelInfoResponse`)
```json
{
  "id": 1,
  "name": "일반",
  "description": "일반 채팅 채널",
  "myNotify": "ON"
}
```

**필드 설명**
- `id` (number): 채널 ID
- `name` (string): 채널 이름
- `description` (string, nullable): 채널 설명
- `myNotify` (enum): 내 알림 설정 (`ON`, `MENTION`, `OFF`)

**가능한 에러**
- `A001`: Unauthorized
- `W001`: Workspace not found
- `W002`: Workspace user not found
- `CH001`: Channel not found
- `CH002`: Channel access denied

---

### PATCH /api/workspaces/{workspaceId}/channels/{channelId}
**설명**: 채널 이름 및 설명 수정

**권한**: `OWNER`, `MANAGER`

**요청**
- Headers:
  - `Authorization: Bearer {accessToken}`
  - `Content-Type: application/json`
- Path Parameters:
  - `workspaceId` (number): 워크스페이스 ID
  - `channelId` (number): 채널 ID
- Body:
```json
{
  "name": "일반 채팅",
  "description": "업데이트된 설명"
}
```

**필드 설명**
- `name` (string, 선택): 채널 이름
- `description` (string, 선택): 채널 설명

**응답 200**: `ChannelResponse`

**가능한 에러**
- `A001`: Unauthorized
- `W001`: Workspace not found
- `W002`: Workspace user not found
- `W004`: Insufficient permission
- `CH001`: Channel not found

---

### PATCH /api/workspaces/{workspaceId}/channels/{channelId}/z-index
**설명**: 채널 정렬 순서 조정

**권한**: `OWNER`, `MANAGER`

**요청**
- Headers:
  - `Authorization: Bearer {accessToken}`
  - `Content-Type: application/json`
- Path Parameters:
  - `workspaceId` (number): 워크스페이스 ID
  - `channelId` (number): 채널 ID
- Body:
```json
{
  "position": "FIRST",
  "beforeId": null,
  "afterId": null
}
```

**필드 설명**
- `position` (string, 필수): `FIRST`, `LAST`, `BETWEEN`
- `beforeId` (number, 선택): position=BETWEEN일 때 앞 채널 ID
- `afterId` (number, 선택): position=BETWEEN일 때 뒤 채널 ID

**응답**: `204 No Content`

**가능한 에러**
- `A001`: Unauthorized
- `W001`: Workspace not found
- `W002`: Workspace user not found
- `W004`: Insufficient permission
- `CH001`: Channel not found
- `P001`: Invalid position

---

### PATCH /api/workspaces/{workspaceId}/channels/{channelId}/notify
**설명**: 개인별 채널 알림 설정 변경

**권한**: `OWNER`, `MANAGER`, `MEMBER`, `GUEST`

**요청**
- Headers:
  - `Authorization: Bearer {accessToken}`
  - `Content-Type: application/json`
- Path Parameters:
  - `workspaceId` (number): 워크스페이스 ID
  - `channelId` (number): 채널 ID
- Body:
```json
{
  "notifyType": "MENTION"
}
```

**필드 설명**
- `notifyType` (enum, 필수): `ON`, `MENTION`, `OFF`

**Validation**
- `@NotNull`: notifyType은 필수

**응답**: `204 No Content`

**가능한 에러**
- `A001`: Unauthorized
- `C001`: Invalid input value
- `W001`: Workspace not found
- `W002`: Workspace user not found
- `CH001`: Channel not found
- `CH002`: Channel access denied

---

### DELETE /api/workspaces/{workspaceId}/channels/{channelId}
**설명**: 채널 삭제 (soft delete)

**권한**: `OWNER`, `MANAGER`

**요청**
- Headers: `Authorization: Bearer {accessToken}`
- Path Parameters:
  - `workspaceId` (number): 워크스페이스 ID
  - `channelId` (number): 채널 ID

**응답**: `204 No Content`

**가능한 에러**
- `A001`: Unauthorized
- `W001`: Workspace not found
- `W002`: Workspace user not found
- `W004`: Insufficient permission
- `CH001`: Channel not found

---

### GET /api/workspaces/{workspaceId}/channels/accessible
**설명**: 사용자가 접근 가능한 채널 목록을 카테고리별로 리턴. `permission` 값은 `ChannelPermission` 문자열이다.

**권한**: `OWNER`, `MANAGER`, `MEMBER`, `GUEST`

**요청**
- Headers: `Authorization: Bearer {accessToken}`
- Path Parameters:
  - `workspaceId` (number): 워크스페이스 ID

**응답 200** (`AccessibleChannelListResponse`)
```json
{
  "categories": [
    {
      "id": 1,
      "name": "개발",
      "channels": [
        {
          "id": 1,
          "name": "일반",
          "permission": "WRITE"
        }
      ]
    }
  ]
}
```

**필드 설명**
- `categories` (array): 카테고리 목록
  - `id` (number): 카테고리 ID
  - `name` (string): 카테고리 이름
  - `channels` (array): 채널 목록
    - `id` (number): 채널 ID
    - `name` (string): 채널 이름
    - `permission` (enum): `NONE`, `READ`, `WRITE`, `MANAGE`

**가능한 에러**
- `A001`: Unauthorized
- `W001`: Workspace not found
- `W002`: Workspace user not found

---

### GET /api/workspaces/{workspaceId}/channels/{channelId}/users
**설명**: 채널 멤버(정규/게스트) 목록. 이름 기준으로 정렬되어 있다.

**권한**: `OWNER`, `MANAGER`, `MEMBER`, `GUEST`

**요청**
- Headers: `Authorization: Bearer {accessToken}`
- Path Parameters:
  - `workspaceId` (number): 워크스페이스 ID
  - `channelId` (number): 채널 ID

**응답 200** (`ChannelUserListResponse`)
```json
{
  "regularUsers": [
    {
      "id": 10,
      "state": "ONLINE",
      "image": "https://minio.example.com/users/profile-123.jpg",
      "name": "홍길동"
    }
  ],
  "guestUsers": [
    {
      "id": 15,
      "state": "OFFLINE",
      "image": null,
      "name": "게스트1"
    }
  ]
}
```

**필드 설명**
- `regularUsers` (array): 정규 사용자 목록 (OWNER, MANAGER, MEMBER)
  - `id` (number): 워크스페이스 사용자 ID
  - `state` (enum): `ONLINE`, `AWAY`, `BUSY`, `OFFLINE`
  - `image` (string, nullable): 프로필 이미지 URL
  - `name` (string): 사용자 이름
- `guestUsers` (array): 게스트 사용자 목록 (GUEST)
  - 필드 구조는 regularUsers와 동일

**가능한 에러**
- `A001`: Unauthorized
- `W001`: Workspace not found
- `W002`: Workspace user not found
- `CH001`: Channel not found
- `CH002`: Channel access denied

---

## 그룹 API

### POST /api/workspaces/{workspaceId}/groups
**설명**: 새 그룹 생성

**권한**: `OWNER`, `MANAGER`

**요청**
- Headers:
  - `Authorization: Bearer {accessToken}`
  - `Content-Type: application/json`
- Path Parameters:
  - `workspaceId` (number): 워크스페이스 ID
- Body:
```json
{
  "name": "개발팀"
}
```

**필드 설명**
- `name` (string, 필수): 그룹 이름

**Validation**
- `@NotBlank`: name은 필수이며 공백 불가

**응답 200** (`GroupResponse`)
```json
{
  "id": 1,
  "workspaceId": 1,
  "name": "개발팀",
  "createdAt": "2025-02-14T09:30:00Z"
}
```

**필드 설명**
- `id` (number): 그룹 ID
- `workspaceId` (number): 워크스페이스 ID
- `name` (string): 그룹 이름
- `createdAt` (string): ISO-8601 UTC 형식

**가능한 에러**
- `A001`: Unauthorized
- `C001`: Invalid input value
- `W001`: Workspace not found
- `W002`: Workspace user not found
- `W004`: Insufficient permission

---

### GET /api/workspaces/{workspaceId}/groups
**설명**: 워크스페이스의 그룹 목록 조회

**권한**: `OWNER`, `MANAGER`

**요청**
- Headers: `Authorization: Bearer {accessToken}`
- Path Parameters:
  - `workspaceId` (number): 워크스페이스 ID

**응답 200** (`GroupListResponse`)
```json
{
  "groups": [
    {
      "id": 1,
      "name": "개발팀"
    }
  ]
}
```

**필드 설명**
- `groups` (array): 그룹 목록
  - `id` (number): 그룹 ID
  - `name` (string): 그룹 이름

**가능한 에러**
- `A001`: Unauthorized
- `W001`: Workspace not found
- `W002`: Workspace user not found
- `W004`: Insufficient permission

---

### GET /api/workspaces/{workspaceId}/groups/{groupId}
**설명**: 그룹 상세 정보 조회 (소속 사용자 및 채널 권한 포함)

**권한**: `OWNER`, `MANAGER`

**요청**
- Headers: `Authorization: Bearer {accessToken}`
- Path Parameters:
  - `workspaceId` (number): 워크스페이스 ID
  - `groupId` (number): 그룹 ID

**응답 200** (`GroupDetailResponse`)
```json
{
  "id": 1,
  "name": "개발팀",
  "users": [
    {
      "id": 10,
      "name": "홍길동"
    }
  ],
  "categories": [
    {
      "id": 1,
      "name": "개발",
      "channels": [
        {
          "id": 1,
          "name": "일반",
          "permission": "WRITE"
        }
      ]
    }
  ]
}
```

**필드 설명**
- `id` (number): 그룹 ID
- `name` (string): 그룹 이름
- `users` (array): 소속 사용자 목록
  - `id` (number): 워크스페이스 사용자 ID
  - `name` (string): 사용자 이름
- `categories` (array): 채널이 있는 카테고리 목록
  - `id` (number): 카테고리 ID
  - `name` (string): 카테고리 이름
  - `channels` (array): 채널 목록
    - `id` (number): 채널 ID
    - `name` (string): 채널 이름
    - `permission` (enum): `READ`, `WRITE`, `MANAGE`

**가능한 에러**
- `A001`: Unauthorized
- `W001`: Workspace not found
- `W002`: Workspace user not found
- `W004`: Insufficient permission
- `G001`: Group not found

---

### PATCH /api/workspaces/{workspaceId}/groups/{groupId}
**설명**: 그룹 정보 수정. 전달된 `userIds`와 `channels` 전체로 그룹 구성이 대체된다. `GUEST`는 추가할 수 없다.

**권한**: `OWNER`, `MANAGER`

**요청**
- Headers:
  - `Authorization: Bearer {accessToken}`
  - `Content-Type: application/json`
- Path Parameters:
  - `workspaceId` (number): 워크스페이스 ID
  - `groupId` (number): 그룹 ID
- Body:
```json
{
  "name": "개발팀 A",
  "userIds": [10, 15, 20],
  "channels": [
    {
      "channelId": 1,
      "permission": "WRITE"
    },
    {
      "channelId": 2,
      "permission": "READ"
    }
  ]
}
```

**필드 설명**
- `name` (string, 선택): 그룹 이름
- `userIds` (array, 선택): 소속시킬 워크스페이스 사용자 ID 목록 (GUEST 제외)
- `channels` (array, 선택): 채널 권한 설정
  - `channelId` (number, 필수): 채널 ID
  - `permission` (enum, 필수): `READ`, `WRITE`, `MANAGE`

**Validation**
- `@NotNull`: channels 배열 내 channelId와 permission은 필수
- GUEST 사용자는 userIds에 포함할 수 없음

**응답 200**: `GroupResponse`

**가능한 에러**
- `A001`: Unauthorized
- `C001`: Invalid input value
- `W001`: Workspace not found
- `W002`: Workspace user not found
- `W004`: Insufficient permission
- `G001`: Group not found
- `G002`: Cannot assign GUEST users to groups
- `CH001`: Channel not found

---

### DELETE /api/workspaces/{workspaceId}/groups/{groupId}
**설명**: 그룹 삭제

**권한**: `OWNER`, `MANAGER`

**요청**
- Headers: `Authorization: Bearer {accessToken}`
- Path Parameters:
  - `workspaceId` (number): 워크스페이스 ID
  - `groupId` (number): 그룹 ID

**응답**: `204 No Content`

**가능한 에러**
- `A001`: Unauthorized
- `W001`: Workspace not found
- `W002`: Workspace user not found
- `W004`: Insufficient permission
- `G001`: Group not found

---

## 추가 참고 사항
- 모든 이미지 URL은 `ImageUploadHelper`/`MinioService`가 생성한 공개 URL이며, null이면 이미지가 없는 상태이다.
- 채널 접근 목록은 `channelAccess` 캐시(SPRING Cache)를 활용하며, `ChannelService`에서 채널/카테고리 정보를 갱신할 때 `ChannelAccessService.invalidateWorkspaceCache`로 무효화한다.
- `WorkspaceInviteCreateRequest`의 legacy 필드 `allowedUserId`는 서버에서 사용하지 않는다. 동일한 기능은 `allowedUserIds` 배열로만 처리된다.
- `@CurrentUser Long` 파라미터는 JWT `id` 클레임(사용자 ID)을 그대로 주입한다. 워크스페이스 엔드포인트는 해당 값을 바탕으로 `workspaceId` + `userId` 조합으로 멤버십을 조회한다.