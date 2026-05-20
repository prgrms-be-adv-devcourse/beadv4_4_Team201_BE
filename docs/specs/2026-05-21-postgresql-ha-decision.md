# PostgreSQL HA Decision

> 작성: 2026-05-21 / 적용 milestone: MS3 W7 (T7.1)

## 0. 결정 사항
**선택: bitnami/postgresql Helm Chart with architecture=replication.**

Primary 1 + Replica 1 (synchronous commit) 의 최소 구성으로 시작.
필요 시 numSynchronousReplicas 증가로 확장.

## 1. 옵션 비교

| 항목 | bitnami/postgresql (Helm) | CloudNativePG (Operator) | GCP Cloud SQL |
|------|---------------------------|--------------------------|---------------|
| 복잡도 | 중 — values.yaml + Helm | 중-고 — CR + operator 학습 | 낮 — 관리형 |
| 비용 | k8s 자원 (이미 사용) | k8s 자원 (이미 사용) | ≈ $30/mo |
| 자동 Failover | architecture=replication + pg-pool 패턴 | 자동 (operator) | 자동 |
| Backup | CronJob 내장 (Velero 연계 가능) | operator 통합 (자동 WAL archive) | 관리형 |
| PVC 호환 | 기존 단일 statefulset PVC 형식과 동일 | 다른 PVC 형식 (data migration 필요) | dump/restore 필요 |
| 학습 곡선 | 낮 | 중-고 (CRD, operator lifecycle) | 낮 |
| 모니터링 통합 | postgres-exporter sidecar | operator 자체 metric exporter | Cloud Monitoring |
| 확장성 | replica 수 helm value 조정 | spec.instances 조정 (declarative) | 인스턴스 변경 |
| Vendor lock-in | 없음 (any k8s) | 없음 (any k8s) | GCP 강한 종속 |

## 2. 선택 근거

### 2.1 왜 bitnami 인가
- **현재 단일 statefulset PVC 와 동일 데이터 형식** — `pg_basebackup` 으로 Primary 만 새 cluster 로 복원하면 Replica 는 streaming replication 으로 자동 동기화. 무중단 또는 최소 점검(30분 이내) migration 가능.
- 기존 `application-prod.yml` 의 `spring.datasource.url=jdbc:postgresql://postgres-postgresql:5432/...` 패턴 유지 가능. **Replica 는 별도 service name (`postgres-postgresql-read`) 추가** — application 측 multi-pool 변경만 필요.
- 학습 비용이 가장 낮아 W7 1 sprint 안에 도입 완료 가능.

### 2.2 왜 CloudNativePG 가 아닌가
- 더 우수한 operator 자동화 (declarative scaling, in-place version upgrade, 자동 WAL archive) 가 매력적이지만:
  - PVC 형식이 달라 **데이터 migration 필수**. W7 안에 완료하기 어려움.
  - operator lifecycle / CRD 의존성 학습 비용 — 본 milestone 의 핵심은 *HA 도입 자체* 이지 operator 학습이 아님.
- 포폴리오 데모 컨텍스트에서 "Postgres HA 를 직접 운영" 보다 "HA 패턴을 *이해하고* 도입" 이 더 중요. bitnami 가 그 학습 곡선에 적합.

### 2.3 왜 GCP Cloud SQL 이 아닌가
- 월 $30 추가 비용 부담 (개인 포폴리오 프로젝트).
- Vendor lock-in — GCP 외 환경 (오프라인 데모 / 다른 클라우드 평가) 에서 재현 어려움.
- "k3s 위에 PostgreSQL HA 를 *직접* 운영했다" 가 포트폴리오 가치. 관리형으로 회피하면 그 가치 소실.

## 3. Architecture

```
                          ┌─────────────────────────────────┐
                          │  k3s (giftify namespace)         │
                          │                                  │
   ┌──────────────┐       │  ┌──────────────────────────┐    │
   │  api-server  │ Write │  │  postgres-postgresql-0   │    │
   │   (Spring)   ├──────►│  │  (Primary, RW)            │    │
   │  Routing     │       │  └──────────┬───────────────┘    │
   │  DataSource  │       │             │ streaming repl     │
   │              │       │             ▼                    │
   │              │ Read  │  ┌──────────────────────────┐    │
   │              ├──────►│  │  postgres-postgresql-1   │    │
   │              │       │  │  (Replica, RO)           │    │
   └──────────────┘       │  └──────────────────────────┘    │
                          └─────────────────────────────────┘

   Service:
   - postgres-postgresql (headless, Primary 만 라우팅)
   - postgres-postgresql-read (Replica 라우팅, replica 0개 시 Primary fallback)
```

## 4. Replication 정책
- **architecture**: `replication` (bitnami chart value)
- **synchronousCommit**: `on` — Primary 에 쓰기 ACK 전 Replica 동기화 필수
- **numSynchronousReplicas**: `1` — 1개 Replica 는 동기 (lag = 0 보장), 나머지는 비동기 (확장 시)
- **fsync, full_page_writes**: 기본값 (default = on)

### Trade-off
- 동기 복제 1개는 *쓰기 latency 증가* (Primary → Replica ACK 왕복) ≈ +1-5ms.
- 대신 zero data loss 보장. RTO/RPO ≈ 30s/0 목표.

## 5. Migration Plan

### 5.1 Staging 우선
- staging overlay 만 신규 HA chart 적용.
- 기존 staging 데이터는 한 번 dump → 새 cluster 로 restore (15분 점검 예상).

### 5.2 Prod 단계 (W7 후반 또는 W8)
- staging 에서 *Failover Test (T7.5)* + *Routing Verification (T7.9)* + *Load Test (W9)* 통과 후 적용.
- Prod 점검 시간: 30분 예상 (dump/restore + DNS cutover).
- ArgoCD 자동 sync 후 health probe + replication lag (`pg_replication_lag_seconds < 5s`) 확인 후 트래픽 인가.

### 5.3 Rollback
- 기존 PVC 보존 (delete chart 시 `--keep-history` 또는 PVC retention).
- Issue 시 application.yml 의 `jdbc-url` 을 원복 + Replica 의 Read traffic 비활성화.

## 6. 후속 결정 자리 (Task 1.1-1.7 에서 채움)
| 항목 | 결정 시점 |
|------|----------|
| HikariCP pool size 최종 | Task 1.5.1 (Cycle 0 Pending 190 기준 산정) |
| readOnly 적용 누락 위치 | Task 1.6 audit 후 |
| Replication lag alert threshold | Task 1.2.3 — 10s/2min 시작, 측정 후 조정 |
| Failover RTO 측정값 | Task 1.3 (목표 < 30s) |
| Replica 수 증가 여부 | W9 stress test 결과 후 |

## 7. 의존성
- bitnami helm repo 추가 (`helm repo add bitnami https://charts.bitnami.com/bitnami`)
- staging namespace에 sufficient PVC quota (Primary 20Gi + Replica 20Gi)
- postgres-exporter sidecar 활성화 (already in bitnami chart default)

---
**상태**: 결정 확정. Task 1.1 (Helm values 파일 작성) 으로 진행.
