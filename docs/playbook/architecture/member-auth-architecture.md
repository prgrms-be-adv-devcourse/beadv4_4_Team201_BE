# Member & Auth Module Architecture Specification

## Overview

Giftify의 인증(Authentication) 및 인가(Authorization) 아키텍처에 대한 기술 명세서입니다.

**관련 문서:**

- [Auth API Catalog](../api/auth-api-catalog.md)
- [Member API Catalog](../api/member-api-catalog.md)
- [Member & Auth Event Catalog](../event/member-auth-event-catalog.md)

---

## 1. Architecture Summary

```
+-------------------+     +-------------------+     +-------------------+
|                   |     |                   |     |                   |
|   Auth0 (IdP)     |     |   Giftify BE      |     |   PostgreSQL      |
|   - 인증 담당     |     |   - 인가 담당     |     |   - Member 저장   |
|   - JWT 발급      |     |   - Role 관리     |     |   - Role 저장     |
|                   |     |                   |     |                   |
+-------------------+     +-------------------+     +-------------------+
```

| 책임 영역                   | 담당 시스템          | 설명                |
|-------------------------|-----------------|-------------------|
| **Authentication (인증)** | Auth0           | 사용자 신원 확인, JWT 발급 |
| **Authorization (인가)**  | Giftify BE + DB | 역할(Role) 기반 권한 관리 |
| **Session Management**  | Auth0           | 토큰 갱신, 만료 관리      |

---

## 2. Authentication Strategy

### 2.1 Token Strategy: Auth0 Token Direct Use

Giftify는 **자체 JWT를 발급하지 않고** Auth0가 발급한 토큰을 그대로 사용합니다.

```
┌──────────────────────────────────────────────────────────────────────────┐
│                        Token Flow                                        │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Frontend (SPA)              Giftify BE                   Auth0          │
│       │                          │                          │            │
│       │  1. Auth0 SDK Login      │                          │            │
│       │ ─────────────────────────────────────────────────▶  │            │
│       │                          │                          │            │
│       │  2. id_token, access_token 발급                     │            │
│       │ ◀───────────────────────────────────────────────────│            │
│       │                          │                          │            │
│       │  3. POST /api/auth/login │                          │            │
│       │     { idToken }          │                          │            │
│       │ ────────────────────────▶│                          │            │
│       │                          │                          │            │
│       │  4. LoginResponse        │                          │            │
│       │     (isNewUser, member)  │                          │            │
│       │ ◀────────────────────────│                          │            │
│       │                          │                          │            │
│       │  5. API Request          │                          │            │
│       │     Authorization:       │                          │            │
│       │     Bearer {access_token}│                          │            │
│       │ ────────────────────────▶│                          │            │
│       │                          │                          │            │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

### 2.2 Why Not Issue Custom JWT?

| 방식                      | 장점                     | 단점                    |
|-------------------------|------------------------|-----------------------|
| **Auth0 토큰 직접 사용 (현재)** | 구현 단순, 토큰 관리 위임        | 매 요청시 DB에서 role 조회 필요 |
| Giftify 자체 JWT 발급       | role을 토큰에 포함, DB 조회 감소 | 토큰 관리 복잡도 증가          |

**현재 선택 이유:**

1. Auth0에 토큰 생명주기 관리 위임으로 보안 강화
2. 구현 복잡도 감소
3. 추후 캐싱으로 DB 조회 부하 해결 가능

---

## 3. Authorization Strategy

### 3.1 Role Management

역할(Role)은 **Giftify 내부 DB에서 관리**합니다. Auth0에는 역할 정보가 없습니다.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        Role Resolution Flow                             │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  1. Request with Auth0 access_token                                     │
│     Authorization: Bearer eyJhbGciOiJSUzI1NiI...                        │
│                          │                                              │
│                          ▼                                              │
│  2. JwtDecoder validates token, extracts 'sub' claim                    │
│     sub = "google-oauth2|104844495450678108304"                         │
│                          │                                              │
│                          ▼                                              │
│  3. Query Member table by authSub                                       │
│     SELECT * FROM member WHERE auth_sub = ?                             │
│                          │                                              │
│                          ▼                                              │
│  4. Get role from Member entity                                         │
│     role = BUYER | SELLER                                               │
│                          │                                              │
│                          ▼                                              │
│  5. Authorize based on role                                             │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 3.2 Role Types

```java
public enum MemberRole {
    BUYER,   // 구매자 - 펀딩 참여, 선물 구매
    SELLER   // 판매자 - 상품 등록, 판매 관리
}
```

### 3.3 Per-Request DB Query

**현재 구현:**

- 매 API 요청마다 `authSub`로 Member 테이블 조회
- `@AuthenticatedMember` 어노테이션으로 authSub 주입

```java
@GetMapping("/me")
public ResponseEntity<MemberResponse> getMe(
    @AuthenticatedMember String authSub  // JWT의 sub 클레임에서 추출
) {
    Member member = getMemberUseCase.getMemberByAuthSub(authSub)  // DB 조회
        .orElseThrow(() -> new MemberNotFoundException(authSub));
    return ResponseEntity.ok(MemberResponse.from(member));
}
```

### 3.4 Performance Considerations

| 현재 상태       | 최적화 방안                                |
|-------------|---------------------------------------|
| 매 요청시 DB 조회 | 1. Redis 캐싱 (`authSub -> MemberInfo`) |
|             | 2. Spring Cache 적용                    |
|             | 3. 자체 JWT 발급으로 전환 (role 클레임 포함)       |

**권장 캐시 전략 (추후 적용 시):**

```
Cache Key: "member:authSub:{authSub}"
TTL: 5분 (role 변경 반영 지연 허용 범위)
Eviction: Member 정보 변경 시 invalidate
```

---

## 4. Module Structure

### 4.1 Package Layout

```
bc/member/
├── adapter/
│   ├── in/web/
│   │   ├── MemberController.java       # 레거시 API
│   │   └── MemberV2Controller.java     # RESTful API (v2)
│   └── out/jpa/
│       ├── entity/MemberJpaEntity.java
│       └── repository/MemberJpaRepository.java
├── application/
│   ├── port/in/
│   │   ├── GetMemberUseCase.java
│   │   ├── UpdateMemberUseCase.java
│   │   └── SignupUseCase.java
│   └── service/MemberService.java
└── domain/
    └── member/
        ├── Member.java
        ├── MemberStatus.java
        └── RandomNicknameGenerator.java

