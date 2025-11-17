# PRIZM API 명세서

**최종 업데이트**: 2025-11-17  
**Base URL**: `http://localhost:8080` (개발)

---

## 목차
- [인증 및 보안](#인증-및-보안)
- [OAuth2 인증](#oauth2-인증)
- [인증 API](#인증-api)
- [사용자 API](#사용자-api)
- [알림 API](#알림-api)
- [워크스페이스 API](#워크스페이스-api)
- [워크스페이스 초대 API](#워크스페이스-초대-api)
- [워크스페이스 사용자 API](#워크스페이스-사용자-api)
- [카테고리 API](#카테고리-api)
- [채널 API](#채널-api)
- [그룹 API](#그룹-api)
- [에러 응답](#에러-응답)

---

## 인증 및 보안

### 기본 정보
- **Access Token**: `Authorization: Bearer {token}` 헤더 방식
- **Refresh Token**: HttpOnly Cookie 방식
- **세션 방식**: Stateless
- **CORS**: `http://localhost:3000` 허용

### Authorization Header 인증
**모든 인증 필요 API 요청 시 헤더 포함:**
```
Authorization: Bearer {access_token}
```

프론트엔드 필수 설정:
```javascript
// Refresh Token Cookie 전송을 위해
fetch: credentials: 'include'
axios: withCredentials = true

// Access Token은 헤더에 직접 설정
headers: {
  'Authorization': `Bearer ${accessToken}`
}
```

### Token 구조
#### Access Token (Authorization Header)
- 형식: JWT
- 전달: `Authorization: Bearer {token}` 헤더
- 만료: 10분 (600초)
- 저장: 클라이언트 메모리 또는 로컬 스토리지

#### Refresh Token (HttpOnly Cookie)
- 이름: `refresh_token`
- HttpOnly: true
- 만료: 7일 (604800초)
- 저장: 브라우저 자동 관리

### JWT Claims
#### Access Token
```json
{
  "role": "USER",
  "id": 123,
  "exp": 1234567890
}
```

#### Refresh Token
```json
{
  "exp": 1234567890
}
```

### Redis 저장 (Refresh Token)
```
Key: {refresh_token_string}
Value: {
  "role": "USER",
  "id": 123
}
TTL: 7일 (JWT exp와 동일)
```

### Public Endpoints
- `/api/auth/oauth2/**`
- `/api/auth/refresh`
- `/api/invites/{code}` (GET only)
- `/error`

### 워크스페이스 권한
| 역할 | 설명 |
|------|------|
| OWNER | 모든 권한 (워크스페이스 삭제 가능) |
| MANAGER | 멤버/채널 관리 |
| MEMBER | 일반 사용자 |
| GUEST | 특정 채널만 접근 |

---

## OAuth2 인증

### Google 로그인
**Endpoint**: `GET /oauth2/authorization/google`

**Response**: `200 OK`
```json
{
  "success": true,
  "accessToken": "eyJhbGc...",
  "redirectPath": "/workspace/123"
}
```

또는

```json
{
  "success": true,
  "accessToken": "eyJhbGc...",
  "redirectPath": null
}
```

**Set-Cookie**:
```
refresh_token={jwt}; HttpOnly; Secure; SameSite=Lax; Max-Age=604800
NEXT_LOCALE=ko; Path=/; Max-Age=31536000
```

**Fields**:
- `success`: Boolean (항상 true)
- `accessToken`: String (JWT)
- `redirectPath`: String | null (Redis에 저장된 마지막 접속 경로, 없으면 null)

**NEXT_LOCALE 처리**:
- 첫 가입: NEXT_LOCALE 쿠키 → User.language 저장
- 기존 유저: User.language → NEXT_LOCALE 쿠키 업데이트

---

### GitHub 로그인
**Endpoint**: `GET /oauth2/authorization/github`

Google과 동일

---

## 인증 API

### 토큰 갱신
**Endpoint**: `POST /api/auth/refresh`

**Request Headers**:
```
Cookie: refresh_token={jwt}
```

**Response**: `200 OK`
```json
{
  "accessToken": "eyJhbGc..."
}
```

**설명**:
- Refresh Token 재사용 가능
- 새로운 Access Token만 발급

**Error**:
- `401 UNAUTHORIZED`: Refresh token 없음/만료/무효

---

### 로그아웃
**Endpoint**: `POST /api/auth/logout`

**Request Headers**:
```
Authorization: Bearer {access_token}
Cookie: refresh_token={jwt}
```

**Response**: `204 No Content`

**Set-Cookie**:
```
refresh_token=; HttpOnly; Max-Age=0
```

**설명**:
- Redis에서 Refresh Token 삭제
- Refresh Token 쿠키 삭제
- Access Token은 클라이언트에서 삭제 필요

---

### 회원 탈퇴
**Endpoint**: `POST /api/auth/withdraw`

**Request Headers**:
```
Authorization: Bearer {access_token}
Cookie: refresh_token={jwt}
```

**Response**: `204 No Content`

**Set-Cookie**:
```
refresh_token=; HttpOnly; Max-Age=0
```

**설명**:
- 유저 소프트 딜리트
- Redis에서 Refresh Token 삭제
- Refresh Token 쿠키 삭제
```
access_token={new_jwt}; HttpOnly; Max-Age=600
refresh_token={new_jwt}; HttpOnly; Max-Age=604800
```

**설명**:
- 기존 Refresh Token은 Redis에서 삭제됨
- 새로운 Access Token과 Refresh Token이 발급됨
- Refresh Token Rotation으로 보안 강화
- 기존 Refresh Token은 재사용 불가

**Error**:
- `401 UNAUTHORIZED`: Refresh token 없음/만료/무효

---

### 로그아웃
**Endpoint**: `POST /api/auth/logout`

**Request**: 없음

**Response**: `204 No Content`

**Set-Cookie** (삭제):
```
refresh_token=; HttpOnly; Max-Age=0
```

---

### 회원 탈퇴
**Endpoint**: `DELETE /api/auth/withdraw`

**Request**: 없음

**Response**: `204 No Content`

**설명**: 계정 soft delete, 모든 토큰 무효화

---

## 사용자 API

### 프로필 조회
**Endpoint**: `GET /api/users/profile`

**Response**: `200 OK`
```json
{
  "profileImage": "https://...",
  "name": "홍길동",
  "email": "hong@example.com",
  "authProvider": "GOOGLE",
  "language": "KO",
  "createdAt": "2025-01-01T00:00:00Z"
}
```

**Fields**:
- `profileImage`: String (nullable)
- `name`: String
- `email`: String
- `authProvider`: "GOOGLE" | "GITHUB"
- `language`: "KO" | "EN" | "JA"
- `createdAt`: ISO 8601

---

### 프로필 수정
**Endpoint**: `PATCH /api/users/profile`

**Content-Type**: `multipart/form-data`

**Request**:
| Field | Type | Required |
|-------|------|----------|
| profileImage | File | No |
| name | String | No |

**Response**: `200 OK` (프로필 조회와 동일)

---

### 언어 변경
**Endpoint**: `PATCH /api/users/language`

**Request**:
```json
{
  "language": "EN"
}
```

**Fields**:
- `language`: "KO" | "EN" | "JA" (필수)

**Response**: `200 OK` (프로필 조회와 동일)

---

### 마지막 경로 저장
**Endpoint**: `POST /api/users/last-path`

**Request**:
```json
{
  "path": "/workspace/123/channel/456"
}
```

**Fields**:
- `path`: String

**Response**: `204 No Content`

---

### 마지막 경로 조회
**Endpoint**: `GET /api/users/last-path`

**Response**: `200 OK`
```json
{
  "path": "/workspace/123/channel/456"
}
```

**Fields**:
- `path`: String (nullable)

---

## 알림 API

### 알림 목록 조회
**Endpoint**: `GET /api/notifications`

**Response**: `200 OK`
```json
{
  "notifications": [
    {
      "id": 1,
      "type": "MENTION",
      "content": "홍길동님이 당신을 멘션했습니다",
      "isRead": false,
      "createdAt": "2025-01-15T10:30:00Z"
    }
  ]
}
```

**Fields**:
- `notifications`: Array
  - `id`: Long
  - `type`: "MENTION" | "MESSAGE" | "REACTION" | "WORKSPACE_INVITE"
  - `content`: String
  - `isRead`: Boolean
  - `createdAt`: ISO 8601

---

## 워크스페이스 API

### 워크스페이스 목록 조회
**Endpoint**: `GET /api/workspaces`

**Response**: `200 OK`
```json
[
  {
    "id": 1,
    "name": "My Workspace",
    "image": "https://..."
  }
]
```

**Fields** (각 항목):
- `id`: Long
- `name`: String
- `image`: String (nullable)

---

### 워크스페이스 생성
**Endpoint**: `POST /api/workspaces`

**Request**:
```json
{
  "name": "New Workspace"
}
```

**Fields**:
- `name`: String (필수, 1-100자)

**Response**: `200 OK`
```json
{
  "id": 2,
  "name": "New Workspace",
  "imageUrl": null,
  "createdAt": "2025-01-15T00:00:00Z"
}
```

---

### 워크스페이스 조회
**Endpoint**: `GET /api/workspaces/{workspaceId}`

**권한**: Public (인증 불필요)

**Response**: `200 OK`
```json
{
  "id": 1,
  "name": "My Workspace",
  "imageUrl": "https://...",
  "createdAt": "2025-01-15T00:00:00Z"
}
```

**Fields**:
- `id`: Long
- `name`: String
- `imageUrl`: String (nullable)
- `createdAt`: Instant (ISO 8601)

---

### 워크스페이스 수정
**Endpoint**: `PATCH /api/workspaces/{workspaceId}`

**권한**: OWNER, MANAGER

**Content-Type**: `multipart/form-data`

**Request**:
| Field | Type | Required |
|-------|------|----------|
| image | File | No |
| name | String | No |

**Response**: `200 OK` (생성 응답과 동일)

---

### 워크스페이스 삭제
**Endpoint**: `DELETE /api/workspaces/{workspaceId}`

**권한**: OWNER

**Response**: `204 No Content`

---

## 워크스페이스 초대 API

### 초대 링크 생성
**Endpoint**: `POST /api/workspaces/{workspaceId}/invites`

**권한**: 
- 멤버 초대: OWNER, MANAGER
- 게스트 초대: OWNER, MANAGER, 또는 MEMBER (해당 채널에 MANAGE 권한 있는 경우)

**Request** (멤버 초대):
```json
{
  "expiresAt": "2025-12-31T23:59:59Z",
  "usageLimit": 10,
  "autoJoinGroupIds": [1, 2]
}
```

**Request** (게스트 초대):
```json
{
  "channelId": 5,
  "allowedUserIds": [1, 2, 3],
  "expiresAt": "2025-12-31T23:59:59Z",
  "usageLimit": 5
}
```

**Fields**:
- `channelId`: Long (게스트 초대 시 필수)
- `allowedUserIds`: Array<Long> (게스트 초대 시 필수)
- `expiresAt`: ISO 8601 (선택)
- `usageLimit`: Integer (선택)
- `autoJoinGroupIds`: Array<Long> (멤버 초대 시 선택)

**Response**: `200 OK`
```json
{
  "code": "ABC123XYZ",
  "expiresAt": "2025-12-31T23:59:59Z",
  "maxUses": 10,
  "channelId": 5
}
```

**Fields**:
- `code`: String
- `expiresAt`: ISO 8601 (nullable)
- `maxUses`: Integer (nullable)
- `channelId`: Long (nullable, 게스트 초대인 경우만)

**Error**:
- `403 FORBIDDEN`: MEMBER가 해당 채널에 MANAGE 권한 없음

---

### 초대 링크 목록 조회
**Endpoint**: `GET /api/workspaces/{workspaceId}/invites`

**권한**: OWNER, MANAGER

**Response**: `200 OK`
```json
[
  {
    "code": "ABC123",
    "createdAt": "2025-01-01T00:00:00Z",
    "expiresAt": "2025-12-31T23:59:59Z",
    "usedCount": 3,
    "maxCount": 10,
    "location": "MEMBER"
  }
]
```

**Fields** (각 항목):
- `code`: String
- `createdAt`: ISO 8601
- `expiresAt`: ISO 8601 (nullable)
- `usedCount`: Integer
- `maxCount`: Integer (nullable)
- `location`: String ("MEMBER" | "GUEST:{channelName}")

---

### 초대 링크 삭제
**Endpoint**: `DELETE /api/workspaces/{workspaceId}/invites/{code}`

**권한**: OWNER, MANAGER

**Response**: `204 No Content`

---

### 초대 링크로 워크스페이스 정보 조회
**Endpoint**: `GET /api/invites/{code}`

**권한**: Public (인증 불필요)

**Response**: `200 OK`
```json
{
  "id": 1,
  "name": "My Workspace",
  "imageUrl": "https://...",
  "createdAt": "2025-01-15T00:00:00Z"
}
```

**Fields**:
- `id`: Long
- `name`: String
- `imageUrl`: String (nullable)
- `createdAt`: Instant (ISO 8601)

**Error**:
- `400 BAD_REQUEST`: 초대 코드 없음 또는 만료됨
- `404 NOT_FOUND`: 워크스페이스 삭제됨

---
- `400 BAD_REQUEST`: 만료/사용 제한 초과

---

### 초대 링크로 참여
**Endpoint**: `POST /api/invites/{code}/join`

**Response**: `200 OK`
```json
{
  "workspaceId": 1,
  "userId": 123,
  "role": "MEMBER"
}
```

**Fields**:
- `workspaceId`: Long
- `userId`: Long
- `role`: String ("MEMBER" | "GUEST")

**Error**:
- `404 NOT_FOUND`: 초대 코드 없음
- `400 BAD_REQUEST`: 만료/사용 제한 초과
- `403 FORBIDDEN`: 접근 권한 없음
- `409 CONFLICT`: 이미 멤버

---

## 워크스페이스 사용자 API

### 사용자 목록 조회
**Endpoint**: `GET /api/workspaces/{workspaceId}/users`

**권한**: OWNER, MANAGER, MEMBER

**Query Parameters**:
| Parameter | Type | Required |
|-----------|------|----------|
| role | String | No | OWNER/MANAGER/MEMBER/GUEST |

**Response**: `200 OK`
```json
{
  "users": [
    {
      "workspaceUserId": 1,
      "state": "ONLINE",
      "image": "https://...",
      "name": "홍길동",
      "email": "hong@example.com"
    }
  ]
}
```

**Fields**:
- `users`: Array
  - `workspaceUserId`: Long
  - `state`: "ONLINE" | "AWAY" | "BUSY" | "OFFLINE"
  - `image`: String (nullable)
  - `name`: String
  - `email`: String

---

### 내 프로필 조회
**Endpoint**: `GET /api/workspaces/{workspaceId}/profile`

**권한**: All

**Response**: `200 OK`
```json
{
  "notifyType": "ALL",
  "state": "ONLINE",
  "image": "https://...",
  "name": "홍길동"
}
```

**Fields**:
- `notifyType`: "ON" | "MENTION" | "OFF"
- `state`: "ONLINE" | "AWAY" | "BUSY" | "OFFLINE"
- `image`: String (nullable)
- `name`: String

---

### 사용자 프로필 조회
**Endpoint**: `GET /api/workspaces/{workspaceId}/users/{targetUserId}/profile`

**권한**: All

**Response**: `200 OK`
```json
{
  "role": "OWNER",
  "state": "ONLINE",
  "image": "https://...",
  "userName": "홍길동",
  "workspaceUserName": "홍팀장",
  "email": "hong@example.com",
  "authProvider": "GOOGLE",
  "phone": null,
  "introduction": null,
  "userCreatedAt": "2025-01-01T00:00:00Z",
  "workspaceUserCreatedAt": "2025-01-01T00:00:00Z",
  "groups": [
    {
      "id": 1,
      "name": "개발자"
    }
  ]
}
```

**Fields**:
- `role`: "OWNER" | "MANAGER" | "MEMBER" | "GUEST"
- `state`: "ONLINE" | "AWAY" | "BUSY" | "OFFLINE"
- `image`: String (nullable)
- `userName`: String (사용자 실명)
- `workspaceUserName`: String (워크스페이스 내 이름)
- `email`: String
- `authProvider`: "GOOGLE" | "GITHUB"
- `phone`: String (nullable)
- `introduction`: String (nullable)
- `userCreatedAt`: ISO 8601
- `workspaceUserCreatedAt`: ISO 8601
- `groups`: Array
  - `id`: Long
  - `name`: String

---

### 워크스페이스 프로필 수정
**Endpoint**: `PATCH /api/workspaces/{workspaceId}/profile`

**권한**: All

**Content-Type**: `multipart/form-data`

**Request**:
| Field | Type | Required |
|-------|------|----------|
| image | File | No |
| name | String | No |
| phone | String | No |
| introduction | String | No |

**Response**: `204 No Content`

**참고**: 이메일은 수정 불가

---

### 알림 설정 변경
**Endpoint**: `PATCH /api/workspaces/{workspaceId}/notify`

**권한**: All

**Request**:
```json
{
  "notifyType": "MENTION_ONLY"
}
```

**Fields**:
- `notifyType`: "ON" | "MENTION" | "OFF" (필수)

**Response**: `204 No Content`

---

### 상태 변경
**Endpoint**: `PATCH /api/workspaces/{workspaceId}/state`

**권한**: All

**Request**:
```json
{
  "state": "AWAY"
}
```

**Fields**:
- `state`: "ONLINE" | "AWAY" | "BUSY" | "OFFLINE" (필수)

**Response**: `204 No Content`

---

### 사용자 역할 변경
**Endpoint**: `PATCH /api/workspaces/{workspaceId}/users/{targetUserId}/role`

**권한**: OWNER, MANAGER

**Request**:
```json
{
  "role": "MANAGER"
}
```

**Fields**:
- `role`: "MANAGER" | "MEMBER" | "GUEST" (필수)

**Response**: `204 No Content`

**제약**: OWNER 역할 변경 불가

---

### 사용자 추방
**Endpoint**: `DELETE /api/workspaces/{workspaceId}/users/{targetUserId}`

**권한**: OWNER, MANAGER

**Response**: `204 No Content`

---

### 사용자 차단
**Endpoint**: `POST /api/workspaces/{workspaceId}/users/{targetUserId}/ban`

**권한**: OWNER, MANAGER

**Response**: `204 No Content`

---

### 사용자 차단 해제
**Endpoint**: `DELETE /api/workspaces/{workspaceId}/users/{targetUserId}/ban`

**권한**: OWNER, MANAGER

**Response**: `204 No Content`

---

### 워크스페이스 나가기
**Endpoint**: `DELETE /api/workspaces/{workspaceId}/leave`

**권한**: MANAGER, MEMBER, GUEST

**Response**: `204 No Content`

**Error**:
- `400 BAD_REQUEST`: OWNER는 나갈 수 없음

---

## 카테고리 API

### 카테고리 생성
**Endpoint**: `POST /api/workspaces/{workspaceId}/categories`

**권한**: OWNER, MANAGER

**Request**:
```json
{
  "name": "개발팀"
}
```

**Fields**:
- `name`: String (필수)

**Response**: `200 OK`
```json
{
  "id": 1,
  "workspaceId": 1,
  "name": "개발팀",
  "zIndex": "1.0",
  "createdAt": "2025-01-01T00:00:00Z"
}
```

**Fields**:
- `id`: Long
- `workspaceId`: Long
- `name`: String
- `zIndex`: String (BigDecimal)
- `createdAt`: ISO 8601

---

### 카테고리 수정
**Endpoint**: `PATCH /api/workspaces/{workspaceId}/categories/{categoryId}`

**권한**: OWNER, MANAGER

**Request**:
```json
{
  "name": "프론트엔드팀"
}
```

**Fields**:
- `name`: String (선택)

**Response**: `200 OK` (생성 응답과 동일)

---

### 카테고리 순서 변경
**Endpoint**: `PATCH /api/workspaces/{workspaceId}/categories/{categoryId}/z-index`

**권한**: OWNER, MANAGER

**Request**:
```json
{
  "position": "after",
  "beforeId": null,
  "afterId": 3
}
```

**Fields**:
- `position`: "before" | "after" | "first" | "last"
- `beforeId`: Long (nullable)
- `afterId`: Long (nullable)

**Response**: `204 No Content`

---

### 카테고리 삭제
**Endpoint**: `DELETE /api/workspaces/{workspaceId}/categories/{categoryId}`

**권한**: OWNER, MANAGER

**Response**: `204 No Content`

---

## 채널 API

### 채널 생성
**Endpoint**: `POST /api/workspaces/{workspaceId}/categories/{categoryId}/channels`

**권한**: OWNER, MANAGER

**Request**:
```json
{
  "name": "일반",
  "description": "일반 대화방",
  "type": "PUBLIC"
}
```

**Fields**:
- `name`: String (필수)
- `description`: String (선택)
- `type`: "PUBLIC" | "PRIVATE" (필수)

**Response**: `200 OK`
```json
{
  "id": 1,
  "workspaceId": 1,
  "categoryId": 1,
  "type": "PUBLIC",
  "name": "일반",
  "description": "일반 대화방",
  "zIndex": "1.0",
  "createdAt": "2025-01-01T00:00:00Z"
}
```

**Fields**:
- `id`: Long
- `workspaceId`: Long
- `categoryId`: Long (nullable)
- `type`: "PUBLIC" | "PRIVATE"
- `name`: String
- `description`: String (nullable)
- `zIndex`: String (BigDecimal)
- `createdAt`: ISO 8601

---

### 채널 정보 조회
**Endpoint**: `GET /api/workspaces/{workspaceId}/channels/{channelId}`

**권한**: All (접근 가능한 채널만)

**Response**: `200 OK`
```json
{
  "id": 1,
  "name": "일반",
  "description": "일반 대화방",
  "myNotify": "ON"
}
```

**Fields**:
- `id`: Long
- `name`: String
- `description`: String (nullable)
- `myNotify`: "ON" | "MENTION" | "OFF"

---

### 채널 수정
**Endpoint**: `PATCH /api/workspaces/{workspaceId}/channels/{channelId}`

**권한**: OWNER, MANAGER

**Request**:
```json
{
  "name": "개발-일반",
  "description": "개발팀 일반 대화"
}
```

**Fields**:
- `name`: String (선택)
- `description`: String (선택)

**Response**: `200 OK` (생성 응답과 동일)

---

### 채널 순서 변경
**Endpoint**: `PATCH /api/workspaces/{workspaceId}/channels/{channelId}/z-index`

**권한**: OWNER, MANAGER

**Request**:
```json
{
  "position": "after",
  "beforeId": null,
  "afterId": 3
}
```

**Fields**:
- `position`: "before" | "after" | "first" | "last"
- `beforeId`: Long (nullable)
- `afterId`: Long (nullable)

**Response**: `204 No Content`

---

### 채널 알림 설정
**Endpoint**: `PATCH /api/workspaces/{workspaceId}/channels/{channelId}/notify`

**권한**: All

**Request**:
```json
{
  "notifyType": "MENTION"
}
```

**Fields**:
- `notifyType`: "ON" | "MENTION" | "OFF" (필수)

**Response**: `204 No Content`

**참고**: `channel_workspace_users`에 레코드가 없으면 자동 생성됨

---

### 채널 삭제
**Endpoint**: `DELETE /api/workspaces/{workspaceId}/channels/{channelId}`

**권한**: OWNER, MANAGER

**Response**: `204 No Content`

---

### 접근 가능한 채널 목록
**Endpoint**: `GET /api/workspaces/{workspaceId}/channels/accessible`

**권한**: All

**Response**: `200 OK`
```json
{
  "categories": [
    {
      "id": 1,
      "name": "개발팀",
      "channels": [
        {
          "id": 1,
          "name": "일반",
          "permission": "READ_WRITE"
        }
      ]
    }
  ]
}
```

**Fields**:
- `categories`: Array
  - `id`: Long
  - `name`: String
  - `channels`: Array
    - `id`: Long
    - `name`: String
    - `permission`: "READ_WRITE" | "READ_ONLY" | "NONE"

---

### 채널 사용자 목록
**Endpoint**: `GET /api/workspaces/{workspaceId}/channels/{channelId}/users`

**권한**: All

**Response**: `200 OK`
```json
{
  "regularUsers": [
    {
      "id": 1,
      "state": "ONLINE",
      "image": "https://...",
      "name": "홍길동"
    }
  ],
  "guestUsers": []
}
```

**Fields**:
- `regularUsers`: Array
  - `id`: Long (workspaceUserId)
  - `state`: "ONLINE" | "AWAY" | "BUSY" | "OFFLINE"
  - `image`: String (nullable)
  - `name`: String
- `guestUsers`: Array (같은 구조)

---

## 그룹 API

### 그룹 생성
**Endpoint**: `POST /api/workspaces/{workspaceId}/groups`

**권한**: OWNER, MANAGER

**Request**:
```json
{
  "name": "개발자"
}
```

**Fields**:
- `name`: String (필수)

**Response**: `200 OK`
```json
{
  "id": 1,
  "workspaceId": 1,
  "name": "개발자",
  "createdAt": "2025-01-01T00:00:00Z"
}
```

**Fields**:
- `id`: Long
- `workspaceId`: Long
- `name`: String
- `createdAt`: ISO 8601

---

### 그룹 목록 조회
**Endpoint**: `GET /api/workspaces/{workspaceId}/groups`

**권한**: OWNER, MANAGER

**Response**: `200 OK`
```json
{
  "groups": [
    {
      "id": 1,
      "name": "개발자"
    }
  ]
}
```

**Fields**:
- `groups`: Array
  - `id`: Long
  - `name`: String

---

### 그룹 상세 조회
**Endpoint**: `GET /api/workspaces/{workspaceId}/groups/{groupId}`

**권한**: OWNER, MANAGER

**Response**: `200 OK`
```json
{
  "id": 1,
  "name": "개발자",
  "users": [
    {
      "id": 1,
      "name": "홍길동"
    }
  ],
  "categories": [
    {
      "id": 1,
      "name": "개발팀",
      "channels": [
        {
          "id": 1,
          "name": "일반",
          "permission": "READ_WRITE"
        }
      ]
    }
  ]
}
```

**Fields**:
- `id`: Long
- `name`: String
- `users`: Array
  - `id`: Long (workspaceUserId)
  - `name`: String
- `categories`: Array
  - `id`: Long
  - `name`: String
  - `channels`: Array
    - `id`: Long
    - `name`: String
    - `permission`: "READ_WRITE" | "READ_ONLY" | "NONE"

---

### 그룹 수정
**Endpoint**: `PATCH /api/workspaces/{workspaceId}/groups/{groupId}`

**권한**: OWNER, MANAGER

**Request**:
```json
{
  "name": "프론트엔드 개발자",
  "userIds": [1, 2, 3],
  "channels": [
    {
      "channelId": 1,
      "permission": "READ_WRITE"
    },
    {
      "channelId": 2,
      "permission": "READ_ONLY"
    }
  ]
}
```

**Fields**:
- `name`: String (선택)
- `userIds`: Array<Long> (선택)
- `channels`: Array (선택)
  - `channelId`: Long
  - `permission`: "READ_WRITE" | "READ_ONLY" | "NONE"

**Response**: `200 OK` (생성 응답과 동일)

**제약**: GUEST 사용자는 그룹에 추가 불가

---

### 그룹 삭제
**Endpoint**: `DELETE /api/workspaces/{workspaceId}/groups/{groupId}`

**권한**: OWNER, MANAGER

**Response**: `204 No Content`

---

## 에러 응답

### 에러 형식
```json
{
  "code": "W001",
  "message": "Workspace not found",
  "timestamp": "2025-01-15T10:30:00Z"
}
```

### HTTP 상태 코드
| Code | Description |
|------|-------------|
| 200 | 성공 |
| 201 | 생성 성공 |
| 204 | 성공 (응답 본문 없음) |
| 400 | 잘못된 요청 |
| 401 | 인증 필요 |
| 403 | 권한 없음 |
| 404 | 리소스 없음 |
| 409 | 충돌 |
| 500 | 서버 오류 |

### 에러 코드
#### Common (C)
- C001: Invalid input value
- C002: Internal server error

#### Auth (A)
- A001: Unauthorized
- A002: Forbidden
- A003: Invalid token
- A004: Token expired

#### User (U)
- U001: User not found
- U002: User already exists
- U003: User is banned

#### Workspace (W)
- W001: Workspace not found
- W002: Workspace user not found
- W003: Workspace user already exists
- W004: Insufficient permission
- W005: Owner cannot leave workspace

#### Category (CT)
- CT001: Category not found

#### Channel (CH)
- CH001: Channel not found
- CH002: Channel access denied

#### Group (G)
- G001: Group not found
- G002: Cannot assign GUEST to group

#### Invite (I)
- I001: Invite not found
- I002: Invite expired
- I003: Invite usage limit reached
- I004: Invite restricted

#### File (F)
- F001: File not found
- F002: File upload failed

---

**문서 버전**: 2.0  
**최종 업데이트**: 2025-11-17