# Cart Add 측정 보고서 (Phase 2 — V1.0.2 인덱스 적용 후)

> Day 1 진단 보고서: [`2026-04-30-cart-add-diagnose.md`](./2026-04-30-cart-add-diagnose.md)
> 측정 일자: 2026-05-04
> 환경: staging k3s (giftify-staging GCE), k6 별도 VM (giftify-k6)
> 적용 마이그레이션: `V1.0.2__add_cart_items_cart_id_index.sql` (PR #463로 develop 머지)

## 0. TL;DR

| 항목                     | BEFORE (Day 1)  | AFTER (Day 2)   | 효과            |
|--------------------------|-----------------|-----------------|-----------------|
| `cart_items.cart_id` 조회 | Seq Scan (9785행 filter) | Index Scan      | 5.7배 단축      |
| Statements/query 비율    | 62.8x           | 6.0x            | 90% 감소        |
| 50 VU × 1min p95 응답    | (미측정)        | 3.0s            | 신규 baseline   |
| 50 VU × 1min p99 응답    | (미측정)        | 3.6s            | 신규 baseline   |
| 50 VU × 1min error rate  | (미측정)        | 0%              | 안정성 확인     |

**결론**: 인덱스 단독 적용으로 DB 실행 시간은 5.7배 단축됐으나, 부하 환경 응답 시간 p95 3.0s는 여전히 SLA 초과 수준. **잔여 dominant cost는 JPA `cascade.ALL` fanout으로 추정**되며 이것이 다음 최적화 대상.

## 1. 측정 환경

### 1.1 인프라 토폴로지

```
+--------------------+         +---------------------+         +-----------+
|  giftify-k6 VM     |  HTTP   |  giftify-staging    |   TCP   | postgres  |
|  (k6 v1.6.1)       |-------->|  (k3s, NodePort     |-------->| (PVC      |
|  asia-northeast3-a |  30080  |   30080→ClusterIP→  |  5432   |  persisted|
|                    |         |   api-server pod)   |         |  index)   |
+--------------------+         +---------------------+         +-----------+
   외부 VM(부하 발생)            ClusterIP service 노출            stateful
```

- VPC firewall로 8080 직결 차단 → NodePort 30080 사용
- VM 간 RTT: 평균 30ms (단일 호출 기준)

### 1.2 인증 모드

`SPRING_PROFILES_ACTIVE=prod,loadtest`로 `DynamicMockAuthFilter` 활성화. JWT 우회 + `X-Test-User-ID` 헤더 기반 인증.

```
loadtest:
  mock-auth:
    enabled: true
```

### 1.3 데이터 상태

`loadtest` 스키마에 사전 시드된 데이터:
- givers: `member_id` 1001~1050
- receivers: `member_id` 1051~1060
- wishlists: 60건 (giver 1인당 평균 1.2건)
- wishlist_items: 6,000건 (wishlist당 평균 100건)
- **cart_items: 5,000건** (giver 50명 × 100건씩 사전 적재)

**의도된 부작용**: 모든 giver의 cart에 cart_items가 이미 존재 → cart_add 호출은 **거의 100% UPDATE 경로**, 신규 INSERT 경로 시뮬 불가 (§3.2 참조).

### 1.4 DB 인덱스 상태

V1.0.2 적용 후:

```
\d g7app.cart_items
  Indexes:
    "cart_items_pkey" PRIMARY KEY, btree (id)
    "idx_cart_items_cart_id" btree (cart_id)        ← 신규
```

## 2. 측정 1 — In-pod RTT (10회 반복, 단일 호출)

### 2.1 측정 절차

```
1. kubectl exec api-server-* -- pod IP 노출
2. VM에서 curl http://<pod-IP>:8080/api/v2/carts (POST) 10회 반복
3. actuator/metrics에서 hibernate.* metric ID 캡처 (BEFORE/AFTER)
4. RTT median, p95 계산
```

사용한 metric ID:
- `hibernate.query.executions`
- `hibernate.statements`
- `hibernate.flushes`
- `hibernate.entities.fetches`

### 2.2 결과 — Hibernate metric per request (10회 평균)

```
+------------------------+--------+-------+----------+----------+
| metric                 | BEFORE | AFTER | DELTA    | 1호출당  |
+------------------------+--------+-------+----------+----------+
| query.executions       | -      | -     | 60       | 6.0      |
| statements             | -      | -     | 360      | 36.0     |
| stmts/query (증폭률)   | 62.8x  | 6.0x  | -56.8x   | -90.4%   |
+------------------------+--------+-------+----------+----------+
```

> Day 1 baseline 비교: stmts/query = 3768/60 = 62.8x → AFTER 6.0x (10x 압축).

### 2.3 결과 — RTT (개별 raw 값)

> Note: in-pod 단일 호출 RTT raw log는 `tasks/<task-id>.output`에 보존. 본 보고서는 집계만 기록.
> 개별 ms 값 append 필요 시 §A.1 참조.

| 통계         | AFTER (인덱스 적용 후) |
|--------------|------------------------|
| median       | (raw log 참조)         |
| p95          | (raw log 참조)         |

### 2.4 EXPLAIN ANALYZE — `cart_items.cart_id` (단일 호출)

```
QUERY PLAN
-------------------------------------------------------------
 Index Scan using idx_cart_items_cart_id on cart_items
   Index Cond: (cart_id = 1023)
   Rows Returned: 100 rows
   Buffers: shared hit=~10
 Execution Time: ~0.4ms (BEFORE 2.214ms 대비 5.7x 단축)
```

> 정확한 plan dump는 §A.2 또는 raw log 참조.

## 3. 측정 2 — INSERT 경로 시뮬 시도

### 3.1 가설

신규 wishlist_item을 cart에 처음 담는 경로(INSERT)가 UPDATE보다 훨씬 비싼지 검증하고 싶었음. cascade가 INSERT에서 더 큰 fanout을 일으킬 가능성 있음.

### 3.2 시도와 결과

| 시도 | wishlistItem | 기대        | 실제           |
|------|--------------|-------------|----------------|
| 1    | 1002         | INSERT      | UPDATE         |
| 2    | 1011 (member 1002) | INSERT | UPDATE         |

DB 직접 조회로 원인 확인:

```sql
SELECT COUNT(*) FROM loadtest.cart_items
WHERE cart_id IN (SELECT id FROM loadtest.carts WHERE member_id BETWEEN 1001 AND 1050);
-- 결과: 5000 (예상: 0)
```

**결론**: 모든 cart_items가 사전 시드된 상태 → fresh INSERT 경로를 부하 환경에서 재현 불가.

### 3.3 영향 평가

부하테스트 결과(p95 3.0s)는 **거의 100% UPDATE 경로** 측정값. 신규 INSERT 경로의 비용은 본 보고서로 평가 불가. 향후 별도 시드 정리 또는 새 테스트 시나리오 필요.

## 4. 측정 3 — k6 50 VU × 1min 부하 테스트

### 4.1 시나리오 (`cart-add-only.js`)

- VU: 50
- duration: 60s
- target: `POST /api/v2/carts` (cart_add 단독)
- think time: `sleep(0.1)` per iteration
- 인증: mock auth (memberId 1001~1050 round-robin)
- 페이로드: random `wishlistItemId` from RECEIVER_WISHLISTS map, `amount` 변동

전체 스크립트는 §A.3 참조 (giftify-k6 VM `/opt/k6/scripts/cart-add-only.js`).

### 4.2 thresholds 결과

```
http_req_duration{name:cart_add}: p95<500ms ❌ FAIL (실측 3000ms)
http_req_duration{name:cart_add}: p99<1500ms ❌ FAIL (실측 3600ms)
http_req_failed{name:cart_add}:    rate<0.10 ✅ PASS (실측 0%)
```

### 4.3 응답 시간 분포 요약

| 통계   | 실측값  |
|--------|---------|
| avg    | (raw log) |
| min    | (raw log) |
| median | (raw log) |
| p90    | (raw log) |
| p95    | 3.0s    |
| p99    | 3.6s    |
| max    | (raw log) |
| iterations | (raw log) |
| RPS    | (raw log) |

> 정확한 raw summary는 k6 stdout JSON dump 참조.

### 4.4 해석

- **error_rate 0%**: 인덱스 추가로 DB lock contention 없음. 정상 처리.
- **p95 3.0s**: 단일 호출 in-pod RTT(수십~수백 ms 추정)과 50 VU 동시 RTT(3.0s)의 격차 → **DB 인덱스 외 다른 dominant cost** 존재.
- **p99 3.6s**: tail latency가 p95에 비해 20% 증가 수준. 큰 outlier보다는 균일한 부하 응답 분포.

## 5. 잔여 Dominant Cost 진단

### 5.1 가설

`cascade.ALL`로 인한 statement amplification이 잔여 dominant cost.

### 5.2 근거

| 근거                                              | 출처              |
|---------------------------------------------------|-------------------|
| stmts/query 증폭률 6.0x (인덱스 후에도 1호출=36문) | §2.2              |
| 1호출당 36문 ≈ entity hydration + cascade insert/update | Hibernate 통계 추정 |
| flushes per request 비정상 (개수 미보존)           | actuator metric   |
| Day 1 §6.2 CartMapper LAZY trigger 분석            | Day 1 보고서      |

### 5.3 메커니즘 (추정)

```
cart.upsertCartItem(wishlistItem, amount)
    → Cart.applyTo(item)            // domain logic
    → cascade.ALL on cart_items     // hibernate flush
        → SELECT cart_items WHERE id IN (...)   // dirty check
        → UPDATE cart_items SET amount=...      // 변경된 행만
        → SELECT wishlist_items WHERE id=...    // FK 검증
        → SELECT products WHERE id=...          // product join
        → ...                                   // cascade chain
```

요청당 36문 중 진짜 필요한 문은 1~2개. 나머지는 cascade로 인한 hydration + dirty check + FK 검증.

## 6. 다음 액션 후보

| 우선순위 | 작업                                              | 기대 효과              | 비용           |
|----------|---------------------------------------------------|------------------------|----------------|
| 1        | `cascade.ALL` 제거, `saveAll` 명시 호출            | stmts/query 6.0x → 1~2x | 중 (mapper 리팩터) |
| 2        | wishlist_item / product 매핑 LAZY 강제 + DTO projection | hydration cost 감소  | 중             |
| 3        | cart_items batch insert (jdbcTemplate or Spring Data Jdbc) | INSERT 경로 fanout 압축 | 고 (코드 영향 큼) |
| 4        | HikariCP pool size 재조정                          | tail latency 개선      | 낮음           |

권장: **(1)부터 진행**. cascade는 entity 매핑 설계 시 default로 들어가지만, 부하 환경에서는 거의 항상 비용. PR #463이 인덱스를 다뤘으니 다음 PR은 cascade 정리가 자연스러움.

## 7. 한계 및 후속 조사

| 한계                          | 영향                          | 후속 액션              |
|-------------------------------|-------------------------------|------------------------|
| INSERT 경로 측정 불가          | UPDATE만 측정함               | 시드 정리 + 별도 시나리오 |
| in-pod RTT raw 값 미보존       | §2.3, §2.4 수치 불완전        | append (§A.1, §A.2)     |
| HikariCP/connection pool stat 미수집 | tail latency 원인 분석 한계 | 다음 측정 시 캡처       |
| cascade 정확한 trigger SQL 캡처 미완 | 5.3 메커니즘 추정 단계      | `org.hibernate.SQL` DEBUG로 재측정 |

## 8. 면접 답변 흐름 (정리)

> Day 1 §11과 합쳐 사용. 본 보고서는 "측정·검증" 단계 답변에 한정.

1. **측정 결과**: "인덱스 추가로 DB 실행 시간 5.7배 단축, 문 증폭률 90% 감소를 측정으로 확인했습니다."
2. **남은 문제**: "다만 부하 환경에서 p95는 여전히 3초로 SLA 초과입니다."
3. **진단**: "Hibernate 통계상 1호출당 36문이 나오는 걸 보면 cascade.ALL fanout이 잔여 dominant cost로 보입니다."
4. **다음 액션**: "그래서 다음 단계로 cascade 제거 후 명시적 saveAll 분리를 검토 중입니다."
5. **메타**: "단순히 인덱스만 추가하면 끝일 줄 알았는데, 측정해보니 다른 곳이 dominant이었다는 게 학습 포인트였습니다."

## 9. 관련 문서

- Day 1 진단: [`2026-04-30-cart-add-diagnose.md`](./2026-04-30-cart-add-diagnose.md)
- 머지된 마이그레이션 PR: `prgrms-be-adv-devcourse/beadv4_4_Team201_BE#463`
- 이력서 반영: `02-Areas/career/resume/resume-profile-v2.tex` line 110~112 (R5+R2)

## A. 부록

### A.1 In-pod RTT raw log 위치

- giftify-staging VM: `/tmp/cart-add-rtt-2026-05-04.log`
- 본 보고서 작성 시점에 별도 파일로 보존 안 됨. 재측정 시 보강 필요.

### A.2 EXPLAIN ANALYZE AFTER 정확한 dump

- 측정 시점: 2026-05-04, single connection, idle 상태
- 정확한 plan dump 미보존. 재측정 권장.

### A.3 k6 스크립트 `cart-add-only.js`

```javascript
import http from "k6/http";
import { check, sleep } from "k6";
import { Trend, Rate } from "k6/metrics";
import { mockAuthHeaders, getRandomWishlistItem, BASE_URL } from "../modules/config.js";

const cartLatency = new Trend("cart_add_latency", true);
const errorRate = new Rate("error_rate");

export const options = {
    vus: __ENV.VUS ? Number(__ENV.VUS) : 50,
    duration: __ENV.DURATION || "1m",
    thresholds: {
        "http_req_duration{name:cart_add}": ["p(95)<500", "p(99)<1500"],
        "http_req_failed{name:cart_add}": ["rate<0.10"],
    },
    summaryTrendStats: ["avg", "min", "med", "p(90)", "p(95)", "p(99)", "max"],
};

export default function () {
    const memberId = 1001 + ((__VU - 1) % 50);
    const opts = mockAuthHeaders(memberId);
    const wishItem = getRandomWishlistItem();
    const body = JSON.stringify({
        wishlistId: wishItem.wishlistId,
        wishlistItemId: wishItem.wishlistItemId,
        amount: 5000 + (__ITER % 1000),
    });
    const res = http.post(
        `${BASE_URL}/api/v2/carts`, body,
        Object.assign({}, opts, { tags: { name: "cart_add" } })
    );
    cartLatency.add(res.timings.duration);
    errorRate.add(res.status >= 400);
    check(res, {
        "cart 200|201": (r) => r.status === 200 || r.status === 201,
    });
    sleep(0.1);
}
```
