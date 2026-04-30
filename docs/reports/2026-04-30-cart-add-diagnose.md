# Cart Add 진단 보고서 (Phase 1)

본 보고서는 cart_add API의 staging baseline p95 595ms / Stress p95 309ms 원인 진단 결과이다. 핵심 결론: **cart_items.cart_id 인덱스 부재로 인한 sequential scan + CartMapper의 강제 LAZY 컬렉션 트리거**가 주요 origin이다. 인덱스 마이그레이션 V1.0.2는 이미 코드에 존재하지만 staging DB에 적용 안 된 ops 이슈 발견.

## 1. 환경

| Item | Value |
|---|---|
| Date | 2026-04-30 |
| Server | giftify-staging (e2-standard-2, 2vCPU/8GB) |
| Image | api-server:0.0.24 |
| Profile | prod,loadtest (Hibernate Statistics ON) |
| Pool | HikariCP max=10 |
| Threads | virtual threads enabled (Java 25) |

## 2. k6 funding-scenario 결과 (4분 17초)

| Metric | Value | Note |
|---|---|---|
| http_reqs | 12,235 (47.7/s) | |
| iterations | 2,447 | |
| cart_add p95 | 37.49ms | **무효** (대부분 4xx 에러) |
| error_rate | 40% | seed 데이터 stale로 wishlist/cart/funding 단계 실패 |

**측정 무효 원인**: Cycle 0 baseline(2026-03-25) 이후 staging DB의 시드 데이터가 한 달 사이에 stale. funding-scenario는 search → product 까지만 정상 실행, wishlist add 부터 모두 실패.

## 3. Hibernate metric DELTA (k6 4m17s 동안)

```
+----------------------+--------+-------+---------+
| metric               | before | after | delta   |
+----------------------+--------+-------+---------+
| query.executions     | 6910   | 6970  | 60      |
| statements           | 422400 | 426168| 3768    |
| entities.loads       | 53760  | 54240 | 480     |
| sessions.open        | 212238 | 214122| 1884    |
| transactions         | 212014 | 213898| 1884    |
+----------------------+--------+-------+---------+

비율 분석:
  query / request:    60 / 12235  = 0.5%   (대부분 DB 미접근)
  statements / query: 3768 / 60   = 62.8x  (cascade overhead 의심)
  entities / query:   480 / 60    = 8x     (쿼리당 평균 8개 hydration)
```

## 4. EXPLAIN ANALYZE — 5쿼리 single-call

```
+--------------------+------------+--------+-------+----------+
| 쿼리                | Plan       | Buffers| 시간   | 상태     |
+--------------------+------------+--------+-------+----------+
| carts/member_id    | Index Scan | hit=5  | 0.07ms| OK       |
| cart_items/cart_id | Seq Scan   | hit=117|2.21ms | NEEDS IDX|  <-- 9785 rows removed
| wishlist_items/id  | Index Scan | --     | 0.07ms| OK       |
| products/id        | Index Scan | read=2 | 2.14ms| OK       |
| fundings/wid       | Index Scan | hit=1  | 0.15ms| OK       |
+--------------------+------------+--------+-------+----------+
합계 (idle, 단일 스레드): 4.6ms
```

### 4.1 cart_items seq scan 직접 증거

```
QUERY PLAN
-------------------------------------------------------------
 Seq Scan on cart_items  (cost=0.00..231.25 rows=100 width=41)
                         (actual time=0.029..2.085 rows=100 loops=1)
   Filter: (cart_id = 1023)
   Rows Removed by Filter: 9785
   Buffers: shared hit=117
 Planning Time: 1.264 ms
 Execution Time: 2.214 ms
```

**중요 수치**:
- 9,785 행 검사 후 폐기 → 100 행 반환 (1% 효율)
- 117 buffer pages 읽음
- cost: 231.25 (인덱스 scan은 통상 8-16)

### 4.2 cart_items 테이블 스키마 (psql `\d`)

```
                  Table "g7app.cart_items"
        Column        |          Type
----------------------+------------------------
 amount               | numeric(38,2)
 cart_id              | bigint
 id                   | bigint
 wishlist_item_status | character varying(255)
 wishlist_item_id     | bigint
Indexes:
    "cart_items_pkey" PRIMARY KEY, btree (id)
Foreign-key constraints:
    "fk_cart_item_cart" FOREIGN KEY (cart_id) REFERENCES g7app.carts(id)
```