bc/member/  (auth 서브모듈)
├── auth/
│   ├── adapter/inbound/web/
│   │   ├── AuthController.java
│   │   └── dto/
│   │       ├── LoginRequest.java
│   │       └── LoginResponse.java
│   ├── application/
│   │   ├── AuthService.java
│   │   ├── LoginService.java
│   │   └── inbound/LoginUseCase.java
│   └── support/config/
│       └── SecurityConfig.java
```

### 4.2 Key Components

| 컴포넌트                   | 책임                             |
|------------------------|--------------------------------|
| `AuthService`          | Auth0 토큰 검증/갱신, OIDC 사용자 로드    |
| `LoginService`         | 로그인 처리, 신규/기존 사용자 판별           |
| `MemberService`        | 회원 CRUD, 닉네임 자동생성              |
| `SecurityConfig`       | Spring Security 설정, JWT 디코더 구성 |
| `@AuthenticatedMember` | 커스텀 어노테이션, JWT에서 authSub 추출    |

---

## 5. Authentication Flows

### 5.1 Login Flow (SPA SDK Pattern)

```
┌────────────┐     ┌────────────┐     ┌────────────┐     ┌────────────┐
│  Frontend  │     │   Auth0    │     │ Giftify BE │     │  Database  │
└─────┬──────┘     └─────┬──────┘     └─────┬──────┘     └─────┬──────┘
      │                  │                  │                  │
      │  1. loginWithRedirect()             │                  │
      │ ────────────────▶│                  │                  │
      │                  │                  │                  │
      │  2. Auth0 로그인 화면               │                  │
      │ ◀────────────────│                  │                  │
      │                  │                  │                  │
      │  3. 사용자 인증 (ID/PW, Social)     │                  │
      │ ────────────────▶│                  │                  │
      │                  │                  │                  │
      │  4. Callback (id_token, access_token)                  │
      │ ◀────────────────│                  │                  │
      │                  │                  │                  │
      │  5. POST /api/auth/login            │                  │
      │     { idToken }                     │                  │
      │ ────────────────────────────────────▶                  │
      │                  │                  │                  │
      │                  │  6. Validate     │                  │
      │                  │     idToken      │                  │
      │                  │ ◀────────────────│                  │
      │                  │                  │                  │
      │                  │                  │  7. Find by      │
      │                  │                  │     authSub      │
      │                  │                  │ ────────────────▶│
      │                  │                  │                  │
      │                  │                  │  8. Member or    │
      │                  │                  │     null         │
      │                  │                  │ ◀────────────────│
      │                  │                  │                  │
      │  9. LoginResponse                   │                  │
      │     { isNewUser, member }           │                  │
      │ ◀────────────────────────────────────                  │
      │                  │                  │                  │
