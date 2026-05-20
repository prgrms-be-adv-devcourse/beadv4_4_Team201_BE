# Anti-pattern Identification Report

> 2026-05-21 / module flattening (T10.24~T10.29) 사전 입력
> 본 보고서는 Task 1 (Group A) 의 산출물이며, 후속 task 의 grep 결과 박제.

## 1. 식별 대상 — 확정

### 1.1 bc/core/facade/CoreFacade

**파일**:
- `bc/core/src/main/java/app/giftify/facade/CoreFacade.java` (본체)
- `bc/core/src/main/java/app/giftify/facade/vo/GetOrdersResult.java`
- `bc/core/src/main/java/app/giftify/facade/vo/PlaceOrderResult.java`
- `bc/core/src/main/java/app/giftify/facade/vo/GetOrderResult.java`
- `bc/core/src/main/java/app/giftify/facade/command/ParticipateFundingItemCommand.java`
- `bc/core/src/main/java/app/giftify/facade/command/ParticipateFundingCommand.java`

**Production caller** (1 개):
- `bc/core/src/main/java/app/giftify/order/adapter/inbound/web/controller/OrderController.java`

**Test caller** (1 개):
- `bc/core/src/test/java/app/giftify/facade/CoreFacadeTest.java`

**Doc reference** (참고만):
- `docs/sequences/4.1-funding-lifecycle.md`
- `docs/user-stories/4.1-funding-lifecycle.md`

**분석**:
- `CoreFacade.participateFunding(...)` 은 `@Transactional` 을 갖고 Order, Payment,
  Funding 모듈을 *한 트랜잭션* 으로 묶음. 본질은 *분산 트랜잭션 코디네이터*.
- 해체 후 `bootstrap/api-server/src/main/java/app/giftify/usecase/ParticipateFundingUseCase`
  로 이동. `@Transactional` 제거, 1 단계 호출 (placeOrder + createPayment) 까지만 직접 수행.
- 2 단계 (markOrderAsPaid, processFundingActions) 는 *PaymentSucceeded 이벤트 listener*
  에 위임 (각 모듈 안의 `@ApplicationModuleListener`).

**후속 task**: 14 (UseCase 신설 + listener 보강), 15 (CoreFacade 삭제 + caller 갱신).

---

### 1.2 bc/catalog/replica/member

**파일**:
- `bc/catalog/src/main/java/app/giftify/replica/member/MemberEventListener.java`
- `bc/catalog/src/main/java/app/giftify/replica/member/MemberSyncUseCase.java`
- `bc/catalog/src/main/java/app/giftify/replica/member/MemberRepository.java`
- `bc/catalog/src/main/java/app/giftify/replica/member/Member.java`

**Consumer 분포** (3 모듈 모두에 분산):

| 모듈 | Production caller | Test caller |
|-----|------------------|-------------|
| cart | `application/inbound/CartService.java` | `CartServiceTest.java` |
| product | `adapter/outbound/elasticsearch/ProductEsAdapter.java`, `application/service/ProductService.java` | `ProductServiceCacheSliceTest.java`, `ProductServiceTest.java`, `concurrency/ProductStockConcurrencyTest.java` |
| wishlist | `adapter/in/web/controller/PublicWishlistController.java`, `application/service/WishlistItemService.java`, `application/service/WishlistService.java` | `WishlistItemServiceTest.java`, `WishlistServiceTest.java` |

**분석**:
- 단일 host 선택이 *불가능* — 3 모듈 모두 nickname 을 직접 사용.
- spec §3.3 의 *Major 2 옵션 (a) 모듈별 readmodel 분리* 가 *현실의 정답*.
- 각 모듈에 자기 readmodel 보유:
  - `bc/product/src/main/java/app/giftify/product/readmodel/ProductSellerView.java`
  - `bc/cart/src/main/java/app/giftify/cart/readmodel/CartSellerView.java`
  - `bc/wishlist/src/main/java/app/giftify/wishlist/readmodel/WishlistSellerView.java`
