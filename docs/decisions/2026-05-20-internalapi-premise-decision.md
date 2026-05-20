# Plan 0 W4 — InternalApiOnly Premise Decision (2026-05-20)

## 배경

Roadmap T10.11 + MEMORY.md Backlog #13 의 InternalApiOnly API Key 인증 전환 작업. Pre-flight (워크트리 staging/post-deploy-cleanup) 에서 `INTERNAL_SERVICE` / FIXME 가 0건으로 관측되어 *premise stale* 가능성이 보고됨. 본 문서는 광역 grep 재검증 결과와 분기 결정을 기록.

## 광역 grep 결과 (2026-05-20)

| Axis | grep 패턴 | 결과 |
|---|---|---|
| 1 | `INTERNAL_SERVICE\|INTERNAL_API_KEY\|internal-api-key\|internal_api_key` | **4 hits** (문서 3 + 코드 1) |
| 2 | `InternalApiOnly\|@InternalApi\|ApiOnly` | **2 hits** (정의 1 + 사용 1) |
| 3 | `(FIXME\|TODO).*(internal\|api[- ]?key)` | **1 hit** — `support/security/.../InternalApiOnly.java:30` |
| 4 | `X-API-Key\|X-Api-Key\|x-api-key` | **0 hits** (미구현) |

### 발견 코드 위치

```
support/security/src/main/java/app/giftify/security/common/annotation/InternalApiOnly.java:30
  → public @interface InternalApiOnly { // FIXME :: API Key 를 통해 인증하는 방식으로 변경 필요

bc/core/src/main/java/app/giftify/payment/adapter/inbound/web/InternalPaymentController.java
  → @InternalApiOnly 사용 (1 endpoint 또는 그 이상)
```

### 현재 InternalApiOnly 정의 (support/security)

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("hasRole('INTERNAL_SERVICE')")
public @interface InternalApiOnly { // FIXME :: API Key 를 통해 인증하는 방식으로 변경 필요
}
```

→ 임시 구현 (PreAuthorize Role 기반). 실제 `ROLE_INTERNAL_SERVICE` Authorization 부여 메커니즘 부재 → 사실상 *비활성 보호* 상태.

## 판정

- [x] **Case A (premise valid)** — Task 4.1 진행 (InternalApiAuthFilter 구현 + SecurityConfig 등록 + FIXME 제거)
- [ ] Case B (premise stale) — 단일 doc commit 으로 close

## 근거

1. **FIXME 명시 잔존**: `InternalApiOnly.java:30` 의 주석이 *변경 필요* 를 명시. 백로그 #13 의 전제와 정합.
2. **X-API-Key 헤더 미구현**: 실제 인증 메커니즘 부재. PreAuthorize Role 만으로는 *서비스 계정 토큰 발급 메커니즘* 가 별도 필요한데 그것도 부재 → 사실상 인증되지 않은 endpoint.
3. **InternalPaymentController 노출**: bc/core 의 payment internal endpoint 가 *실질 보호 없이* 공개 가능성. 보안 위험 실재.

## 다음 액션 (Task 4.1-4.4)

1. **Task 4.1**: `InternalApiAuthFilter` 단위 테스트 작성 → 실패 확인
2. **Task 4.2**: 필터 구현 — `OncePerRequestFilter` + `MessageDigest.isEqual` 상수시간 비교
3. **Task 4.3**: SecurityConfig 등록 + `application.yml` 시크릿 placeholder
4. **Task 4.4**: prod overlay 시크릿 + FIXME 제거 + Roadmap T10.11 / MEMORY.md #13 close

## 설계 결정

### 1. 어노테이션 vs 필터 — 병행

기존 `@InternalApiOnly` 어노테이션은 *의도 표현 (intent)* 으로 유지. 필터가 *실제 검증* 담당. `@PreAuthorize` 제거하고 javadoc 만 갱신.

```java
// 변경 후 (어노테이션은 marker 역할만)
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InternalApiOnly {
}
```

### 2. URI prefix 매칭 vs 어노테이션 매칭

| 방식 | Pros | Cons |
|---|---|---|
| URI prefix (`/internal/**`) | Filter 단순, 정적 | endpoint 가 prefix 외 위치 가능 |
| 어노테이션 reflection | endpoint flexible | Filter 가 HandlerMethod resolve 필요 (성능) |

권장: **URI prefix** + InternalPaymentController 의 RequestMapping 을 `/internal/payment` 로 정렬. 어노테이션은 *secondary marker* 로 codecov / 검색용.

### 3. 시크릿 관리

- 환경변수 `INTERNAL_API_KEY` (필수)
- 기본값 없음 — 미설정 시 Bean 생성 실패 (보안 fail-safe)
- staging/prod overlay 는 SOPS 암호화 (기존 패턴)

## 관련 파일 (예상 변경 범위)

| 파일 | 변경 |
|---|---|
| `support/security/.../InternalApiOnly.java` | @PreAuthorize 제거 + FIXME 제거 + javadoc 갱신 |
| `bootstrap/api-server/.../api/security/InternalApiAuthFilter.java` | **신규** |
| `bootstrap/api-server/.../api/security/InternalApiAuthFilterTest.java` | **신규** (test) |
| SecurityConfig (위치 grep 필요) | filter 등록 |
| `bootstrap/api-server/src/main/resources/application.yml` | `giftify.security.internal-api-key` 추가 |
| `k8s/overlays/{staging,prod}/secrets/` | SOPS 시크릿 |
| `bc/core/.../InternalPaymentController.java` | URI prefix `/internal/...` 확인 |

= 약 7 파일 변경 예상. 단일 PR cherry-pick 적합 크기.
