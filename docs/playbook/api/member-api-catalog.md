# Member Domain - API Catalog

**최종 수정일:** 2026-01-30
**버전:** 2.0
**담당 모듈:** `bc/member`

---

## 개요

Member 도메인의 모든 REST API 엔드포인트를 정의합니다.
이 문서는 프론트엔드, 외부 시스템, 내부 서비스 간 통신을 위한 API 계약서 역할을 합니다.

---

## API 분류

| 분류 | Base Path | 설명 |
|------|-----------|------|
| Public API v1 | `/api/members` | 외부 클라이언트용 레거시 API |
| Public API v2 | `/api/v2/members` | 외부 클라이언트용 RESTful API (신규) |
| Internal API | `/api/internal/members` | 내부 서비스 간 통신용 API |

---

## Public API v1 (Legacy)

### 1. 가입 상태 확인

회원의 가입 여부를 확인합니다.

| 항목 | 값 |
|------|---|
| **Method** | `GET` |
| **Path** | `/api/members/check-registration` |
| **Auth** | Required (Bearer Token) |
| **Deprecated** | No (유지) |

#### Request Headers

| Header | Required | Description |
|--------|:--------:|-------------|
| `Authorization` | ✅ | `Bearer {accessToken}` |

#### Response - 가입된 회원

```json
{
  "id": 1,
  "email": "user@example.com",
  "nickname": "행복한고양이1234",
  "birthday": "1990-01-15",
  "address": "서울시 강남구",
  "phoneNum": "010-1234-5678",
  "name": "홍길동",
  "status": "ACTIVE",
  "role": "BUYER",
  "authSub": "auth0|abc123"
}
```

#### Response - 미가입 사용자

```json
{
  "status": "NOT_REGISTERED"
}
```

#### Error Responses

| Status | Condition |
|--------|-----------|
| `401` | 인증 토큰 누락/유효하지 않음 |

---

### 2. 회원가입

신규 회원을 등록합니다. PreSignup 임시 정보를 기반으로 회원을 생성합니다.

| 항목 | 값 |
|------|---|
| **Method** | `POST` |
| **Path** | `/api/members/signup` |
| **Auth** | Required (Bearer Token) |
| **Content-Type** | `application/json` |

#### Request Body

| Field | Type | Required | Description |
|-------|------|:--------:|-------------|
| `birthday` | `string` (ISO 8601) | ❌ | 생년월일 (예: "1990-01-15") |
| `address` | `string` | ❌ | 배송 주소 |
| `phoneNum` | `string` | ❌ | 전화번호 |

> **참고**: 모든 필드가 Optional입니다. 빈 body `{}`로도 가입 가능합니다.
> 닉네임은 서버에서 "형용사+동물+숫자" 형식으로 자동 생성됩니다.

#### Request Example

```json
{
  "birthday": "1990-01-15",
  "address": "서울시 강남구 테헤란로 123",
  "phoneNum": "010-1234-5678"
}
```

#### Response - 201 Created

```json
{
  "id": 1,
  "email": "user@example.com",
  "nickname": "행복한고양이1234",
  "birthday": "1990-01-15",
  "address": "서울시 강남구 테헤란로 123",
  "phoneNum": "010-1234-5678",
  "name": "홍길동",
  "status": "ACTIVE",
  "role": "BUYER",
  "authSub": "auth0|abc123"
}
```

#### Error Responses

| Status | Code | Condition |
|--------|------|-----------|
| `400` | `M201` | 이미 가입된 회원 (DuplicateMemberException) |
| `401` | - | 인증 토큰 누락/유효하지 않음 |

#### Domain Events Published

- `MemberSignedEvent` - 회원가입 완료 시

---

### 3. 내 정보 조회 (Deprecated)

| 항목 | 값 |
|------|---|
| **Method** | `GET` |
| **Path** | `/api/members/getMyInfo` |
| **Auth** | Required (Bearer Token) |
| **Deprecated** | ⚠️ Yes - Use `GET /api/v2/members/me` instead |

