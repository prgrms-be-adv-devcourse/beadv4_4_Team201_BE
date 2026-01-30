# Auth Domain - API Catalog

**최종 수정일:** 2026-01-30
**버전:** 2.0
**담당 모듈:** `bc/member` (auth 논리 모듈)

---

## 개요

Auth 도메인의 인증 관련 REST API 엔드포인트를 정의합니다.
Auth0 기반 OAuth2/OIDC 인증 플로우와 SPA SDK 기반 인증을 모두 지원합니다.

---

## 인증 방식

| 방식                   | 사용 시나리오      | 관련 API                                  |
|----------------------|--------------|-----------------------------------------|
| **SPA SDK (BFF 패턴)** | 프론트엔드 SPA 앱  | `POST /api/auth/login`                  |
| **OAuth2 Redirect**  | 서버 사이드 렌더링 앱 | `GET /api/auth/login` → OAuth2 Flow     |
| **JWT Bearer**       | API 호출 시 인증  | Header: `Authorization: Bearer {token}` |

---

## API Endpoints

### 1. SPA SDK 로그인 (신규)

Auth0 SPA SDK에서 발급받은 idToken을 검증하고, 회원 정보와 가입 여부를 반환합니다.

| 항목               | 값                           |
|------------------|-----------------------------|
| **Method**       | `POST`                      |
| **Path**         | `/api/auth/login`           |
| **Auth**         | Not Required (idToken으로 인증) |
| **Content-Type** | `application/json`          |
| **Version**      | v2 (신규)                     |

#### Request Body

| Field     | Type     | Required | Description                   |
|-----------|----------|:--------:|-------------------------------|
| `idToken` | `string` |    ✅     | Auth0 SPA SDK에서 발급받은 ID Token |

#### Request Example

```json
{
  "idToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhdXRoMHxhYmMxMjMiLCJlbWFpbCI6InVzZXJAZXhhbXBsZS5jb20iLCJuYW1lIjoi7ZmN6ri464-ZIiwibmlja25hbWUiOiJ1c2VyMTIzIiwiaWF0IjoxNjE2MjM5MDIyfQ..."
}
```

#### Response DTO: `LoginResponse`

| Field       | Type         | Nullable | Description                 |
|-------------|--------------|:--------:|-----------------------------|
| `isNewUser` | `boolean`    |    ❌     | 신규 사용자 여부 (`true`: 온보딩 필요)  |
| `authSub`   | `string`     |    ❌     | Auth0 고유 식별자                |
| `email`     | `string`     |    ❌     | 사용자 이메일 (JWT 클레임에서 추출)      |
| `name`      | `string`     |    ❌     | 사용자 이름/닉네임                  |
| `member`    | `MemberInfo` |    ✅     | 기존 회원인 경우 회원 정보, 신규면 `null` |

#### Response - 기존 회원

```json
{
  "isNewUser": false,
  "authSub": "auth0|abc123",
  "email": "user@example.com",
  "name": "기존닉네임",
  "member": {
    "memberId": 1,
    "authSub": "auth0|abc123",
    "role": "BUYER",
    "email": "user@example.com",
    "nickname": "기존닉네임"
  }
}
```

#### Response - 신규 사용자

```json
{
  "isNewUser": true,
  "authSub": "auth0|xyz789",
  "email": "newuser@example.com",
  "name": "홍길동",
  "member": null
}
```

#### member 객체 구조 (MemberInfo)

| Field      | Type     | Description                       |
|------------|----------|-----------------------------------|
| `memberId` | `number` | 내부 회원 ID (DB PK)                  |
| `authSub`  | `string` | Auth0 고유 식별자                      |
| `role`     | `string` | 회원 역할: `BUYER`, `SELLER`, `ADMIN` |
| `email`    | `string` | 회원 이메일                            |
| `nickname` | `string` | 회원 닉네임                            |

#### Error Responses

| Status | Condition                      |
|--------|--------------------------------|
| `400`  | idToken 누락 (Validation Error)  |
| `401`  | 유효하지 않은 idToken (만료, 서명 불일치 등) |

#### Domain Events Published

- `UserAuthenticatedEvent` - 신규 사용자인 경우 (PreSignup 생성 트리거)

---

### 2. OAuth2 리다이렉트 로그인 (Legacy)

Auth0 로그인 페이지로 리다이렉트합니다. 서버 사이드 렌더링 애플리케이션용입니다.

| 항목           | 값                                             |
|--------------|-----------------------------------------------|
| **Method**   | `GET`                                         |
| **Path**     | `/api/auth/login`                             |
| **Auth**     | Not Required                                  |
| **Response** | 302 Redirect to `/oauth2/authorization/auth0` |

#### Flow

```
Client                    Server                     Auth0
  │                          │                          │
  │  GET /api/auth/login     │                          │
  │─────────────────────────▶│                          │
  │                          │                          │
  │  302 Redirect            │                          │
  │◀─────────────────────────│                          │
  │  Location: /oauth2/...   │                          │
  │                          │                          │
  │  Follow Redirect         │                          │
  │─────────────────────────────────────────────────────▶│
  │                          │                          │
  │                     OAuth2 Authorization Flow       │
  │◀────────────────────────────────────────────────────│
```

---

### 3. 로그인 성공 콜백

OAuth2 로그인 성공 후 호출되는 엔드포인트입니다. Access Token을 반환합니다.

| 항목         | 값                         |
|------------|---------------------------|
| **Method** | `GET`                     |
| **Path**   | `/api/auth/login-success` |
| **Auth**   | OAuth2 Session Required   |

#### Response - 200 OK

