# PRIZM API 명세서

**Base URL**: `http://localhost:8080`
**인증 방식**: Bearer Token (JWT)

---

## 인증

### OAuth2 로그인 플로우
1. 프론트엔드에서 `GET /api/auth/oauth2/{provider}` 로 리다이렉트 (provider: `google`, `github`)
2. 사용자가 OAuth2 제공자로 인증
3. 백엔드가 refresh token을 HttpOnly 쿠키로 설정
4. 프론트엔드에서 `/api/auth/refresh` 호출하여 access token 획득
5. 이후 요청에서 `Authorization: Bearer {token}` 헤더 사용

---

## 에러 응답 형식

```json
{
  "code": "W001",
  "message": "Workspace not found",
  "timestamp": "2025-11-18T10:30:00Z"
}
```

### 에러 코드

| 코드 | 상태 | 메시지 |
|------|------|--------|
| C001 | 400 | Invalid input value |
| C002 | 500 | Internal server error |
| A001 | 401 | Unauthorized |
| A002 | 403 | Forbidden |
| A003 | 401 | Invalid token |
| A004 | 401 | Token expired |
| A005 | 401 | Refresh token not found |
| A006 | 401 | Invalid or expired refresh token |
| A007 | 401 | Refresh token not found in storage |
| A008 | 401 | Invalid authentication |
| U001 | 404 | User not found |
| U002 | 409 | User already exists |
| U003 | 403 | User is banned |
| U004 | 403 | User is deleted |
| W001 | 404 | Workspace not found |
| W002 | 404 | Workspace user not found |
| W003 | 409 | Workspace user already exists |
| W004 | 403 | Insufficient permission |
| W005 | 400 | Owner cannot leave workspace |
| W006 | 403 | Only OWNER can delegate OWNER role |
| W007 | 400 | Channel not in workspace |
| W008 | 403 | User is banned from this workspace |
| W009 | 409 | User already joined workspace |
| W010 | 403 | User not allowed to create invite |
| W011 | 404 | Workspace is deleted |
| CT001 | 404 | Category not found |
| CH001 | 404 | Channel not found |
| CH002 | 403 | Channel access denied |
| G001 | 404 | Group not found |
| G002 | 400 | Cannot assign GUEST users to groups |
| I001 | 404 | Invite not found |
| I002 | 400 | Invite expired |
| I003 | 400 | Invite usage limit reached |
| I004 | 403 | Invite restricted to specific users |
| I005 | 400 | Guest invite requires allowed user IDs |
| I006 | 403 | Only OWNER, MANAGER, or MEMBER with MANAGE permission can create guest invite |
| I007 | 403 | MEMBER requires MANAGE permission on this channel to create guest invite |
| I008 | 400 | Invite not for this workspace |
| I009 | 403 | User not allowed to use this invite |
| I010 | 404 | Allowed user not found |
| F001 | 404 | File not found |
| F002 | 500 | File upload failed |
| F003 | 500 | File download failed |
| F004 | 500 | File delete failed |
| T001 | 400 | Invalid language code |
| T002 | 500 | Translation failed |
| R001 | 500 | Cache operation failed |
| P001 | 400 | Invalid position |

---

## 인증 API

### POST /api/auth/refresh
refresh token 쿠키로 새 access token 발급

**요청**
- Cookie: `refresh_token` (필수)

**응답 200**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**에러**: A005, A006, A007, U001, U004

---

### POST /api/auth/logout
로그아웃 및 refresh token 무효화

**요청**
- Header: `Authorization: Bearer {token}`
- Cookie: `refresh_token`

**응답 204**
내용 없음

**에러**: 없음

---

### DELETE /api/auth/withdraw
회원 탈퇴

**요청**
- Header: `Authorization: Bearer {token}`

**응답 204**
내용 없음

**에러**: A001, A003, A004, U001

---

## 사용자 API

### GET /api/users/profile
현재 사용자 프로필 조회

**요청**
- Header: `Authorization: Bearer {token}`

**응답 200**
```json
{
  "profileImage": "https://storage.example.com/profile.jpg",
  "name": "홍길동",
  "email": "user@example.com",
  "authProvider": "GOOGLE",
  "language": "KO",
  "createdAt": "2025-01-15T08:30:00Z"
}
```