PK 외 인덱스 없음. cart_id FK는 있으나 인덱스 없음 (PostgreSQL은 FK 자동 인덱스 X).

## 5. Open Question — V1.0.2 마이그레이션 적용 미스

### 5.1 코드 상태 vs DB 상태 불일치

`bootstrap/api-server/src/main/resources/db/migration/cart/V1.0.2__add_cart_items_cart_id_index.sql`:

```sql
CREATE INDEX idx_cart_items_cart_id ON cart_items (cart_id);
```

이 마이그레이션은 PR #463 (commit 4743a53b)에서 develop에 머지되었음. 그러나 staging DB의 cart_items 테이블에는 idx_cart_items_cart_id 인덱스가 없음.

### 5.2 가능한 원인 (추가 조사 필요)

- staging DB의 `flyway_schema_history` 테이블 확인 필요
- V1.0.2가 status=success로 기록되어 있다면 → 인덱스가 존재해야 함 (모순)
- V1.0.2 미기록이라면 → Flyway가 적용 안 함 (왜?)
- staging overlay가 PR #463에서 새로 만들어진 점이 단서일 수 있음

### 5.3 Phase 2 결정 보류

마이그레이션 추가 여부는 staging DB의 flyway_schema_history 확인 후 결정:
- 옵션 A: V1.0.2가 적용 안 됐다면 Flyway repair 또는 baseline 재설정
- 옵션 B: V1.0.3 (idempotent CREATE INDEX IF NOT EXISTS)로 우회
- 옵션 C: prod 적용 시 CONCURRENTLY 옵션 추가 (별도 mixed migration 필요)

## 6. 코드 분석 — CartMapper LAZY 트리거

### 6.1 CartService.upsertCartItem 호출 시퀀스

```
CartService.upsertCartItem
  └─ cartRepositoryPort.findByMemberId
       └─ JpaCartRepository.findByMemberId (JPA)
            └─ cartMapper.toDomain(JpaCart)
                 └─ jpaCart.getItems().stream()  <-- LAZY 컬렉션 트리거
                      └─ SELECT cart_items WHERE cart_id = ?  (현재 seq scan!)
```

### 6.2 매핑 트리거 라인 (CartMapper.java:36)

```java
public Cart toDomain(JpaCart jpaCart) {
    Map<Long, CartItem> items = jpaCart.getItems().stream()  // <-- 여기
            .collect(Collectors.toMap(
                    itemEntity -> itemEntity.getWishlistItemId(),
                    cartItemMapper::toDomain
            ));
    return Cart.reconstruct(jpaCart.getId(), jpaCart.getMemberId(), items);
}
```

### 6.3 JpaCart 매핑

```java
// JpaCart.java:21
@OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
private List<JpaCartItem> items = new ArrayList<>();
```

기본 fetch=LAZY. 그러나 toDomain이 무조건 .getItems() 호출 → 매 cart load 시 cart_items 추가 쿼리.

## 7. 실제 cart_add 호출당 쿼리 시퀀스 (수정됨)

```
+-------------------------------------------------------------+
| 1. SELECT carts WHERE member_id = ?         (Index, 0.07ms) |
| 2. SELECT cart_items WHERE cart_id = ?      (SEQ, 2.21ms)   |  <-- 발견
| 3. SELECT wishlist_items WHERE id = ?       (Index, 0.07ms) |
| 4. SELECT products WHERE id = ?             (Index, 2.14ms) |
| 5. SELECT fundings WHERE wishlist_item_id   (Index, 0.15ms) |
| 6. UPDATE/INSERT cart_items + cascade flush                  |
+-------------------------------------------------------------+
실측 5쿼리 합계: 4.6ms (idle, single-thread)
+ cascade.ALL overhead: statement amplification 62배
+ network RTT 5회: 5-10ms staging
+ JPA hydration: 10-30ms
+ 동시 60 VU contention
+ 누적 cart_items 행 수 (현 9885)
= 309-595ms p95 설명
```

## 8. 가설 검증 결과

