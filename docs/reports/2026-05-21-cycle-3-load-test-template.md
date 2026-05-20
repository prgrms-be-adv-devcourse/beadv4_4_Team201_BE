# Cycle 3 Load Test — MS3 종료 측정 자리

> 작성: 2026-05-21 / W9 (T9.1-T9.6)

## 0. 목적
MS1 baseline (W3) → MS3 종료 시점 부하 한계 비교.
목표: VU 200+ 또는 RPS 5k+ 달성, failover 중 error rate < 5%.

## 1. 시나리오 구성

### 1.1 Stress (T9.1)
```
duration: 30m
vus: ramp 0 → 200 over 5m, hold 200 for 20m, ramp down 5m
script: scripts/business-scenario.js
target: http://giftify-api-staging.chan99k.dev
```

### 1.2 Soak 4h (T9.2)
```
duration: 4h
vus: 100 constant
metrics: heap/GC/Hikari/replication lag — 30분 간격 Grafana 스냅샷 6장
```

### 1.3 Failover Load (T9.3)
```
duration: 15m
background: k6 100 VU
event: T+5m 에 Primary pod kill
```

### 1.4 Concurrency Re-run (T9.4)
- 2026-03-22 concurrency-test-report.md 와 동일 시나리오 재실행

## 2. 측정 결과 (실행 후 채움)

### 2.1 Stress
| 메트릭 | MS1 baseline | MS3 결과 | 증가율 |
|--------|--------------|---------|--------|
| Max sustained VU | 100 | ? | ? % |
| p95 latency | ?ms | ?ms | ? % |
| Error rate | ?% | ?% | |
| Throughput (RPS) | ? | ? | ? % |

### 2.2 Soak 4h
| 메트릭 | 0h | 1h | 2h | 3h | 4h | 추세 |
|--------|----|----|----|----|----|------|
| Heap baseline (GC 후) | ?MB | ? | ? | ? | ? | 안정/상승 |
| Hikari active connections | ? | ? | ? | ? | ? | 안정/상승 |
| Replication lag | ?s | ? | ? | ? | ? | 안정 |
| p95 latency | ?ms | ? | ? | ? | ? | 안정 |

→ Leak 시그널: heap 베이스라인 상승 또는 active connections 단조 증가.

### 2.3 Failover Load
| 시간 (T+) | event | error rate | latency p95 |
|-----------|-------|------------|-------------|
| 0-5m | normal | ?% | ?ms |
| 5m | Primary kill | | |
| 5-7m | failover window | ?% | ?ms |
| 7-15m | recovered | ?% | ?ms |

기준: 5m-7m 동안 error rate < 5%.

### 2.4 Concurrency Re-run
- MS1 시 결과: (참조 path)
- MS3 결과:
- 차이:

## 3. Before/After 종합 (T9.5)
- 부하 한계: ?
- 핵심 병목 (해소된 것): 캐시 / Read replica / Pod 확장 중 무엇이 가장 큰 효과
- 신규 발견 병목:

## 4. MS4 분리 Decision Matrix (T9.6)

| 후보 | Read/Write 비율 | 동시성 요구 | 트래픽 패턴 | 분리 ROI |
|------|----------------|------------|------------|---------|
| payment | ? | 높음 (idempotency) | 스파이크 | ? |
| settlement | ? | 낮음 (배치) | 안정 | ? |
| catalog | ? | 높음 (read) | 안정 | ? |
| funding | ? | 매우 높음 | 스파이크 | ? |

### 4.1 분리 권장 순위
1. ?
2. ?

### 4.2 비분리 권장
- 후보: ?
- 이유: ?

---
**상태**: 미실행 — MS3 HA 도입 + Application 변경 cherry-pick 완료 후 실행 예정
