# W6 — Query Optimization + Log Collection 측정 자리

> 작성: 2026-05-20 / 측정 예정: staging 환경 부하 사이클 직후

## 0. 목적
JSON 로깅 + slow query threshold 도입 효과를 staging 부하 사이클(K6)에서 정량 확인.
N+1 / Eager fetch / 누락 인덱스 후보를 식별하고 W7 (HA 도입 전) 까지 후속 PR로 분리 진행.

## 1. 도입 항목 요약
| 항목 | 적용 위치 | 비고 |
|------|----------|------|
| `logstash-logback-encoder` 9.0 | `gradle/libs.versions.toml` | Logback 1.5 + SLF4J 2.x 라인 |
| JSON_STDOUT appender | `support/logging/src/main/resources/logback-spring.xml` | prod/staging/loadtest 활성 |
| MDC 자동 승격 | traceId/spanId/memberId/requestId | 추후 분산 추적 도입 호환 |
| Slow query threshold | `application-prod.yml`: 500ms / `application-loadtest.yml`: 200ms | `org.hibernate.SQL_SLOW` 로거 |

## 2. 측정 시나리오 (사전 정의)
```
시나리오 A — 정상 기동 5분 (사용자 없음)
  목적: JSON 라인 파싱 가능 여부, MDC 누락 여부, app=giftify-be 필드 부착 여부
시나리오 B — K6 정상 부하 10분 (RPS 50)
  목적: slow query 검출 수, SQL_SLOW 로거가 정상적으로 JSON line 으로 흘러가는지
시나리오 C — K6 부하 + 인기 상품 단건 조회 집중 (RPS 100)
  목적: product-detail 캐시 적중률(W4-W5 도입) + slow query 의 후보 SQL 식별
```

## 3. Before/After 항목 (측정 후 채움)

### 3.1 로그 라인 가독성
| 항목 | Before (plain) | After (JSON) | 차이 |
|------|----------------|--------------|------|
| 평균 라인 길이 | 측정 예정 | 측정 예정 | |
| traceId 부착 | x | o | k8s 수집 파이프라인에서 grep 가능 |
| 스택트레이스 멀티라인 → 1 이벤트 | x | o | LogstashEncoder 기본동작 |

### 3.2 Slow query 검출
| Threshold | 시나리오 B 검출 수 | 시나리오 C 검출 수 | Top-3 SQL 패턴 |
|-----------|--------------------|--------------------|----------------|
| 200ms (loadtest) | 측정 예정 | 측정 예정 | 측정 예정 |
| 500ms (prod 가정) | 측정 예정 | 측정 예정 | 측정 예정 |

### 3.3 Hibernate Statistics (load-time)
```
Sessions opened:
Sessions closed:
Transactions:
Queries:
NaturalId queries:
Entities loaded:
Entities updated:
Collections loaded:
Slowest query (ms):
```

## 4. N+1 / 인덱스 식별 (사후 작성)
> 시나리오 B/C 로그에서 추출된 Top-3 SQL 패턴별로 작성.

### 4.1 후보 1
- **SQL 패턴**: `SELECT ... FROM product WHERE ...`
- **호출 빈도** (10분 동안):
- **평균 latency**:
- **추정 원인**:
  - [ ] N+1 (collection lazy + 반복 호출)
  - [ ] Eager fetch 누락 → 단일 join 으로 해소 가능
  - [ ] 인덱스 누락
- **수정안**:
- **PR 분리**: `perf/<topic>`

### 4.2 후보 2
(동일)

### 4.3 후보 3
(동일)

## 5. 운영 영향 평가
| 항목 | 영향 | 비고 |
|------|------|------|
| JSON 인코더 오버헤드 | 측정 예정 | LogstashEncoder ~ 평균 50µs/event 알려져 있음 |
| Slow query 로깅 자체 비용 | 측정 예정 | threshold 미만은 hibernate 비용 없음 |
| stdout 출력량 | 측정 예정 | k8s 노드 디스크 압박 가능성 — fluentbit 가속 처리 가능 |

## 6. 후속 작업
| ID | 항목 | 우선순위 | PR 분리 여부 |
|----|------|---------|---------------|
| W6.1 | N+1 후보 1 수정 | TBD | 별도 PR |
| W6.2 | 누락 인덱스 추가 (Flyway V_) | TBD | 별도 PR |
| W6.3 | Prometheus 측 hibernate metrics 연결 (hibernate.statistics.true) | 중 | W7 HA 사이클 |

## 7. 의존성 / 차후 사이클 연계
- W7 HA 도입 시 JSON 로그 라인이 k8s replica 별로 흘러야 함 — 본 사이클이 그 사전 작업.
- W8 모니터링 보강 시 traceId 컨텍스트를 OpenTelemetry 로 잇는 전환점.

---
**상태**: 미측정 (코드 도입만 완료, staging 부하 사이클 후 결과 채움)
