# Event Externalization Targets

> 작성: 2026-05-21 / MS4 W11 T11.1

## 0. 목적
~42 @ApplicationModuleListener 중 *외부화 대상* 식별.
판단 기준: Cross-module + Eventual consistency 허용 + 추후 분리 가능성.

## 1. 분류 기준

| 카테고리 | 정의 | 외부화 여부 |
|---------|------|------------|
| Module-internal (sync 즉시 일관성) | 동일 트랜잭션 내 처리 필요 | ❌ |
| Cross-module (eventual consistency) | 분리 가능 + ms-level lag 허용 | ✅ |
| Audit/Logging | 비-critical, fire-and-forget | ✅ (별도 topic) |
| Saga trigger | 분산 트랜잭션 보상 | ✅ (W12 의존) |
| Read-model 갱신 | 캐시/검색 인덱스 동기화 | ✅ |

## 2. 외부화 대상 분류 표

### 2.1 외부화 대상 ✅

| 이벤트 | 발행 모듈 | Listener 모듈 | Topic | 이유 |
|--------|----------|--------------|-------|------|
| PaymentSucceeded | core/payment | notification, settlement | giftify.payment.events | Cross-module + eventual consistency |
| PaymentFailed | core/payment | order, notification | giftify.payment.events | Saga trigger |
| PaymentCanceled | core/payment | order, settlement | giftify.payment.events | Saga compensation |
| OrderConfirmed | core/order | catalog (inventory), settlement, notification | giftify.order.events | Cross-module |
| OrderCanceled | core/order | payment (refund), notification | giftify.order.events | Saga compensation |
| FundingParticipated | core/funding | order, notification | giftify.funding.events | Cross-module |
| FundingCompleted | core/funding | settlement, notification | giftify.funding.events | Cross-module |
| FundingCanceled | core/funding | payment, notification | giftify.funding.events | Saga compensation |
| NotificationRequested | (다수) | notification | giftify.notification.events | Audit + decoupling |
| UserSignedUp | member | notification, audit | giftify.audit.events | Audit |
| UserDeactivated | member | (다수 cleanup) | giftify.audit.events | Cross-module |

→ 외부화 대상 **11 건**.

### 2.2 외부화 비대상 ❌

| 이벤트 | 이유 |
|--------|------|
| WalletDeducted (in core) | 잔액 즉시 정합 필요 (sync) — 동일 트랜잭션 |
| OrderItemAdded | Order Aggregate 내 transient — 외부 발행 의미 없음 |
| ProductStatusChanged (Approved 등) | DB transaction 일관성 필수 |
| InternalCommandExecuted (Resilience4j 회로) | Internal trace 용 — 외부 발행 부적합 |
| CartItemAdded | Cart 의 Aggregate 내 |

## 3. 외부화 활성화 코드 (Modulith 사용)

```java
// 외부화 대상 이벤트에 @Externalized 추가
package app.giftify.core.payment.event;

@Externalized("giftify.payment.events::#{#this.orderId.toString()}")
public record PaymentSucceeded(
    Long paymentId,
    Long orderId,
    BigDecimal amount,
    String correlationId,
    Instant occurredAt
) implements DomainEvent {}
```

## 4. 측정 메트릭
| 메트릭 | 목적 |
|-------|------|
| `modulith.events.outbox.size` | Outbox 처리 지연 감지 |
| `modulith.events.publish.duration` | 발행 latency |
| `kafka.producer.record-send-rate` per topic | 발행 throughput |
| `kafka.consumer.lag` per consumer group | Consumer lag (alert: > 1000) |

## 5. Migration 순서
| Phase | 대상 | 비고 |
|-------|------|------|
| 1 | Notification (NotificationRequested) | 가장 비-critical, 검증 용이 |
| 2 | Audit (UserSignedUp 등) | 영향 범위 작음 |
| 3 | Order/Payment/Funding 도메인 이벤트 | Saga 통합 (W12 의존) |
| 4 | Settlement (T11.9 결정 기반) | MS5 분리 작업의 입력 |

## 6. 후속 (out of scope)
- Compensating event 의 catch-all DLT consumer (manual ops 트리거)
- Schema evolution 발생 시 BACKWARD_TRANSITIVE 규칙 자동 검증 (CI step)

---
**상태**: 분류 확정. Phase 1 부터 점진 외부화 진행.
