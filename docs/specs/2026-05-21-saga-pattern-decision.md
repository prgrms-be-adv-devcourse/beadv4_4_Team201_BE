# Saga Pattern Decision (PlaceOrder 분산 트랜잭션)

> 작성: 2026-05-21 / MS4 W12 T12.4-T12.5

## 0. 결정 사항
**선택: Choreography Saga (이벤트 기반 분산 코오디네이션).**
중앙 orchestrator 없음 — 각 도메인이 이벤트 수신 + 자신의 응답 이벤트 발행.

## 1. 대상 흐름 — PlaceOrder

```
Order Created
    └─→ Payment Requested
            └─→ Payment Confirmed
                    └─→ Order Confirmed
                            └─→ Funding Participated
                                    └─→ (success)

   (실패 시 보상 흐름 — 역순)
   Payment Failed → Order Canceled
   Order Confirmed → Inventory 차감 실패 → Payment Cancel Required → Refund
```

## 2. Orchestration vs Choreography

| 항목 | Orchestration (중앙 코디네이터) | Choreography (이벤트 체인) |
|------|-------------------------------|---------------------------|
| 가시성 | 높 (state machine 한 곳) | 낮 (이벤트 분산) — Correlation ID 로 보완 |
| 결합도 | 코디네이터에 강한 결합 | 도메인 간 느슨 결합 |
| 신규 단계 추가 | 코디네이터 수정 | 신규 listener 추가만 |
| 디버깅 | 직관 (state machine 추적) | Kafka topic + correlationId 필터 |
| 현재 코드 적합도 | 별도 도입 비용 | **~42 @ApplicationModuleListener 이미 choreography** |
| 보상 (compensation) | 코디네이터가 명시적 호출 | 보상 이벤트 발행 + 역방향 listener |
| 부분 실패 처리 | 코디네이터 책임 | 각 도메인 책임 (DLT + retry) |

## 3. 선택 근거

### 3.1 왜 Choreography 인가
- **현재 코드가 이미 choreography 친화** — Spring Modulith `@ApplicationModuleListener` 42 건 = 이벤트 체인 구조.
- **신규 도입 비용 최소** — 추가 인프라/라이브러리 없음.
- **분리 서버 (MS5 settlement-server) 도입 시 자연 확장** — Kafka 외부화 (W11) 와 결합 시 분리 환경에서도 동작.
- **느슨 결합** — 신규 도메인 (예: Coupon, Loyalty) 추가 시 기존 코드 수정 없음.

### 3.2 왜 Orchestration 이 아닌가
- 별도 state machine 라이브러리 (Spring Statemachine 등) 학습 비용.
- 중앙 코디네이터 자체가 분산 transaction 의 단일 실패점.
- 본 milestone 의 핵심 가치는 *이벤트 외부화 + Saga 패턴 학습* 이지 state machine 학습이 아님.

## 4. Choreography 핵심 도구

### 4.1 Correlation ID
모든 이벤트에 동일 `correlationId` 전파:
```java
public record OrderCreated(
    Long orderId,
    String correlationId,
    Instant occurredAt,
    // ...
) implements DomainEvent {}
```

### 4.2 Compensation Event
각 도메인의 listener 가 *실패* 시 *보상 이벤트* 발행:
```
PaymentConfirmed listener (in Order) fails
    → OrderConfirmFailed publish
        → PaymentCancelRequested listener (in Payment) fires
            → PG cancel API call
                → PaymentCanceled publish
                    → OrderCanceled listener (in Order) fires
```

### 4.3 Idempotency
모든 listener 는 `eventId` 기준 idempotent (IdempotentConsumer 패턴 — T11.6).

## 5. ArchUnit 규칙 (강제 메커니즘)
```java
@ArchTest
static final ArchRule allEventsCarryCorrelationId =
    classes().that().areAssignableTo(DomainEvent.class)
             .should().haveOnlyFinalFields()
             .andShould().beRecords()
             .andShould().haveOnlyPrimitiveOrCertainFields(
                 // record component 중 'correlationId' 포함 강제
             );
```
(구체 규칙은 PoC 단계에서 정의.)

## 6. 가시성 보완 — Saga Tracker
중앙 orchestrator 가 없으므로 *사후 추적 도구* 가 필요:
- Kafka 모든 topic 을 `correlationId` 로 필터 → timeline 재구성
- Grafana 대시보드: `correlationId` query → 이벤트 시계열 차트
- Saga 단계별 latency 메트릭 (Micrometer histogram)

## 7. Saga 단계 명세 (PlaceOrder)

| 단계 | Trigger Event | Action | Success Event | Failure → Compensation |
|------|--------------|--------|---------------|----------------------|
| 1 | UI 요청 | Order create | OrderCreated | (no compensation — DB rollback) |
| 2 | OrderCreated | PG 결제 요청 | PaymentRequested | OrderCanceled |
| 3 | PaymentRequested | PG callback 대기 | PaymentConfirmed | PaymentCanceled → OrderCanceled |
| 4 | PaymentConfirmed | Inventory 차감 | OrderConfirmed | PaymentCancelRequested → PaymentCanceled → OrderCanceled |
| 5 | OrderConfirmed | Funding 참여 | FundingParticipated | (final — no compensation; manual ops) |

## 8. PoC 범위 (Task 3.3)
- Phase 1: 위 단계 5개 중 Phase 1-3 (PaymentConfirmed 까지) 통합 테스트
- Phase 2: Compensation 흐름 검증 (PaymentFailed → OrderCanceled)
- Phase 3: 외부화 후 Kafka via testcontainers Redpanda

## 9. 후속 (out of scope)
- Orchestration 으로 마이그레이션은 *Saga 추적 복잡도 임계점* 도달 시 재평가 — 현재 5단계는 충분히 관리 가능.

---
**상태**: 결정 확정. Task 3.3 (Saga 구현) 의 입력.