#### Response - 200 OK

```json
{
  "id": 1,
  "email": "user@example.com",
  "nickname": "행복한고양이1234",
  "birthday": "1990-01-15",
  "address": "서울시 강남구",
  "phoneNum": "010-1234-5678",
  "name": "홍길동",
  "status": "ACTIVE",
  "role": "BUYER",
  "authSub": "auth0|abc123"
}
```

#### Error Responses

| Status | Condition |
|--------|-----------|
| `401` | 인증 토큰 누락/유효하지 않음 |
| `404` | 회원을 찾을 수 없음 |

---

### 4. 회원 정보 수정 (Deprecated)

| 항목 | 값 |
|------|---|
| **Method** | `PATCH` |
| **Path** | `/api/members/updateMyInfo` |
| **Auth** | Required (Bearer Token) |
| **Content-Type** | `application/json` |
| **Deprecated** | ⚠️ Yes - Use `PATCH /api/v2/members/me` instead |

#### Request Body

| Field | Type | Required | Description |
|-------|------|:--------:|-------------|
| `password` | `string` | ❌ | 비밀번호 |
| `nickname` | `string` | ❌ | 닉네임 |
| `address` | `string` | ❌ | 주소 |
| `phoneNum` | `string` | ❌ | 전화번호 |
| `name` | `string` | ❌ | 이름 |

#### Domain Events Published

- `MemberUpdatedEvent` - 회원정보 수정 시

---

### 5. 회원 탈퇴

| 항목 | 값 |
|------|---|
| **Method** | `DELETE` |
| **Path** | `/api/members/withdraw` |
| **Auth** | Required (Bearer Token) |

#### Response - 204 No Content

(Empty body)

#### Error Responses

| Status | Condition |
|--------|-----------|
| `401` | 인증 토큰 누락/유효하지 않음 |

---

### 6. 닉네임 중복 확인

| 항목 | 값 |
|------|---|
| **Method** | `GET` |
| **Path** | `/api/members/check/nickname` |
| **Auth** | Not Required |

#### Query Parameters

| Parameter | Type | Required | Description |
|-----------|------|:--------:|-------------|
| `nickname` | `string` | ✅ | 확인할 닉네임 |

#### Response - 200 OK

```json
{
  "status": "AVAILABLE"
}
```

또는

```json
{
  "status": "DUPLICATED"
}
```

#### Error Responses

| Status | Condition |
|--------|-----------|
| `400` | 닉네임이 빈 문자열인 경우 |

---

## Public API v2 (RESTful)

### 1. 내 정보 조회

| 항목 | 값 |
|------|---|
| **Method** | `GET` |
| **Path** | `/api/v2/members/me` |
| **Auth** | Required (Bearer Token) |
| **Version** | v2 (신규) |

#### Response DTO: `MemberResponse`

| Field | Type | Nullable | Description |
|-------|------|:--------:|-------------|
| `id` | `number` | ❌ | 회원 ID |
| `email` | `string` | ❌ | 이메일 |
| `nickname` | `string` | ❌ | 닉네임 |
| `birthday` | `string` | ✅ | 생년월일 (ISO 8601) |
| `address` | `string` | ✅ | 주소 |
| `phoneNum` | `string` | ✅ | 전화번호 |
| `name` | `string` | ✅ | 이름 |
| `status` | `string` | ❌ | 회원 상태 |

> **참고**: v1 API와 달리 `authSub`, `role` 필드가 응답에 포함되지 않습니다.

#### Response Example

```json
{
  "id": 1,
  "email": "user@example.com",
  "nickname": "행복한고양이1234",
  "birthday": "1990-01-15",
  "address": "서울시 강남구",
  "phoneNum": "010-1234-5678",
  "name": "홍길동",
  "status": "ACTIVE"
}
```

#### Error Responses

| Status | Condition |
|--------|-----------|
| `401` | 인증 토큰 누락/유효하지 않음 |
| `404` | 회원을 찾을 수 없음 |