**에러**: A001, A003, A004, U001, U004

---

### PATCH /api/users/profile
사용자 프로필 수정

**요청**
- Header: `Authorization: Bearer {token}`
- Content-Type: `multipart/form-data`
- Body:
  - `profileImage` (파일, 선택)
  - `name` (문자열, 선택)

**응답 200**
```json
{
  "profileImage": "https://storage.example.com/profile.jpg",
  "name": "홍길동",
  "email": "user@example.com",
  "authProvider": "GOOGLE",
  "language": "KO",
  "createdAt": "2025-01-15T08:30:00Z"
}
```

**에러**: A001, A003, A004, U001, U004, F002, F004

---

### PATCH /api/users/language
사용자 언어 설정 변경

**요청**
- Header: `Authorization: Bearer {token}`
- Body:
```json
{
  "language": "EN"
}
```
- `language`: KO | EN | JA | FR (필수)

**응답 200**
```json
{
  "profileImage": "https://storage.example.com/profile.jpg",
  "name": "홍길동",
  "email": "user@example.com",
  "authProvider": "GOOGLE",
  "language": "EN",
  "createdAt": "2025-01-15T08:30:00Z"
}
```

**에러**: A001, A003, A004, C001, U001, U004, T001

---

## 알림 API

### GET /api/notifications
사용자 알림 목록 조회

**요청**
- Header: `Authorization: Bearer {token}`

**응답 200**
```json
{
  "notifications": [
    {
      "id": 1,
      "type": "MESSAGE",
      "senderId": 5,
      "content": "김철수님의 새 메시지",
      "locationId": 10,
      "important": false,
      "read": false,
      "createdAt": "2025-11-18T10:30:00Z"
    }
  ]
}
```
- `type`: SYSTEM | INVITE | MESSAGE

**에러**: A001, A003, A004, U001, U004

---

## 워크스페이스 API

### GET /api/workspaces
사용자가 속한 모든 워크스페이스 조회

**요청**
- Header: `Authorization: Bearer {token}`

**응답 200**
```json
[
  {
    "id": 1,
    "name": "내 워크스페이스",
    "image": "https://storage.example.com/workspace.jpg"
  }
]
```

**에러**: A001, A003, A004

---

### POST /api/workspaces
새 워크스페이스 생성

**요청**
- Header: `Authorization: Bearer {token}`
- Body:
```json
{
  "name": "새 워크스페이스",
  "imageId": 10
}
```
- `name` (문자열, 필수)
- `imageId` (숫자, 선택)

**응답 200**
```json
{
  "id": 1,
  "name": "새 워크스페이스",
  "imageUrl": "https://storage.example.com/workspace.jpg",
  "createdAt": "2025-11-18T10:30:00Z"
}
```

**에러**: A001, A003, A004, C001, U001

---

### GET /api/workspaces/{workspaceId}
워크스페이스 상세 조회

**요청**
- Header: `Authorization: Bearer {token}`

**응답 200**
```json
{
  "id": 1,
  "name": "내 워크스페이스",
  "imageUrl": "https://storage.example.com/workspace.jpg",
  "createdAt": "2025-11-18T10:30:00Z"
}
```

**에러**: A001, A003, A004, W001

---

### PATCH /api/workspaces/{workspaceId}
워크스페이스 수정 (OWNER, MANAGER만 가능)

**요청**
- Header: `Authorization: Bearer {token}`
- Content-Type: `multipart/form-data`
- Body:
  - `image` (파일, 선택)
  - `name` (문자열, 선택)

**응답 200**
```json
{
  "id": 1,
  "name": "수정된 워크스페이스",
  "imageUrl": "https://storage.example.com/workspace.jpg",
  "createdAt": "2025-11-18T10:30:00Z"
}
```

**에러**: A001, A003, A004, W001, W004, F002, F004

---

### DELETE /api/workspaces/{workspaceId}
워크스페이스 삭제 (OWNER만 가능)

**요청**
- Header: `Authorization: Bearer {token}`

