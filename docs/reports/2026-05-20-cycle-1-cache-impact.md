# Cycle 1 — 캐시 도입 영향 측정 (template)

본 보고서는 Spring Cache + Redis 캐시 적용 (Cycle 1) 의 *Before / After*
측정 결과를 채울 자리이다. 측정은 W6 부하 환경 정비 후 수행한다.

## 0. 측정 대상

| 항목 | 적용 캐시 | TTL |
|---|---|---|
| `GET /api/products/{id}` (상품 단건) | `product-detail` | 5 min |
| `GET /api/products?...` (상품 목록) | 미적용 (Cycle 2 검토) | - |
| `GET /api/wishlist/me` | 미적용 (Cycle 2 검토) | - |

## 1. 측정 환경

- **VM**: GCP e2-standard-2 (staging, giftify-vpc/loadtest 10.0.2.3)
- **k6 러너**: GCP e2-medium (10.0.2.2)
- **DB**: Postgres (k3s 내부, single instance, HA 적용 전 = MS3 이전 상태)
- **Cache**: Redis (k3s 내부, single instance)
- **프로파일**: `prod,loadtest`

## 2. 시나리오

```
[k6 scenario] product_detail_read_only
  - VUs: 50
  - duration: 5m (warm-up 30s + measurement 4m30s)
  - request: GET /api/products/{id}
  - id 분포: 100 개 활성 상품에서 uniform random
```

## 3. 측정 지표

| 지표 | 측정 도구 |
|---|---|
| 평균 응답 시간 (ms) | k6 `http_req_duration{avg}` |
| p95 응답 시간 (ms) | k6 `http_req_duration{p95}` |
| p99 응답 시간 (ms) | k6 `http_req_duration{p99}` |
| 처리량 (RPS) | k6 `http_reqs/sec` |
| DB 조회 수 (Hibernate statistics) | Spring Actuator `/actuator/metrics/hibernate.query.executions` |
| 캐시 hit ratio | Redis `INFO stats` (`keyspace_hits / (hits + misses)`) |
| 평균 Redis 메모리 사용 | Redis `INFO memory` (used_memory_human) |
| App CPU / GC | Micrometer / VisualVM |

## 4. Before (캐시 미적용)

> 측정 일자: TBD
> 빌드: TBD (cache 적용 직전 커밋 SHA)

| 지표 | 값 |
|---|---|
| 평균 응답 시간 | _measurement TBD_ |
| p95 응답 시간 | _measurement TBD_ |
| p99 응답 시간 | _measurement TBD_ |
| RPS | _measurement TBD_ |
| 분당 DB 조회 수 | _measurement TBD_ |

## 5. After (캐시 적용 — product-detail, TTL 5분)

> 측정 일자: TBD
> 빌드: TBD (`feat: ProductService 단건 조회 캐시 적용` 커밋 698c53ad 이후)

| 지표 | 값 |
|---|---|
| 평균 응답 시간 | _measurement TBD_ |
| p95 응답 시간 | _measurement TBD_ |
| p99 응답 시간 | _measurement TBD_ |
| RPS | _measurement TBD_ |
| 분당 DB 조회 수 | _measurement TBD_ |
| 캐시 hit ratio | _measurement TBD_ |
| Redis 메모리 | _measurement TBD_ |

## 6. 분석 (작성 예정)

1. 응답 시간 개선폭 ms 와 % (특히 p95, p99 의 변화 — long tail 의 변화가
   주된 가치).
2. DB 호출 감소율 (hit ratio 와 일치해야 함; 어긋나면 키 충돌 의심).
3. 메모리 압박 추세 (캐시 항목 수 vs Redis maxmemory 비율).
4. *예상 못한 부작용*: stale 응답 사용자 클레임, eviction 폭주, GC 변화.

## 7. Cycle 2 판단 (작성 예정)

- products / wishlist 캐시 추가 적용 여부.
- TTL 조정 필요성.
- 명시적 evict 정책 추가 (현재는 mutation 메서드 단건 evict 만 적용).

## 8. 관련 커밋 / 문서

- `392808d8` — feat: Spring Cache + RedisCacheManager 도입 (도메인별 TTL)
- `41f2cb50` — docs: 캐시 전략 명세 추가
- `698c53ad` — feat: ProductService 단건 조회 캐시 적용
- spec: `docs/specs/2026-05-20-cache-strategy.md`