---

### 2. 회원 정보 수정

| 항목 | 값 |
|------|---|
| **Method** | `PATCH` |
| **Path** | `/api/v2/members/me` |
| **Auth** | Required (Bearer Token) |
| **Content-Type** | `application/json` |
| **Version** | v2 (신규) |

#### Request Body

| Field | Type | Required | Description |
|-------|------|:--------:|-------------|
| `password` | `string` | ❌ | 비밀번호 |
| `nickname` | `string` | ❌ | 닉네임 |
| `address` | `string` | ❌ | 주소 |
| `phoneNum` | `string` | ❌ | 전화번호 |
| `name` | `string` | ❌ | 이름 |

> **참고**: Partial Update를 지원합니다. 전송된 필드만 수정됩니다.

#### Request Example

```json
{
  "nickname": "새로운닉네임",
  "address": "부산시 해운대구"
}
```

#### Response - 200 OK

```json
{
  "id": 1,
  "email": "user@example.com",
  "nickname": "새로운닉네임",
  "birthday": "1990-01-15",
  "address": "부산시 해운대구",
  "phoneNum": "010-1234-5678",
  "name": "홍길동",
  "status": "ACTIVE"
}
```

#### Error Responses

| Status | Condition |
|--------|-----------|
| `401` | 인증 토큰 누락/유효하지 않음 |
| `403` | 탈퇴한 회원이 수정 시도 (status = WITHDRAWN) |
| `404` | 회원을 찾을 수 없음 |

#### Domain Events Published

- `MemberUpdatedEvent` - 회원정보 수정 시

---

## Internal API

> **주의**: 내부 서비스 간 통신용 API입니다. 외부 클라이언트에서 직접 호출하면 안 됩니다.

### 1. AuthSub으로 회원 조회

| 항목 | 값 |
|------|---|
| **Method** | `GET` |
| **Path** | `/api/internal/members/by-auth-sub/{authSub}` |
| **Auth** | Internal Service Only |
| **Consumer** | Auth Module, Order Module |

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `authSub` | `string` | Auth0 고유 식별자 (URL 인코딩 필요) |

#### Response DTO: `MemberInfo`

| Field | Type | Description |
|-------|------|-------------|
| `memberId` | `number` | 내부 회원 ID |
| `authSub` | `string` | Auth0 고유 식별자 |
| `role` | `string` | 회원 역할 (BUYER, SELLER, ADMIN) |
| `email` | `string` | 회원 이메일 |
| `nickname` | `string` | 회원 닉네임 |

#### Response Example

```json
{
  "memberId": 1,
  "authSub": "auth0|abc123",
  "role": "BUYER",
  "email": "user@example.com",
  "nickname": "행복한고양이1234"
}
```

#### Error Responses

| Status | Condition |
|--------|-----------|
| `404` | 회원을 찾을 수 없음 |

---

## Enums

### MemberStatus

| Value | Description |
|-------|-------------|
| `ACTIVE` | 정상적으로 서비스를 이용 중인 상태 |
| `INACTIVE` | 임시로 서비스 이용이 제한된 상태 |
| `DORMANT` | 장기간 미접속으로 인해 휴면 전환된 상태 |
| `WITHDRAWN` | 회원 탈퇴가 완료된 상태 |

### MemberRole

| Value | Description |
|-------|-------------|
| `BUYER` | 구매자 |
| `SELLER` | 판매자 |
| `ADMIN` | 관리자 |

---

## Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `M201` | 400 | 이미 가입된 이메일입니다 (DuplicateMemberException) |
| `M404` | 404 | 회원을 찾을 수 없습니다 (MemberNotFoundException) |
| `VALIDATION_ERROR` | 400 | 요청 값 유효성 검사 실패 |

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 2.0 | 2026-01-30 | v2 API 추가 (GET/PATCH /api/v2/members/me), SignupRequest 필드 Optional화, 닉네임 자동생성 |
| 1.0 | 2025-12-01 | 초기 버전 |