```

### 5.2 Member Auto-Creation Flow (via Event)

로그인 시 `UserAuthenticatedEvent`를 통해 회원이 자동 생성됩니다.

```
┌────────────┐     ┌────────────┐     ┌────────────┐     ┌────────────┐
│  Frontend  │     │LoginService│     │ Event Bus  │     │EventListener│
└─────┬──────┘     └─────┬──────┘     └─────┬──────┘     └─────┬──────┘
      │                  │                  │                  │
      │ POST /login      │                  │                  │
      │ {idToken}        │                  │                  │
      │─────────────────▶│                  │                  │
      │                  │                  │                  │
      │                  │ (신규 사용자일 때)│                  │
      │                  │ UserAuthenticated│                  │
      │                  │ Event 발행       │                  │
      │                  │─────────────────▶│                  │
      │                  │                  │                  │
      │                  │                  │ Event 전달       │
      │                  │                  │─────────────────▶│
      │                  │                  │                  │
      │                  │                  │     ┌────────────┴───────────┐
      │                  │                  │     │ 1. existsByEmail 체크  │
      │                  │                  │     │ 2. Member 자동 생성     │
      │                  │                  │     │    (닉네임 자동생성)     │
      │                  │                  │     └────────────┬───────────┘
      │                  │                  │                  │
      │ {isNewUser:true} │                  │                  │
      │◀─────────────────│                  │                  │
      │                  │                  │                  │
```

> **참고**: 기존 PreSignup 기반 가입 플로우는 제거되었습니다.
> 회원은 로그인 시 이벤트를 통해 자동 생성되며, `/api/members/signup` 호출 시
> 이미 회원이 존재하면 409 Conflict가 반환됩니다.

### 5.3 API Request Authentication

```
┌────────────┐                           ┌────────────┐     ┌────────────┐
│  Frontend  │                           │ Giftify BE │     │  Database  │
└─────┬──────┘                           └─────┬──────┘     └─────┬──────┘
      │                                        │                  │
      │  1. GET /api/v2/members/me             │                  │
      │     Authorization: Bearer {token}      │                  │
      │ ──────────────────────────────────────▶│                  │
      │                                        │                  │
      │               2. JwtDecoder validates token               │
      │                  (signature, issuer, expiry)              │
      │                                        │                  │
      │               3. Extract sub claim     │                  │
      │                  "google-oauth2|..."   │                  │
      │                                        │                  │
      │               4. @AuthenticatedMember  │                  │
      │                  injects authSub       │                  │
      │                                        │                  │
      │                                        │  5. SELECT *     │
      │                                        │     FROM member  │
      │                                        │     WHERE        │
      │                                        │     auth_sub = ? │
      │                                        │ ────────────────▶│
      │                                        │                  │
      │                                        │  6. Member       │
      │                                        │ ◀────────────────│
      │                                        │                  │
      │  7. 200 OK                             │                  │
      │     { id, email, role, ... }           │                  │
      │ ◀──────────────────────────────────────│                  │
      │                                        │                  │
```

---

## 6. Security Configuration

### 6.1 Dual SecurityFilterChain

공개 엔드포인트와 인증 필요 엔드포인트를 분리하여 처리합니다.

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 공개 엔드포인트용 필터 체인 (JWT 검증 없음)
    @Bean
    @Order(1)
    public SecurityFilterChain publicSecurityFilterChain(HttpSecurity http) {
        http
            .securityMatcher(
                "/api/v2/auth/login",   // 로그인 엔드포인트
                "/api/v2/home",          // 홈 API (공개)
                "/actuator/health"       // 헬스체크
            )
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    // 인증 필요 엔드포인트용 필터 체인 (JWT 검증)
    @Bean
    @Order(2)
    public SecurityFilterChain authSecurityFilterChain(HttpSecurity http) {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .addFilterAfter(memberPrincipalFilter, BearerTokenAuthenticationFilter.class);
        return http.build();
    }
}
```

### 6.2 Dual JwtDecoder