**응답 204**
내용 없음

**에러**: A001, A003, A004, W004

---

## 워크스페이스 멤버 API

### GET /api/workspaces/{workspaceId}/users
워크스페이스 멤버 목록 조회 (OWNER, MANAGER, MEMBER만 가능)

**요청**
- Header: `Authorization: Bearer {token}`
- Query: `role` (선택): OWNER | MANAGER | MEMBER | GUEST

**응답 200**
```json
{
  "users": [
    {
      "workspaceUserId": 1,
      "state": "ONLINE",
      "image": "https://storage.example.com/user.jpg",
      "name": "홍길동",
      "email": "user@example.com"
    }
  ]
}
```
- `state`: ONLINE | AWAY | BUSY | OFFLINE

**에러**: A001, A003, A004, W001, W002, W004

---

### GET /api/workspaces/{workspaceId}/profile
현재 사용자의 워크스페이스 프로필 조회

**요청**
- Header: `Authorization: Bearer {token}`

**응답 200**
```json
{
  "notifyType": "ON",
  "state": "ONLINE",
  "image": "https://storage.example.com/user.jpg",
  "name": "홍길동"
}
```
- `notifyType`: ON | MENTION | OFF
- `state`: ONLINE | AWAY | BUSY | OFFLINE

**에러**: A001, A003, A004, W002

---

### GET /api/workspaces/{workspaceId}/users/{targetUserId}/profile
다른 사용자의 워크스페이스 프로필 조회

**요청**
- Header: `Authorization: Bearer {token}`

**응답 200**
```json
{
  "role": "MEMBER",
  "state": "ONLINE",
  "image": "https://storage.example.com/user.jpg",
  "userName": "홍길동",
  "workspaceUserName": "길동",
  "email": "user@example.com",
  "authProvider": "GOOGLE",
  "phone": "010-1234-5678",
  "introduction": "안녕하세요",
  "userCreatedAt": "2025-01-15T08:30:00Z",
  "workspaceUserCreatedAt": "2025-01-20T09:00:00Z",
  "groups": [
    {
      "id": 1,
      "name": "개발팀"
    }
  ]
}
```
- `role`: OWNER | MANAGER | MEMBER | GUEST
- `authProvider`: GOOGLE | GITHUB

**에러**: A001, A003, A004, W002

---

### PATCH /api/workspaces/{workspaceId}/profile
워크스페이스 프로필 수정

**요청**
- Header: `Authorization: Bearer {token}`
- Content-Type: `multipart/form-data`
- Body:
  - `image` (파일, 선택)
  - `name` (문자열, 선택)
  - `phone` (문자열, 선택)
  - `introduction` (문자열, 선택)

**응답 204**
내용 없음

**에러**: A001, A003, A004, W002, F002, F004

---

### PATCH /api/workspaces/{workspaceId}/notify
알림 설정 변경

**요청**
- Header: `Authorization: Bearer {token}`
- Body:
```json
{
  "notifyType": "MENTION"
}
```
- `notifyType`: ON | MENTION | OFF (필수)

**응답 204**
내용 없음

**에러**: A001, A003, A004, C001, W002

---

### PATCH /api/workspaces/{workspaceId}/state
상태 메시지 변경

**요청**
- Header: `Authorization: Bearer {token}`
- Body:
```json
{
  "state": "AWAY"
}
```
- `state`: ONLINE | AWAY | BUSY | OFFLINE (필수)

**응답 204**
내용 없음

**에러**: A001, A003, A004, C001, W002

---

### PATCH /api/workspaces/{workspaceId}/users/{targetUserId}/role
사용자 역할 변경 (OWNER, MANAGER만 가능)

**요청**
- Header: `Authorization: Bearer {token}`
- Body:
```json
{
  "role": "MANAGER"
}
```
- `role`: OWNER | MANAGER | MEMBER | GUEST (필수)

**응답 204**
내용 없음

**에러**: A001, A003, A004, C001, W002, W004, W006

---

### DELETE /api/workspaces/{workspaceId}/users/{targetUserId}
워크스페이스에서 사용자 추방 (OWNER, MANAGER만 가능)

