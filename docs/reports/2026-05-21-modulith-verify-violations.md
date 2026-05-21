# Modulith verify() 위반 분석 보고서

**날짜**: 2026-05-21
**작성 컨텍스트**: 모듈 평탄화 (Group C/D/E/F 완료) 후 Task 17 (ModularityTest verify() 활성화) 시도
**상태**: 위반 297건 발견. 별도 후속 task 로 분리 (plan Task 17 Step 3 지침 따름).

---

## 1. 시도 결과

```
./gradlew :bootstrap:api-server:test --tests ModularityTest
```

`ModularityTest.verifyModuleBoundaries` → FAILED.
`org.springframework.modulith.core.Violations` 발생, unique 위반 라인 297건.
`writeDocumentationSnippets` 는 PASS (PlantUML 생성 정상).

→ 단순 `@Disabled` 제거로는 통과 불가. plan Task 17 Step 3 의 "큰 변경이 필요하면 별 task 추가" 지침에 따라 후속 task 로 분리.

---

## 2. 위반 집계

### 2.1 위반 *대상* 패키지 (피의존)

| 대상 모듈 | 위반 건수 | 비고 |
|---|---:|---|
| shared | 4,840 | Money, FundingStatus, PaymentMethod, RsData, Page, DomainEvent 등 -- 거의 모든 BC 가 의존 |
| security | 190 | CurrentMemberId, InternalApiOnly |
| support | 168 | BaseJpaEntity, QBaseJpaEntity, EventIdempotent |
| product | 146 | Product domain class |
| order | 112 | OrderService, OrderSnapshot, OrderItemSnapshot |
| wishlist | 70 | WishlistItem |
| wallet | 58 | DeductWalletUseCase 등 |
| member | 26 | MemberRepositoryPort, Member |
| auth | 22 | TokenBlacklistService, Filter 들 |
| payment | 16 | CreatePaymentService 등 |

### 2.2 위반 *주체* 모듈 (의존하는 측)

| 소스 모듈 | 위반 건수 |
|---|---:|
| order | 1,042 |
| payment | 954 |
| funding | 786 |
| wallet | 524 |
| wishlist | 392 |
| product | 376 |
| settlement | 360 |
| cart | 354 |
| notification | 268 |
| member | 176 |
| friendship | 140 |
| usecase | 78 |
| auth | 68 |
| support | 52 |
| security | 38 |
| loadtest | 30 |
| image | 10 |

**총 17개 모듈** 에서 위반 발생.

---

## 3. 위반 분류

### 3.1 NamedInterface 누락 (대다수)

Spring Modulith 의 기본 규칙: 모듈 root package (`app.giftify.<module>.*`) 의 *직속 public 타입만* 다른 모듈에서 import 가능. sub-package (`app.giftify.<module>.application.*`, `<module>.domain.*` 등) 타입을 다른 모듈이 import 하면 위반.

해결: 각 모듈에 노출할 sub-package 의 `package-info.java` 에 `@NamedInterface("api")` 선언, 또는 모듈 root 의 `@ApplicationModule(allowedDependencies = {"target::api"})` 로 명시.

예시 위반:
```
Module 'funding' depends on non-exposed type app.giftify.order.application.OrderService
Module 'funding' depends on non-exposed type app.giftify.order.domain.OrderSnapshot
```

해결안:
```java
// bc/order/src/main/java/app/giftify/order/application/package-info.java
@org.springframework.modulith.NamedInterface("api")
package app.giftify.order.application;

// bc/funding/src/main/java/app/giftify/funding/package-info.java
@ApplicationModule(allowedDependencies = { "order::api", "shared", "security", "support" })
```

### 3.2 @ApplicationModule 자체 누락

현재 package-info.java 가 있는 모듈은 wishlist, cart, image, product 4개뿐. 나머지 13개 (order, payment, wallet, funding, settlement, notification, member, auth, friendship, security, support, usecase, loadtest) 는 `@ApplicationModule` 선언이 없어 Modulith 가 _패키지 first-depth_ 만으로 자동 인식 → friendship/loadtest/usecase 같은 _BC 가 아닌 모듈명_ 이 verify 결과에 등장하는 원인.

### 3.3 shared 모듈의 평탄도

