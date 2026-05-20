# Kafka Topics Design (Redpanda)

> 작성: 2026-05-21 / MS4 W11 T11.3

## 0. Topic 명세

| Topic | Partitions | Replication | Retention | Compaction | 외부화 대상 이벤트 |
|-------|-----------|-------------|-----------|-----------|-------------------|
| `giftify.payment.events` | 6 | 3 | 7d | delete | PaymentSucceeded, PaymentFailed, PaymentCanceled |
| `giftify.order.events` | 6 | 3 | 7d | delete | OrderConfirmed, OrderCanceled, OrderShipped |
| `giftify.funding.events` | 6 | 3 | 7d | delete | FundingParticipated, FundingCompleted, FundingCanceled |
| `giftify.notification.events` | 3 | 3 | 1d | delete | NotificationRequested |
| `giftify.settlement.events` | 3 | 3 | 30d | delete | SnapshotCreated (4 종) |
| `giftify.audit.events` | 3 | 3 | 30d | delete | UserAudit, AdminAction |

## 1. Partition 키 전략
| Topic | Key | 이유 |
|-------|-----|------|
| payment | `orderId` | 같은 order 의 payment 이벤트 순서 보존 |
| order | `orderId` | 동일 |
| funding | `fundingId` | 펀딩 단위 순서 보존 |
| notification | `memberId` | 동일 사용자 알림 순서 보존 |
| settlement | `sellerId` | 판매자별 정산 이벤트 응집 |
| audit | `random` (no key) | 순서 불필요, 균등 분산 |

## 2. Schema 진화 정책
- **호환 정책**: BACKWARD_TRANSITIVE (Consumer 가 모든 이전 버전 읽기 가능)
- **신규 필드**: optional + default 필수
- **필드 제거**: deprecation 한 메이저 cycle 후 제거 (최소 1 milestone)
- **타입 변경**: 금지 — 신규 필드 추가 + 구필드 deprecation

## 3. Consumer Group 명명
```
{service}-{purpose}-v{version}
  ex) notification-sender-v1
       settlement-snapshot-recorder-v1
       audit-logger-v1
```

## 4. Topic 생성 manifest
```yaml
# infra/k3s/base/apps/redpanda/topics-init-job.yaml
apiVersion: batch/v1
kind: Job
metadata:
  name: redpanda-topics-init
spec:
  template:
    spec:
      restartPolicy: OnFailure
      containers:
      - name: rpk
        image: docker.redpanda.com/vectorized/redpanda:latest
        command: ["sh", "-c"]
        args:
          - |
            rpk topic create giftify.payment.events --partitions 6 --replicas 3 --topic-config retention.ms=604800000
            rpk topic create giftify.order.events --partitions 6 --replicas 3 --topic-config retention.ms=604800000
            rpk topic create giftify.funding.events --partitions 6 --replicas 3 --topic-config retention.ms=604800000
            rpk topic create giftify.notification.events --partitions 3 --replicas 3 --topic-config retention.ms=86400000
            rpk topic create giftify.settlement.events --partitions 3 --replicas 3 --topic-config retention.ms=2592000000
            rpk topic create giftify.audit.events --partitions 3 --replicas 3 --topic-config retention.ms=2592000000
```

## 5. Dead Letter Topic (DLT)
모든 topic 의 `*.dlt` 변형 자동 생성:
- Retention: 30일
- Partitions: 원본의 절반 (3 → 2, 6 → 3)
- Consumer 실패 시 Spring Modulith 가 자동 라우팅

## 6. Replication factor
- staging: 3 (전체 Redpanda 노드 = 3)
- prod: 3 동일

## 7. 의사결정
- **단일 topic 통합 vs 도메인별 분리**: **도메인별 분리** 선택. 이유:
  - Consumer 가 관심 도메인만 구독 (network/IO 절약)
  - Retention 정책 도메인별 차별화 가능 (audit 30d, notification 1d)
- **Partition 수 6 vs 3**: Payment/Order/Funding 은 *부하 spike* 대비 6. Notification/Settlement/Audit 은 *낮은 throughput* 3.
- **Compaction**: 모든 topic 은 `delete` 전략. Compaction 은 키별 최종 상태가 필요한 경우만 (예: 사용자 프로필) — 본 도메인은 불필요.

## 8. 모니터링 메트릭
- `kafka.consumer.lag` per consumer group (Prometheus + Grafana)
- `kafka.records.consumed.rate`
- DLT 이벤트 수 (alert: > 10/min)

## 9. 추후 (out of scope)
- Schema Registry 도입 (Avro 전환 시) — Task 2.5 결정에 따름
- Cross-region replication

---
**상태**: 설계 완료. 실제 topic 생성 (rpk) 은 사용자 작업 (`./scripts/init-topics.sh` 또는 manifest 적용).