**요청**
- Header: `Authorization: Bearer {token}`

**응답 204**
내용 없음

**에러**: A001, A003, A004, W002, W004

---

### POST /api/workspaces/{workspaceId}/users/{targetUserId}/ban
사용자 차단 (OWNER, MANAGER만 가능)

**요청**
- Header: `Authorization: Bearer {token}`

**응답 204**
내용 없음

**에러**: A001, A003, A004, W002, W004

---

### DELETE /api/workspaces/{workspaceId}/users/{targetUserId}/ban
사용자 차단 해제 (OWNER, MANAGER만 가능)

**요청**
- Header: `Authorization: Bearer {token}`

**응답 204**
내용 없음

**에러**: A001, A003, A004, W002, W004

---

### DELETE /api/workspaces/{workspaceId}/leave
워크스페이스 나가기 (MANAGER, MEMBER, GUEST만 가능)

**요청**
- Header: `Authorization: Bearer {token}`

**응답 204**
내용 없음

**에러**: A001, A003, A004, W002, W005

---

## 초대 API

### POST /api/workspaces/{workspaceId}/invites
워크스페이스 초대 코드 생성 (OWNER, MANAGER, MEMBER)

**요청**
- Header: `Authorization: Bearer {token}`
- Body:
```json
{
  "expiresInSeconds": 86400,
  "maxUses": 10,
  "allowedUserIds": [1, 2, 3],
  "autoJoinGroupIds": [5],
  "channelId": null
}
```
- `expiresInSeconds` (숫자, 선택, 최소: 1): null이면 만료 없음
- `maxUses` (숫자, 선택, 최소: 1): null이면 무제한 사용
- `allowedUserIds` (배열, 선택): 복수 사용자 제한 (게스트 초대 시 필수)
- `autoJoinGroupIds` (배열, 선택): 자동 참여 그룹
- `channelId` (숫자, 선택): 게스트 초대용 (지정 시 allowedUserIds 필수)

**응답 200**
```json
{
  "code": "abc123xyz",
  "expiresAt": "2025-11-19T10:30:00Z",
  "maxUses": 10,
  "channelId": null
}
```
- 게스트 초대의 경우 `expiresAt`, `maxUses` 는 null일 수 있음

**에러**: A001, A003, A004, C001, W002, W004, W007, W010, CH001, I005, I006, I007, I010

---

### GET /api/workspaces/{workspaceId}/invites
워크스페이스 초대 목록 조회 (OWNER, MANAGER만 가능)

**요청**
- Header: `Authorization: Bearer {token}`

**응답 200**
```json
[
  {
    "code": "abc123xyz",
    "createdAt": "2025-11-18T10:30:00Z",
    "expiresAt": "2025-11-19T10:30:00Z",
    "usedCount": 3,
    "maxCount": 10,
    "location": "일반 채널"
  }
]
```

**에러**: A001, A003, A004, W004

---

### DELETE /api/workspaces/{workspaceId}/invites/{code}
초대 코드 삭제 (OWNER, MANAGER만 가능)

**요청**
- Header: `Authorization: Bearer {token}`

**응답 204**
내용 없음

**에러**: A001, A003, A004, W004, I001, I008

---

### GET /api/invites/{code}
초대 코드로 워크스페이스 정보 조회

**요청**
- Header: `Authorization: Bearer {token}`

**응답 200**
```json
{
  "id": 1,
  "name": "내 워크스페이스",
  "imageUrl": "https://storage.example.com/workspace.jpg",
  "createdAt": "2025-11-18T10:30:00Z"
}
```

**에러**: A001, A003, A004, I001, I002, W001, W011

---

### POST /api/invites/{code}/join
초대 코드로 워크스페이스 참여

**요청**
- Header: `Authorization: Bearer {token}`

**응답 200**
```json
{
  "workspaceId": 1,
  "userId": 5,
  "role": "MEMBER"
}
```
- `userId`: 워크스페이스 사용자 ID

**에러**: A001, A003, A004, I001, I002, I003, I004, I008, I009, U001, U003, W001, W008, W009, W011

---

## 카테고리 API