shared 모듈에 대한 위반이 4,840건으로 압도적 -- `Money`, `FundingStatus`, `PaymentMethod`, `RsData`, `Page`, `DomainEvent`, `EventPublisher` 등 _공유 vo/type/api/event_ 가 sub-package 에 깊이 분산 (`shared.domain.vo.*`, `shared.domain.type.*`, `shared.api.paging.*`, `shared.domain.event.*`).

해결안: shared 자체에는 `@ApplicationModule(type = OPEN)` 적용. shared 는 공유 라이브러리 성격이므로 OPEN 모듈로 두면 sub-package 노출이 자유로움.

---

## 4. 후속 작업 계획

본 위반 해결은 *3개 단위 작업* 으로 분리하는 것이 cherry-pick 친화적:

### Task 17a: 모듈 root package-info.java 일괄 도입

- 대상 13개 모듈에 `package-info.java` 신설 + `@ApplicationModule` 선언
- `allowedDependencies` 는 빈 배열로 시작 (verify() 가 알려주는 대로 채움)
- shared 는 `@ApplicationModule(type = OPEN)` 적용
- 단일 commit, 13~14개 신규 파일

### Task 17b: NamedInterface 도입 (모듈별 API 면 명시)

- 노출할 sub-package 별로 `package-info.java` 추가, `@NamedInterface("api")` 선언
- 대상 sub-package (위반 기준):
  - `order.application`, `order.domain` (OrderService / OrderSnapshot 노출)
  - `payment.application`, `payment.application.inbound`, `payment.domain` (CreatePaymentService 등 노출)
  - `wallet.application.inbound`, `wallet.domain.event` (DeductWalletUseCase, WalletDeductedEvent 노출)
  - `product.domain` (Product 노출)
  - `wishlist.core.domain` (WishlistItem 노출)
  - `member.application.port.out`, `member.domain.member` (MemberRepositoryPort, Member 노출)
  - `auth.application`, `auth.support.filter` (TokenBlacklistService, Filter 노출)
  - `security.common`, `security.common.annotation` (CurrentMemberId, InternalApiOnly 노출)
  - `support.jpa` (BaseJpaEntity 노출)
- 단일 commit, 약 15~20개 신규 package-info.java

### Task 17c: shared sub-package 노출 또는 OPEN 모듈 전환

- 선택 A: shared 자체를 `@ApplicationModule(type = OPEN)` 으로 (가장 빠른 해결)
- 선택 B: shared 의 모든 sub-package 에 `@NamedInterface` 각각 부여 (엄격한 경계)
- 권장: A (Money / Page / RsData 등은 본질적으로 *공유 라이브러리* 성격)
- 단일 commit

### Task 17d: allowedDependencies 보강 + @Disabled 제거

- 17a~17c 적용 후 `MODULES.verify()` 재실행
- 각 모듈의 `allowedDependencies` 를 verify 결과에 맞게 보강
- ModularityTest 의 `@Disabled` 제거 (Task 17 본 commit)
- 단일 commit

### Task 17 본 commit (이 보고서가 포함되는 commit)

- 본 보고서 작성 + `@Disabled` 사유 갱신
- 후속 17a~17d 가 별도 PR/commit 으로 진행될 수 있도록 분리

---

## 5. 영향도 평가

- **cherry-pick 영향**: 본 commit 은 _후속 task 도입_ 만 포함하므로 cherry-pick 시 기존 코드를 깨지 않음
- **회귀 가드 손실**: ModularityTest 가 여전히 @Disabled 상태로 머무는 동안은 모듈 경계 회귀를 자동 검출 불가. 17a~17d 완료 후 회복
- **현재 분리도**: Gradle 모듈 분리는 끝났으므로 *물리적 boundary* 는 확보됨. Modulith verify 는 *논리적 boundary* 의 명시화만 남은 상태

---

## 6. References

- plan: `docs/reports/2026-05-21-module-flattening-plan.md` Task 17 (Step 3 분기 지침)
- spec: `docs/specs/2026-05-21-module-flattening-design.md` §3 (모듈 경계 원칙)
- 시도 commit: 본 보고서 동봉 commit
- Spring Modulith 2.0.3 docs: https://docs.spring.io/spring-modulith/reference/fundamentals.html