- 동일 이벤트 (MemberSignedEvent, MemberUpdatedEvent) 를 3 개 listener 가 처리 → outbox
  부담 3 배. 현 회원/이벤트 규모로 무시 가능.
- `SellerNicknameChangedEvent` 발행은 *product 모듈에서만* (ES 인덱스 갱신 라인 보존).

**후속 task**:
- Task 12: `ProductSellerView` + listener + Flyway migration (`product_seller_views`).
- 추가 task (plan 보강 필요): `CartSellerView`, `WishlistSellerView` — 본 보고서로 *증분 task 도출*.
- Task 13: `bc/catalog/replica/` 삭제 + 사용처 갱신.

---

## 2. 잠재 후보 — notification

**Cross-module imports**:

```
app.giftify.security.common.CurrentMemberId
```

**분석**:
- `notification` 모듈이 다른 *도메인 모듈* 에서 import 하는 것은 *없음*. 단 `support`
  (security) 의 `CurrentMemberId` (보안 컨텍스트의 memberId 추출) 만 사용.
- *Member 의 nickname, email 등 의존 없음*. 알림 발송은 *memberId + 메시지* 만 필요.
- spec §3.4 의 판단 표 적용: "식별자만 들고있고 표시 시점 동기 호출" → **readmodel 불요**.

**후속**: 본 spec 범위 외. 현 구조 유지.

---

## 3. 잠재 후보 — settlement

**Cross-module imports**:

```
app.giftify.security.common.CurrentMemberId
```

**분석**:
- `notification` 과 동일. *도메인 모듈* 의 직접 의존 없음.
- Settlement 는 *주문/결제 이벤트* (OrderConfirmed, PaymentSucceeded) 를 수신하여
  자체 `Snapshot` 으로 시점 데이터를 *박제* — 이는 spec §4 Case C 의 *이벤트 payload
  박제* 패턴. read-model 아닌 immutable snapshot in own aggregate.
- spec §3.4 의 판단 표 적용: "이벤트 payload 에 박제" → **readmodel 불요**.

**후속**: 본 spec 범위 외. 현 구조 유지.

---

## 4. 후속 task 매핑

| 식별 항목 | Plan task | Group | 상태 |
|---------|----------|-------|-----|
| CoreFacade | Task 14, 15 | F | 본 spec 안에서 해체 |
| replica/member — product 사용 | Task 12 | E | 본 spec 안에서 readmodel 신설 |
| replica/member — cart 사용 | **Task 12.5 (plan 보강 — CartSellerView)** | E | 본 spec 안에서 추가 |
| replica/member — wishlist 사용 | **Task 12.6 (plan 보강 — WishlistSellerView)** | E | 본 spec 안에서 추가 |
| replica/member — 패키지 삭제 | Task 13 | E | 본 spec 안에서 |
| notification cross-module | — | — | spec 외 (readmodel 불요) |
| settlement cross-module | — | — | spec 외 (readmodel 불요) |

## 5. Plan 의 보강 필요사항

본 식별 결과로 *원 plan 의 Task 12* 가 *3 모듈 readmodel* 로 확장되어야 함:
- Task 12 (현재): ProductSellerView 만 → 그대로 유지.
- Task 12.5 (신규): CartSellerView (cart 모듈) — Task 12 와 동일 패턴.
- Task 12.6 (신규): WishlistSellerView (wishlist 모듈) — Task 12 와 동일 패턴.
- Task 13: 모든 caller 가 *자기 모듈의 readmodel* 사용으로 갱신된 후 replica 삭제.

→ plan 의 Task 12 진행 시 본 보고서 참고하여 *3 모듈 동시 처리* 결정.

---

## 6. 결론

- spec §3.4 의 *잠재 후보 식별 의무* 충족.
- 본 spec 범위 내 *추가 readmodel 신설은 cart, wishlist 2 개* (product 외).
- spec §3.3 의 Major 2 옵션 (a) 모듈별 분리가 *3 모듈 모두 필요* 함을 확정.
- spec §9 의 Out of Scope 와 일관 — notification, settlement 의 readmodel 신설은 진행 안 함.
