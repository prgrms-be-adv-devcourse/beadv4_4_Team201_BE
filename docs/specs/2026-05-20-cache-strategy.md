# Cache Strategy (MS2 Cycle 1)

본 문서는 Giftify 백엔드의 Redis 캐시 전략을 정의한다. CacheConfig
(`support/common/.../config/CacheConfig.java`) 구현 시점의 규칙을 기준으로
하며, 캐시를 추가/변경하는 모든 작업은 본 문서의 정책을 따른다.

## 1. 전략: Cache-Aside + TTL-based Invalidation

### 1.1 패턴

```
              ┌──────────────┐
              │   Client     │
              └──────┬───────┘
                     │ 1. read
                     ▼
              ┌──────────────┐    miss     ┌──────────────┐
              │ Service Bean │ ──────────► │ Repository   │
              │ @Cacheable   │             │ (DB)         │
              └──────┬───────┘ ◄────────── └──────────────┘
                     │ hit          2b. load + populate
                     ▼
              ┌──────────────┐
              │ Redis Cache  │  TTL expires → eviction
              └──────────────┘
```

- **읽기**: Service 메서드에 `@Cacheable(cacheNames=..., key=...)` 부착.
  hit 시 메서드 본체 우회, miss 시 본체 실행 + 결과 캐시 저장.
- **쓰기**: 도메인 mutation 시 명시적 `@CacheEvict` 또는 TTL 만료에 의존.
- **기본 정책**: *TTL-based eventual consistency*. 즉시 일관성이 필요한
  엔드포인트(주문/결제/잔액)는 캐시 미적용.

### 1.2 적용 도메인

| 도메인 | 캐시 이름 | TTL | 일관성 허용 | 비고 |
|---|---|---|---|---|
| 상품 목록 (검색/카테고리) | `products` | 5 min | eventual | 변경 빈도 낮음 |
| 상품 상세 | `product-detail` | 5 min | eventual | 변경 빈도 낮음 |
| 위시리스트 (개인) | `wishlist` | 1 min | near-real-time | 사용자 mutation 잦음 |
| JWT JWKS | `jwks` | 5 min | eventual | Auth0 rotation 주기 ≫ 5분 |
| (기본값) | * | 10 min | eventual | 명시 안 한 캐시 fallback |

### 1.3 캐시하지 않는 영역

- 주문 (Order), 결제 (Payment), 정산 (Settlement) — *write-heavy &
  consistency-critical*.
- 잔액 (Wallet) — 동일.
- 사용자 인증 토큰 / 블랙리스트 — Cache 추상화 대신 `RedisTemplate` 직접
  사용 (TTL 모델은 같으나 명시적 키 관리 필요).

## 2. 키 네이밍 규칙

### 2.1 Spring Cache 추상화 사용 영역

```
giftify:cache:<cache-name>::<key>
```

- prefix: `giftify:cache:` — `CacheConfig.KEY_PREFIX` 상수.
- cache-name: `products` / `product-detail` / `wishlist` / `jwks`.
- key: `@Cacheable(key=...)` SpEL 결과. 기본 `SimpleKey` 대신 명시 권장.

### 2.2 명시 권장 key 패턴

```
@Cacheable(cacheNames="products", key="#category + ':' + #page + ':' + #size")
```

- 파라미터 조합으로 unique 가능한 key 구성. 충돌 가능성 있는 메서드는
  명시적 key 필수.
- 객체 인자는 hash 사용 금지 (재현성 X). 대신 `id` / 자연 키 직접 참조.

### 2.3 직접 RedisTemplate 사용 영역 (참고)

```
blacklist:token:<jti>             (token blacklist)
idempotency:<scope>:<key>         (idempotency keys)
```

- Cache prefix 와 별개 네임스페이스. CacheConfig 와 충돌 없음.

## 3. Invalidation 정책

### 3.1 TTL 우선 (Default)

- 모든 캐시는 TTL 만료로 자동 무효화.
- 정합성 윈도우 = TTL. 즉, 상품 가격 변경 후 최대 5분간 stale 가능.

### 3.2 명시적 Evict (선택)

다음 상황에서 `@CacheEvict` 사용:

- **사용자 직접 변경**: 본인이 위시리스트에 상품 추가 → 본인 view 는
  TTL 기다림 없이 즉시 갱신해야 UX 자연스러움.
- **관리자 변경**: 상품 가격 / 재고 정정 등 *비즈니스적 일관성 요구*.

```
@CacheEvict(cacheNames="wishlist", key="#memberId")
public void addToWishlist(Long memberId, Long productId) { ... }
```

### 3.3 전체 무효화

`@CacheEvict(cacheNames="products", allEntries=true)` 은 신중히. 대용량
패턴 삭제는 Redis SCAN 비용 발생.

## 4. 운영 고려

### 4.1 직렬화

- Key: `StringRedisSerializer` (사람이 읽을 수 있는 키).
- Value: `GenericJackson2JsonRedisSerializer` — Jackson `@class` 메타 포함
  (LocalDateTime, BigDecimal 등 직렬화 OK).
- **주의**: 도메인 객체 캐시 시 클래스명 변경 = 캐시 호환성 깨짐. 운영
  중 패키지 이동이 필요하면 deploy 전후 캐시 flush 동반.

### 4.2 캐시 null 값

`disableCachingNullValues()` 적용. null 결과 캐시 X. 의도가 "조회 결과
없음" 도 캐시하고 싶다면 `Optional` 래퍼 사용.

### 4.3 메모리 / Eviction

- Redis maxmemory-policy: `allkeys-lru` 권장 (현재 환경별 확인 필요).
- Cycle 1 에선 TTL 만료에 의존. eviction 정책은 Cycle 2 부하 측정 후
  조정.

### 4.4 모니터링

| 지표 | 출처 | 임계 |
|---|---|---|
| Hit ratio | Redis INFO stats / Micrometer | < 70% → 키 설계 재검토 |
| 평균 메모리 사용 | Redis INFO memory | maxmemory 80% → scale |
| stale 응답 사용자 클레임 | 운영 채널 | 발생 시 TTL 축소 검토 |

## 5. 변경 절차

신규 캐시 도입 시:

1. 본 문서 표 2.1 / 표 4 에 캐시 이름 / TTL / 일관성 분류 추가.
2. `CacheConfig.ttlPolicies()` 에 (이름, TTL) 항목 추가.
3. `CacheConfigTest` 에 TTL 검증 케이스 추가.
4. 적용 메서드에 `@Cacheable` 부착 + 명시적 key.
5. invalidation 정책 (TTL only / explicit evict) 명시.
6. Cycle 측정 후 hit ratio 보고서에 반영.

## 6. 관련 코드

| 파일 | 책임 |
|---|---|
| `support/common/.../config/CacheConfig.java` | RedisCacheManager + TTL 정책 |
| `support/common/.../config/RedisConfig.java` | StringRedisTemplate (블랙리스트/멱등성) |
| `support/common/.../config/CacheConfigTest.java` | TTL / prefix / 도메인 등록 검증 |

## 7. Cycle 변경 이력

- 2026-05-20 — 본 문서 초안 + 4개 캐시 (products / product-detail /
  wishlist / jwks). Cycle 1 Before 측정 예정.