### POST /api/workspaces/{workspaceId}/categories
카테고리 생성 (OWNER, MANAGER만 가능)

**요청**
- Header: `Authorization: Bearer {token}`
- Body:
```json
{
  "name": "개발"
}
```
- `name` (문자열, 필수)

**응답 200**
```json
{
  "id": 1,
  "workspaceId": 1,
  "name": "개발",
  "zIndex": "100.0",
  "createdAt": "2025-11-18T10:30:00Z"
}
```

**에러**: A001, A003, A004, C001, W001, W004

---

### PATCH /api/workspaces/{workspaceId}/categories/{categoryId}
카테고리 수정 (OWNER, MANAGER만 가능)

**요청**
- Header: `Authorization: Bearer {token}`
- Body:
```json
{
  "name": "수정된 이름"
}
```
- `name` (문자열, 선택)

**응답 200**
```json
{
  "id": 1,
  "workspaceId": 1,
  "name": "수정된 이름",
  "zIndex": "100.0",
  "createdAt": "2025-11-18T10:30:00Z"
}
```

**에러**: A001, A003, A004, C001, CT001, W004

---

### PATCH /api/workspaces/{workspaceId}/categories/{categoryId}/z-index
카테고리 위치 변경 (OWNER, MANAGER만 가능)

**요청**
- Header: `Authorization: Bearer {token}`
- Body:
```json
{
  "position": "BETWEEN",
  "beforeId": 5,
  "afterId": null
}
```
- `position`: FIRST | LAST | BETWEEN (필수)
- `beforeId` (숫자, `BETWEEN` 시 필수)
- `afterId` (숫자, `BETWEEN` 시 필수)

**응답 204**
내용 없음

**에러**: A001, A003, A004, C001, CT001, W004, P001

---

### DELETE /api/workspaces/{workspaceId}/categories/{categoryId}
카테고리 삭제 (OWNER, MANAGER만 가능)

**요청**
- Header: `Authorization: Bearer {token}`

**응답 204**
내용 없음

**에러**: A001, A003, A004, CT001, W004

---

## 채널 API

### POST /api/workspaces/{workspaceId}/categories/{categoryId}/channels
채널 생성 (OWNER, MANAGER만 가능)

**요청**
- Header: `Authorization: Bearer {token}`
- Body:
```json
{
  "name": "general",
  "description": "일반 대화",
  "type": "CHAT"
}
```
- `name` (문자열, 필수)
- `description` (문자열, 선택)
- `type`: CHAT | DM | WEBHOOK | ASSISTANT (필수)

**응답 200**
```json
{
  "id": 1,
  "workspaceId": 1,
  "categoryId": 1,
  "type": "CHAT",
  "name": "general",
  "description": "일반 대화",
  "zIndex": "100.0",
  "createdAt": "2025-11-18T10:30:00Z"
}
```

**에러**: A001, A003, A004, C001, W001, CT001, W004

---

### GET /api/workspaces/{workspaceId}/channels/{channelId}
채널 정보 조회

**요청**
- Header: `Authorization: Bearer {token}`

**응답 200**
```json
{
  "id": 1,
  "name": "general",
  "description": "일반 대화",
  "myNotify": "ON"
}
```
- `myNotify`: ON | MENTION | OFF

**에러**: A001, A003, A004, CH001, W002

---

### PATCH /api/workspaces/{workspaceId}/channels/{channelId}
채널 수정 (OWNER, MANAGER만 가능)

**요청**
- Header: `Authorization: Bearer {token}`
- Body:
```json
{
  "name": "updated-general",
  "description": "수정된 설명"
}
```
- `name` (문자열, 선택)
- `description` (문자열, 선택)

**응답 200**
```json
{
  "id": 1,
  "workspaceId": 1,
  "categoryId": 1,
  "type": "CHAT",
  "name": "updated-general",
  "description": "수정된 설명",
  "zIndex": "100.0",
  "createdAt": "2025-11-18T10:30:00Z"
}
```

**에러**: A001, A003, A004, C001, CH001, W004

---

### PATCH /api/workspaces/{workspaceId}/channels/{channelId}/z-index
채널 위치 변경 (OWNER, MANAGER만 가능)