```json
{
  "message": "로그인 성공! 환영합니다, 홍길동님.",
  "user": {
    "sub": "auth0|abc123",
    "email": "user@example.com",
    "name": "홍길동",
    "nickname": "user123",
    "picture": "https://..."
  },
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### Error Responses

| Status | Condition          |
|--------|--------------------|
| `401`  | 로그인이 필요합니다 (세션 없음) |

---

### 4. 내 정보 조회 (JWT 검증)

JWT 토큰의 유효성을 검증하고 클레임 정보를 반환합니다.

| 항목         | 값                       |
|------------|-------------------------|
| **Method** | `GET`                   |
| **Path**   | `/api/auth/me`          |
| **Auth**   | Required (Bearer Token) |

#### Response - 200 OK

```json
{
  "sub": "auth0|abc123",
  "iss": "https://your-tenant.auth0.com/",
  "aud": ["your-api-identifier"],
  "iat": 1616239022,
  "exp": 1616325422,
  "azp": "your-client-id",
  "scope": "openid profile email"
}
```

#### Error Responses

| Status | Condition     |
|--------|---------------|
| `401`  | 유효하지 않은 토큰입니다 |

---

### 5. 토큰 갱신

Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.

| 항목         | 값                   |
|------------|---------------------|
| **Method** | `GET`               |
| **Path**   | `/api/auth/refresh` |
| **Auth**   | Not Required        |

#### Query Parameters

| Parameter | Type     | Required | Description   |
|-----------|----------|:--------:|---------------|
| `token`   | `string` |    ✅     | Refresh Token |

#### Response - 200 OK

```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 86400
}
```

---

### 6. 로그아웃

> **참고**: 로그아웃은 Spring Security 설정에서 처리됩니다.

| 항목          | 값                                  |
|-------------|------------------------------------|
| **Method**  | `POST` / `GET`                     |
| **Path**    | `/api/auth/logout`                 |
| **Auth**    | Required (Session or Bearer Token) |
| **Handler** | Spring Security (SecurityConfig)   |

---

### 7. Public 페이지

인증 없이 접근 가능한 테스트 엔드포인트입니다.

| 항목         | 값            |
|------------|--------------|
| **Method** | `GET`        |
| **Path**   | `/api/auth/` |
| **Auth**   | Not Required |

#### Response - 200 OK

```text
아무나 접근 가능한 페이지 입니다.
```

---

## Authentication Flow Diagrams

### SPA SDK 인증 플로우 (권장)

```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│  Client  │     │  Auth0   │     │  Backend │     │    DB    │
│  (SPA)   │     │          │     │          │     │          │
└────┬─────┘     └────┬─────┘     └────┬─────┘     └────┬─────┘
     │                │                │                │
     │  1. Login UI   │                │                │
     │───────────────▶│                │                │
     │                │                │                │
     │  2. idToken    │                │                │
     │◀───────────────│                │                │
     │                │                │                │
     │  3. POST /api/auth/login        │                │
     │  { idToken }   │                │                │
     │────────────────────────────────▶│                │
     │                │                │                │
     │                │                │  4. Verify JWT │
     │                │                │───────────────▶│
     │                │                │                │
     │                │                │  5. Lookup     │
     │                │                │  Member        │
     │                │                │───────────────▶│
     │                │                │                │
     │  6. LoginResponse               │                │
     │  { isNewUser, member }          │                │
     │◀────────────────────────────────│                │
     │                │                │                │
     ▼                ▼                ▼                ▼
```

### 신규 사용자 온보딩 플로우

```
                        isNewUser?
                            │
              ┌─────────────┴─────────────┐
              │                           │
            true                        false
              │                           │
              ▼                           ▼
    ┌─────────────────┐         ┌─────────────────┐
    │ UserAuthenticated│         │   홈 화면으로    │
    │ Event 발행       │         │   바로 이동      │
    └────────┬────────┘         └─────────────────┘
             │
             ▼
    ┌─────────────────┐
    │ PreSignup 생성   │
    │ (임시 정보 저장)  │
    └────────┬────────┘
             │
             ▼
    ┌─────────────────┐
    │ 온보딩 화면 표시  │
    │ (추가 정보 입력)  │
    └────────┬────────┘
             │
             ▼
    ┌─────────────────┐
    │ POST /api/      │
    │ members/signup  │
    │ { ... }         │
    └────────┬────────┘
             │
             ▼
    ┌─────────────────┐
    │ 회원가입 완료!   │
    │ MemberSigned    │
    │ Event 발행      │
    └─────────────────┘
```

---

## JWT Token Structure

### ID Token Claims (Auth0)

| Claim      | Description            |
|------------|------------------------|
| `sub`      | Auth0 고유 식별자 (authSub) |
| `email`    | 사용자 이메일                |
| `name`     | 사용자 이름                 |
| `nickname` | 사용자 닉네임                |
| `picture`  | 프로필 이미지 URL            |
| `iat`      | 토큰 발급 시간               |
| `exp`      | 토큰 만료 시간               |

### Access Token Usage

```http
GET /api/v2/members/me
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## Error Handling

| Status             | Description      | Action         |
|--------------------|------------------|----------------|
| `401 Unauthorized` | 토큰 없음 또는 유효하지 않음 | 로그인 페이지로 리다이렉트 |
| `403 Forbidden`    | 권한 부족            | 권한 요청 또는 에러 표시 |

---

## Version History

| Version | Date       | Changes                                     |
|---------|------------|---------------------------------------------|
| 2.0     | 2026-01-30 | SPA SDK 로그인 API 추가 (`POST /api/auth/login`) |
| 1.0     | 2025-12-01 | 초기 버전 (OAuth2 Redirect 방식)                  |