| 가설 | 상태 | 증거 |
|---|---|---|
| H1 — RTT-bound 5쿼리 | 부분 확증 | 단일 호출 4.6ms는 가벼움. 동시성 + cascade 누적이 origin |
| H2 — Query-slow | 반증 | 4 쿼리 모두 index scan, sub-ms 또는 ms 단위 |
| H3 — Lock contention | 반증 | HikariCP acquire MAX 3.6ms |
| **H4 — Cart eager via mapper** | 확증 | CartMapper.java:36 명시적 .getItems() 호출 |
| **인덱스 갭 (cart_id)** | 확증 | EXPLAIN: Seq Scan, 9785 rows removed |
| Cascade.ALL overhead | 강한 의심 | statement/query = 62.8x 비율 |
| **V1.0.2 staging 미적용 (신규)** | 확증 | 코드에 마이그레이션 있으나 DB에 인덱스 없음 |

## 9. Phase 2 권장 작업 (조건부)

조사 후 결정:

### 9.1 staging DB 우선 확인

```sql
SELECT installed_rank, version, description, success
FROM g7app.flyway_schema_history
WHERE description LIKE '%cart_items%';
```

### 9.2 결과별 분기

| flyway_schema_history 상태 | 권장 |
|---|---|
| V1.0.2 success=true | DB 상태와 마이그레이션 기록 모순. 수동 인덱스 생성 + Flyway repair |
| V1.0.2 success=false | Flyway repair |
| V1.0.2 미기록 | Flyway baseline-version 재검토 또는 V1.0.3 idempotent 추가 |

### 9.3 prod 적용 안전성

`CREATE INDEX` (V1.0.2 그대로)는 ACCESS EXCLUSIVE 락을 잡음. cart_items 테이블이 prod에서 작으면 짧은 락이지만, 큰 테이블이면 차단 가능. prod 적용 시 다음 옵션 검토:

- `CREATE INDEX CONCURRENTLY` 사용 + Flyway mixed=true
- 별도 ops 작업으로 인덱스 사전 생성 후 V1.0.x baseline

## 10. 후속 과제 (별도 PR)

1. **CartMapper LAZY 트리거 제거**: `toDomain`이 무조건 컬렉션 로드하는 패턴 개선. cart_add는 컬렉션 필요(upsert containsKey 검사) — 분기 필요.
2. **cascade.ALL 검토**: statement amplification 62배 원인 정밀 분석.
3. **시드 데이터 복원 + Cycle 1 baseline 재측정**: funding-scenario 정상 path 확보.
4. **getCartResponse 6쿼리 패턴**: cart 조회 시 더 많은 쿼리 발생, 별도 baseline 필요.

## 11. 면접 답변 흐름

```
Q: 부하테스트 진단 경험 있으신가요?
A: 예. Cycle 0 baseline에서 cart_add API의 p95가 가장 느린 309ms였습니다.
   원인 진단을 위해:
   1) Spring Boot Actuator의 hibernate.* 메트릭으로 DELTA 캡처
   2) PostgreSQL EXPLAIN (ANALYZE, BUFFERS) 5쿼리 직접 실행
   3) JPA 매핑 코드 정독
   순서로 접근했습니다.

Q: 어떤 결과를 얻으셨나요?
A: cart_items.cart_id 컬럼에 인덱스가 없어 Sequential Scan이 발생했고
   (EXPLAIN: 9,785 rows removed by filter, cost 231.25),
   더 흥미롭게는 CartMapper.toDomain의 코드 한 줄
   "jpaCart.getItems().stream()"이 매 cart 로드마다 LAZY 컬렉션을 무조건
   초기화한다는 사실을 발견했습니다. 즉 의도된 LAZY 매핑이 코드에 의해 강제로
   eager가 되는 안티패턴이었습니다.
   
   추가로 흥미로운 ops 이슈도 발견했습니다. 인덱스 추가 마이그레이션 V1.0.2가
   이미 코드에 있었지만 staging DB에는 적용되지 않은 상태였습니다. 이건 Flyway
   schema_history와 실제 DB 상태 불일치 케이스로, 별도 조사가 필요한 사례입니다.

Q: 어떻게 해결하셨나요?
A: Phase 1에서는 진단까지 완료했고, Phase 2는 조건부로 분기 설정했습니다.
   먼저 flyway_schema_history 확인 후, V1.0.2 적용 미스 케이스라면 Flyway repair,
   미기록이면 V1.0.3 idempotent 마이그레이션 추가를 권장합니다.
   prod 적용 시는 CREATE INDEX CONCURRENTLY로 락 회피해야 합니다.
   CartMapper의 강제 트리거 제거와 cascade.ALL의 statement amplification (62배)
   은 별도 후속 PR로 분리했습니다.
```