**요청**
- Header: `Authorization: Bearer {token}`
- Body:
```json
{
  "position": "BETWEEN",
  "beforeId": 5,
  "afterId": null
}
```
- `position`: FIRST | LAST | BETWEEN (필수)
- `beforeId` (숫자, `BETWEEN` 시 필수)
- `afterId` (숫자, `BETWEEN` 시 필수)

**응답 204**
내용 없음

**에러**: A001, A003, A004, C001, CH001, W004, P001

---

### PATCH /api/workspaces/{workspaceId}/channels/{channelId}/notify
채널 알림 설정 변경

**요청**
- Header: `Authorization: Bearer {token}`
- Body:
```json
{
  "notifyType": "MENTION"
}
```
- `notifyType`: ON | MENTION | OFF (필수)

**응답 204**
내용 없음

**에러**: A001, A003, A004, C001, CH001, W002

---

### DELETE /api/workspaces/{workspaceId}/channels/{channelId}
채널 삭제 (OWNER, MANAGER만 가능)

**요청**
- Header: `Authorization: Bearer {token}`

**응답 204**
내용 없음

**에러**: A001, A003, A004, CH001, W004

---

### GET /api/workspaces/{workspaceId}/channels/accessible
워크스페이스 내 접근 가능한 모든 채널 조회

**요청**
- Header: `Authorization: Bearer {token}`

**응답 200**
```json
{
  "categories": [
    {
      "id": 1,
      "name": "개발",
      "channels": [
        {
          "id": 1,
          "name": "general",
          "permission": "WRITE"
        }
      ]
    }
  ]
}
```
- `permission`: READ | WRITE | MANAGE

**에러**: A001, A003, A004, W002

---

### GET /api/workspaces/{workspaceId}/channels/{channelId}/users
채널 내 사용자 목록 조회

**요청**
- Header: `Authorization: Bearer {token}`

**응답 200**
```json
{
  "regularUsers": [
    {
      "id": 1,
      "state": "ONLINE",
      "image": "https://storage.example.com/user.jpg",
      "name": "홍길동"
    }
  ],
  "guestUsers": []
}
```
- `state`: ONLINE | AWAY | BUSY | OFFLINE

**에러**: A001, A003, A004, W002

---

## 그룹 API

### POST /api/workspaces/{workspaceId}/groups
그룹 생성 (OWNER, MANAGER만 가능)

**요청**
- Header: `Authorization: Bearer {token}`
- Body:
```json
{
  "name": "개발팀"
}
```
- `name` (문자열, 필수)

**응답 200**
```json
{
  "id": 1,
  "workspaceId": 1,
  "name": "개발팀",
  "createdAt": "2025-11-18T10:30:00Z"
}
```

**에러**: A001, A003, A004, C001, W001, W004

---

### GET /api/workspaces/{workspaceId}/groups
그룹 목록 조회 (OWNER, MANAGER만 가능)

**요청**
- Header: `Authorization: Bearer {token}`

**응답 200**
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

**에러**: A001, A003, A004, W001, W004

---

### GET /api/workspaces/{workspaceId}/groups/{groupId}
그룹 상세 조회 (OWNER, MANAGER만 가능)

**요청**
- Header: `Authorization: Bearer {token}`

**응답 200**
```json
{
  "id": 1,
  "name": "개발팀",
  "users": [
    {
      "id": 1,
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
          "name": "general",
          "permission": "WRITE"
        }
      ]
    }
  ]
}
```
- `permission`: READ | WRITE | MANAGE

**에러**: A001, A003, A004, G001, W004

---

### PATCH /api/workspaces/{workspaceId}/groups/{groupId}
그룹 수정 (OWNER, MANAGER만 가능)

**요청**
- Header: `Authorization: Bearer {token}`
- Body:
```json
{
  "name": "수정된 그룹",
  "userIds": [1, 2, 3],
  "channels": [
    {
      "channelId": 1,
      "permission": "WRITE"
    }
  ]
}
```
- `name` (문자열, 선택)
- `userIds` (배열, 선택): 할당할 워크스페이스 사용자 ID 목록
- `channels` (배열, 선택): 채널 권한 목록
  - `channelId` (숫자, 필수)
  - `permission`: READ | WRITE | MANAGE (필수)

