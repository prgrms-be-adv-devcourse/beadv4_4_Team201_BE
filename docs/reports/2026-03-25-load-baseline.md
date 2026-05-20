# Cycle 0 Baseline Report

## Environment

| Item | Value |
|------|-------|
| Date | 2026-03-24 (20:45 KST) |
| Server | giftify-staging (e2-standard-2, 2vCPU/8GB) |
| Scenario | funding-scenario.js (read-only, Mock Auth) |
| VU | max 60, 5-stage ramp |
| Duration | 4m17s |
| Profile | prod,loadtest (Hibernate Statistics ON) |

## Overall Results

| Metric | Value |
|--------|-------|
| RPS | 39.2/s |
| Total Requests | 10,080 |
| Iterations | 2,016 |
| **p(95) Response Time** | **454ms** |
| Error Rate | 0.00% |
| Checks Passed | 100% (10,080/10,080) |

## Per-API Breakdown

```
+------------------+--------+--------+--------+--------+--------+
| API              |  avg   |  med   | p(90)  | p(95)  |  max   |
+------------------+--------+--------+--------+--------+--------+
| product_search   | 166ms  | 124ms  | 350ms  | 424ms  | 4.89s  |
| product_detail   | 122ms  | 83ms   | 283ms  | 370ms  | 4.96s  |
| wishlist         | 141ms  | 96ms   | 321ms  | 425ms  | 4.38s  |
| cart_add         | 225ms  | 177ms  | 471ms  | 595ms  | 5.15s  |
| funding_list     | 130ms  | 89ms   | 300ms  | 382ms  | 5.15s  |
| order_create     | 0ms    | 0ms    | 0ms    | 0ms    | 0ms    |
+------------------+--------+--------+--------+--------+--------+
| TOTAL            | 157ms  | 109ms  | 354ms  | 454ms  | 5.15s  |
+------------------+--------+--------+--------+--------+--------+
```

Note: order_create는 read-only 모드(TEST_RECEIVER_ID 미설정)라 실행되지 않음.

## SLO Compliance (Cycle 0 SLO: Read p95 < 500ms)

| API | p(95) | SLO | Result |
|-----|-------|-----|--------|
| product_search | 424ms | < 200ms | FAIL |
| product_detail | 370ms | < 200ms | FAIL |
| wishlist | 425ms | < 200ms | FAIL |
| cart_add | 595ms | < 500ms | FAIL |
| funding_list | 382ms | < 200ms | FAIL |
| **Overall** | **454ms** | **< 500ms** | **PASS** |

Overall p(95)는 500ms SLO를 통과하지만, 개별 API threshold는 대부분 위반.

## Bottleneck Analysis

1. **cart_add** (p95=595ms) — 가장 느린 API. Cart + WishlistItem + Product JOIN 복합 쿼리
2. **max latency 5.15s** — cold start 또는 GC pause 의심. JVM warming 후 안정화 필요
3. **전체 avg 157ms, med 109ms** — median은 양호하나 tail latency가 높음 (p95/med = 4.2x)

## Identified Optimization Targets (MS2)

| Priority | Target | Approach | Expected Impact |
|----------|--------|----------|-----------------|
| 1 | cart_add latency | N+1 query 분석 (Hibernate Statistics) | p95 50%+ 개선 |
| 2 | Tail latency (5s+) | JVM warm-up, GC tuning (Compact Headers 이미 적용) | max < 2s |
| 3 | product_search | Elasticsearch query 최적화 또는 Redis 캐시 | p95 < 200ms |
| 4 | HikariCP pool | Baseline에서 pending 확인 필요 (Grafana) | pool 고갈 방지 |

## Hibernate Statistics

Grafana "Hibernate Statistics" 패널에서 확인 필요:
- [ ] hibernate_query_executions_total (rate) — N+1 의심 구간
- [ ] hibernate_query_executions_max_seconds — slow query 후보
- [ ] hibernate_sessions_open_total — 세션 수
- [ ] hibernate_second_level_cache_requests_total — 2L cache (현재 0% 예상)
- [ ] hibernate_statements_total — PreparedStatement 재사용

## Comparison with Previous Results

| Test | Date | VU | RPS | p(95) | Error |
|------|------|----|-----|-------|-------|
| Funding Scenario (local) | 2026-03-16 | 60 | 46.07 | 107ms | 0% |
| Stress Test (staging) | 2026-03-23 | 120 | ~178 | SLO 초과 | 0% |
| **Baseline (staging)** | **2026-03-24** | **60** | **39.2** | **454ms** | **0%** |

Staging p(95)=454ms vs local p(95)=107ms — staging 환경이 더 느림 (VM 성능, 네트워크 홉).

## Next Steps

1. Grafana Hibernate Statistics 패널 스크린샷 확보
2. MS2 Cycle 1: 가장 큰 병목(cart_add) 분석 및 최적화
3. 최적화 후 재측정 → Before/After 비교