id_token과 access_token의 audience가 다르므로 별도의 JwtDecoder를 사용합니다.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                       Token Type & Decoder Mapping                       │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Token Type      Audience                   Decoder        Usage         │
│  ──────────────────────────────────────────────────────────────────────  │
│  id_token        Auth0 Client ID            idTokenDecoder  /login       │
│                  (예: qpuevxs5...)                                       │
│                                                                          │
│  access_token    API Identifier             jwtDecoder      /api/*       │
│                  (예: https://api.giftify.app)              (Primary)    │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

```java
@AutoConfiguration
public class SharedSecurityAutoConfiguration {

    // Access Token용 (API 호출 인가)
    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = JwtDecoders.fromIssuerLocation(issuer);
        decoder.setJwtValidator(new AudienceValidator(apiAudience));  // API identifier
        return decoder;
    }

    // ID Token용 (로그인 검증)
    @Bean
    public JwtDecoder idTokenDecoder() {
        NimbusJwtDecoder decoder = JwtDecoders.fromIssuerLocation(issuer);
        decoder.setJwtValidator(new AudienceValidator(clientId));  // Auth0 Client ID
        return decoder;
    }
}
```

### 6.3 JWT Validation

| 검증 항목      | 설명                                  |
|------------|-------------------------------------|
| Signature  | Auth0 JWKS로 서명 검증                   |
| Issuer     | `https://{tenant}.auth0.com/` 일치 확인 |
| Audience   | Token 타입에 따라 다름 (위 표 참조)           |
| Expiration | 토큰 만료 시간 확인                         |

---

## 7. Data Model

### 7.1 Member Entity

```
┌─────────────────────────────────────────────────────────────┐
│                        Member                               │
├─────────────────────────────────────────────────────────────┤
│ id            : Long (PK)                                   │
│ email         : String (Unique, Not Null)                   │
│ nickname      : String (Unique, Auto-generated if null)     │
│ name          : String (Nullable)                           │
│ birthday      : LocalDate (Nullable)                        │
│ address       : String (Nullable)                           │
│ phoneNum      : String (Nullable)                           │
│ role          : MemberRole (BUYER | SELLER)                 │
│ status        : MemberStatus (ACTIVE | WITHDRAWN)           │
│ authSub       : String (Unique, Not Null) ◀── Auth0 sub     │
│ createdAt     : LocalDateTime                               │
│ updatedAt     : LocalDateTime                               │
└─────────────────────────────────────────────────────────────┘
```

### 7.2 Auth0 ↔ Member Mapping

```
Auth0 User                          Giftify Member
┌──────────────────┐                ┌──────────────────┐
│ sub ─────────────────────────────▶│ authSub          │
│ email ───────────────────────────▶│ email            │
│ name ────────────────────────────▶│ name             │
│ nickname ─────────(참고용)────────▶│ nickname         │
│                  │                │                  │
│ (역할 정보 없음)  │                │ role: BUYER     │
│                  │                │ status: ACTIVE   │
│                  │                │ address, phone.. │
└──────────────────┘                └──────────────────┘
```

---

## 8. Error Handling

### 8.1 Authentication Errors

| HTTP Status | Error Code    | 상황                   |
|-------------|---------------|----------------------|
| 401         | INVALID_TOKEN | JWT 검증 실패 (서명, 만료 등) |
| 401         | TOKEN_EXPIRED | 토큰 만료                |
| 401         | MISSING_TOKEN | Authorization 헤더 없음  |

### 8.2 Member Errors

| HTTP Status | Error Code            | 상황                  |
|-------------|-----------------------|---------------------|
| 404         | MEMBER_NOT_FOUND      | authSub에 해당하는 회원 없음 |
| 403         | MEMBER_WITHDRAWN      | 탈퇴한 회원의 수정 요청       |
| 409         | MEMBER_ALREADY_EXISTS | 이미 가입된 사용자          |

---

## 9. Future Improvements

### 9.1 Short-term (캐싱)

```
┌─────────────────────────────────────────────────────────────┐
│  Redis Cache Layer                                          │
│                                                             │
│  Key: "member:{authSub}"                                    │
│  Value: { id, role, status, ... }                           │
│  TTL: 5 minutes                                             │
│                                                             │
│  Benefits:                                                  │
│  - DB 조회 횟수 대폭 감소                                   │
│  - 응답 시간 개선                                           │
│  - 기존 아키텍처 유지                                       │
└─────────────────────────────────────────────────────────────┘
```

### 9.2 Long-term (자체 JWT 발급)

```
┌─────────────────────────────────────────────────────────────┐
│  Giftify JWT Claims                                         │
│                                                             │
│  {                                                          │
│    "sub": "google-oauth2|104844...",                        │
│    "memberId": 1,                                           │
│    "role": "BUYER",                                         │
│    "email": "user@example.com",                             │
│    "exp": 1706700000                                        │
│  }                                                          │
│                                                             │
│  Benefits:                                                  │
│  - role이 토큰에 포함 → DB 조회 불필요                      │
│  - 완전한 stateless 인증                                    │
│                                                             │
│  Trade-offs:                                                │
│  - 토큰 관리 복잡도 증가                                    │
│  - role 변경 시 토큰 재발급 필요                            │
└─────────────────────────────────────────────────────────────┘
```

---

## 10. Related Documents

| 문서                                                   | 설명                  |
|------------------------------------------------------|---------------------|
| [Auth API Catalog](../api/auth-api-catalog.md)       | Auth 모듈 API 상세 명세   |
| [Member API Catalog](../api/member-api-catalog.md)   | Member 모듈 API 상세 명세 |
| [Event Catalog](../event/member-auth-event-catalog.md) | 도메인 이벤트 명세          |

---

**Document Version:** 2.0
**Last Updated:** 2026-01-31
**Author:** Giftify Backend Team
