# Settlement Event Delivery Decision

> 작성: 2026-05-21 / MS4 W11 T11.9 (MS5 W13 분리 작업의 선행 결정)

## 0. 결정 사항
**선택: Kafka (Spring Modulith Events Externalization).**
Transactional Outbox 패턴은 Modulith 내부에서 자동 적용 (modulith-events-jpa).

## 1. 옵션 비교

| 항목 | Kafka (Modulith externalization) | Plain REST | Debezium CDC |
|------|----------------------------------|-----------|---------------|
| 분리 시 표준 | ✅ | partial | ✅ |
| DB transaction 정합 | Modulith Outbox 자동 | 수동 (best-effort) | 자동 (WAL) |
| Replay 가능 | ✅ (Kafka retention) | ❌ | ✅ |
| Schema 관리 | Topic 별 | 엔드포인트 컨벤션 | DB 스키마 = 이벤트 |
| Operational 복잡도 | 중 (Redpanda 운영 중) | 낮 | 높 (CDC connector + Debezium) |
| 도입 비용 | 낮 (인프라 기존) | 매우 낮 | 중 (Debezium 학습) |
| Coupling | 느슨 (Topic schema) | 강 (HTTP 계약) | 강 (DB schema) |
| Failure 시 | DLT + Modulith retry | 호출자 책임 | Debezium retry |

## 2. 선택 근거

### 2.1 왜 Kafka 인가
- **Redpanda 이미 운영 중** — 신규 인프라 없음.
- **Modulith Externalization 자동 통합** — `@Externalized` 어노테이션만 추가하면 Outbox + 발행 처리.
- **Settlement 가 분리 서버(별 Pod)로 가더라도 코드 거의 무변경** — Consumer 가 동일 topic 구독.
- **Replay 가능** — 정산 누락 시 Kafka offset 되감기로 재처리.

### 2.2 왜 Plain REST 가 아닌가
- 분리 서버로 REST 호출 시 *동기* — 호출 성공/실패 처리 책임이 호출자에게.
- DB transaction 과 REST 호출 간 정합 보장 어려움 (호출 후 DB rollback 시).
- Settlement 의 부분 장애가 Core 의 transaction 에 직접 영향.

### 2.3 왜 Debezium CDC 가 아닌가
- DB 스키마 자체를 contract 로 노출 — coupling 가장 강.
- Debezium connector 운영 부담 — KafkaConnect cluster 별도 운영 필요.
- Schema 변경 시 consumer 영향 큼.

## 3. Topic 매핑
| Source Aggregate | Target Topic | Event 종류 |
|------------------|--------------|-----------|
| OrderConfirmed | `giftify.settlement.events` | SnapshotCreated |
| PaymentSucceeded | `giftify.settlement.events` | SnapshotCreated |
| OrderCanceled | `giftify.settlement.events` | RollbackTriggered |
| FundingCompleted | `giftify.settlement.events` | SnapshotCreated |

## 4. Modulith Externalization 구현 예
```java
package app.giftify.settlement.events;

@Externalized("giftify.settlement.events::#{#this.sellerId.toString()}")
public record SnapshotCreated(
    Long sellerId,
    Long orderId,
    BigDecimal amount,
    Instant occurredAt,
    String correlationId
) {}
```

- `::#{...}` SpEL: Kafka partition key 로 sellerId 사용 (Section 1 의 partition 전략과 일치).
- Modulith 가 자동으로 outbox table 에 저장 → 별도 publisher 스레드가 Kafka 로 발행.

## 5. Consumer 분리 시 (MS5 W13 작업)
- `bootstrap/settlement-server` (별 Spring Boot 앱)
- `KafkaListener("giftify.settlement.events")` → SettlementService 호출.
- 동일 코드베이스 (api-server 와 코드 공유) but 별 JVM.

## 6. 부분 분리 단계
| 단계 | 위치 | Externalization | 비고 |
|------|------|-----------------|------|
| Phase 1 (현재) | api-server 내부 | OFF (Modulith internal) | 기존 ApplicationModuleListener |
| Phase 2 (W11) | api-server 내부 | ON (Externalized) | Kafka 통해 *자기 자신* 이 consume |
| Phase 3 (MS5 W13) | settlement-server 분리 | ON | api-server publish, settlement-server consume |

## 7. Failure 처리
- Outbox 발행 실패 → Modulith 자동 retry (default 3회)
- 3회 실패 → `*.dlt` 이동 + alert
- Consumer 처리 실패 → IdempotentConsumer (T11.6) 이미 적용 → 안전 재시도

---
**상태**: 결정 확정. Phase 2 (W11) 부터 적용. MS5 W13 분리 작업의 선행 조건.