**응답 200**
```json
{
  "id": 1,
  "workspaceId": 1,
  "name": "수정된 그룹",
  "createdAt": "2025-11-18T10:30:00Z"
}
```

**에러**: A001, A003, A004, C001, G001, CH001, G002, W002, W004

---

### DELETE /api/workspaces/{workspaceId}/groups/{groupId}
그룹 삭제 (OWNER, MANAGER만 가능)

**요청**
- Header: `Authorization: Bearer {token}`

**응답 204**
내용 없음

**에러**: A001, A003, A004, G001, W004

---

## 채팅 API

### WebSocket: /chat.send
채널에 메시지 전송

**엔드포인트**: `/chat.send`
**프로토콜**: WebSocket (STOMP)

**요청**
```json
{
  "channelId": 1,
  "workspaceUserId": 5,
  "contentType": "TEXT",
  "content": "안녕하세요"
}
```
- `channelId` (숫자, 필수)
- `workspaceUserId` (숫자, 필수)
- `contentType` (문자열, 필수): TEXT | LINK | MEDIA | DOCUMENT | FILE
- `content` (문자열, 필수)

**응답**
채널 구독자에게 브로드캐스트

**에러**: CH001, W002, C001

---

### WebSocket: /chat.translate
메시지 번역

**엔드포인트**: `/chat.translate`
**프로토콜**: WebSocket (STOMP)
**응답 목적지**: `/user/queue/translate`

**요청**
```json
{
  "messageId": 10,
  "targetLang": "ko"
}
```
- `messageId` (숫자, 필수)
- `targetLang` (문자열, 필수): ko, en, ja, fr

**응답**
```json
{
  "messageId": 10,
  "translatedMessage": "안녕하세요",
  "originalMessage": null,
  "targetLang": "ko"
}
```

**에러**: T001, T002

---

### POST /api/translate
메시지 번역 (REST API)

**요청**
- Header: `Authorization: Bearer {token}`
- Body:
```json
{
  "messageId": 10,
  "targetLang": "ko"
}
```
- `messageId` (숫자, 필수)
- `targetLang` (문자열, 필수): ko, en, ja, fr

**응답 200**
```json
{
  "messageId": 10,
  "translatedMessage": "안녕하세요",
  "originalMessage": null,
  "targetLang": "ko"
}
```

**에러**: A001, A003, A004, C001, T001, T002

---

## Enum 타입

### Language
- `KO` - 한국어
- `EN` - 영어
- `JA` - 일본어
- `FR` - 프랑스어

### UserAuthProvider
- `GOOGLE` - Google OAuth2
- `GITHUB` - GitHub OAuth2

### WorkspaceUserRole
- `OWNER` - 워크스페이스 소유자
- `MANAGER` - 관리자
- `MEMBER` - 일반 멤버
- `GUEST` - 게스트

### WorkspaceUserState
- `ONLINE` - 온라인
- `AWAY` - 자리비움
- `BUSY` - 다른 용무 중
- `OFFLINE` - 오프라인

### WorkspaceUserNotify
- `ON` - 모든 알림
- `MENTION` - 멘션만
- `OFF` - 알림 끔

### ChannelWorkspaceUserNotify
- `ON` - 모든 알림
- `MENTION` - 멘션만
- `OFF` - 알림 끔

### ChannelType
- `CHAT` - 채팅 채널
- `DM` - 다이렉트 메시지
- `WEBHOOK` - 웹훅 채널
- `ASSISTANT` - AI 어시스턴트 채널

### ChannelPermission
- `NONE` - 접근 불가
- `READ` - 읽기만
- `WRITE` - 읽기/쓰기
- `MANAGE` - 전체 권한

### GroupChannelPermission
- `READ` - 읽기만
- `WRITE` - 읽기/쓰기
- `MANAGE` - 전체 권한

### UserNotifyType
- `SYSTEM` - 시스템 알림
- `INVITE` - 초대 알림
- `MESSAGE` - 메시지 알림
