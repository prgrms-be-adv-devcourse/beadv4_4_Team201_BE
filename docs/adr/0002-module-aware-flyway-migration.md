# ADR-0002: Module-Aware Flyway Migration - Spring Modulith 기반 모듈별 독립 마이그레이션

**상태:** 승인됨 (Accepted)
**날짜:** 2026-03-10
**의사결정자:** 팀 201
**선행 결정:** ADR-0001 (Modular Monolith 아키텍처)

---

## 컨텍스트

### 문제 상황

Giftify 백엔드는 ADR-0001에서 결정한 모듈러 모놀리스 아키텍처를 채택하고 Spring Modulith를 도입했다.
그러나 Flyway 마이그레이션은 여전히 단일 디렉토리(`db/migration/`)에 23개 파일이 혼재하는 구조였다.

이로 인해 다음과 같은 문제가 반복적으로 발생했다:

1. **팀원 간 마이그레이션 충돌**: 여러 사람이 동시에 마이그레이션 파일을 추가하면 버전 번호가 충돌하거나,
   서로 다른 브랜치에서 작성한 DDL이 merge 시 꼬이는 문제가 빈번했다.

2. **소유권 불명확**: `V15__add_settlement_column.sql` 같은 파일이 어떤 모듈 소속인지
   파일명만으로 판단하기 어려웠다. 23개 파일이 시간순으로 쌓이면서 특정 테이블의 현재 상태를
   파악하려면 여러 파일을 추적해야 했다.

3. **Spring Modulith 미활용**: ADR-0001에서 Spring Modulith를 도입한 핵심 이유 중 하나는
   모듈 간 경계의 명확화였다. 그런데 DB 스키마 관리는 모듈 경계와 무관하게 단일 디렉토리에서
   전역적으로 이루어지고 있었다. 코드는 모듈별로 분리했지만 DDL은 분리하지 않은 반쪽짜리 모듈화였다.

4. **테스트 격리 불가**: `@ApplicationModuleTest`로 특정 모듈만 테스트할 때,
   해당 모듈과 무관한 테이블의 DDL까지 모두 실행되었다.

### 결정이 필요한 사항

1. **마이그레이션 분리 전략**: 모듈별로 어떻게 분리할 것인가?
2. **인프라 테이블 관리**: Spring Batch, Event Publication 등 특정 모듈에 속하지 않는 테이블은?
3. **기존 23개 마이그레이션 처리**: 기존 history를 유지할 것인가, 재출발할 것인가?
4. **테이블 네이밍 컨벤션**: 분리와 동시에 일관성 있는 네이밍 기준을 정할 것인가?
5. **Seed Data 관리**: Dev/Staging 환경에 필요한 테스트 데이터를 어떻게 주입하고 관리할 것인가?

---

## 고려한 옵션

### Option 1: 수동 Flyway 멀티 인스턴스

각 모듈별로 Flyway Bean을 수동으로 생성하고, `locations`를 모듈별 디렉토리로 지정한다.

```java
@Bean
public Flyway memberFlyway(DataSource ds) {
    return Flyway.configure()
        .dataSource(ds)
        .locations("classpath:db/migration/member")
        .table("flyway_schema_history_member")
        .load();
}
```

**장점:**
- Spring Modulith 없이도 동작
- 세밀한 제어 가능

**단점:**
- 모듈 추가 시마다 Bean 수동 등록 필요
- 모듈 의존성 순서를 개발자가 직접 관리
- Spring Modulith와의 통합 부재 (`@ApplicationModuleTest` 격리 불가)

### Option 2: Spring Modulith Flyway Integration (채택)

Spring Modulith 2.0의 `SpringModulithFlywayMigrationStrategy`를 활용한다.
`spring.modulith.runtime.flyway-enabled=true` 한 줄로 모듈별 독립 Flyway가 자동 구성된다.

**장점:**
- 프로젝트가 이미 Spring Modulith 사용 — 추가 의존성 불필요
- Application Module 자동 감지 → `db/migration/{module}/` 디렉토리 자동 매핑
- 모듈 의존성 그래프 기반 실행 순서 자동 추론
- `@ApplicationModuleTest` 시 해당 모듈 + 의존 모듈 마이그레이션만 실행
- `__root` 예약 디렉토리로 인프라 테이블 우선 실행 보장

**단점:**
- Spring Modulith에 대한 추가 결합
- 모듈이 많을수록 DB 커넥션 순간 사용량 증가

### Option 3: Flyway + Custom Resolver

Flyway의 `MigrationResolver`를 커스텀 구현하여 디렉토리 기반 분리를 처리한다.

**장점:**
- 프레임워크 독립적

**단점:**
- 구현 복잡도 높음
- Spring Modulith의 모듈 감지 로직과 별도 유지보수 필요
- 투자 대비 Option 2와 결과 동일

---

## 결정

### 1. Spring Modulith Flyway Integration 채택

**Option 2를 채택** 근거:

- 이미 Spring Modulith를 도입한 프로젝트에서 공식 솔루션을 사용하지 않을 이유가 없다.
  수동 Bean 등록(Option 1)이나 커스텀 구현(Option 3)은 Spring Modulith가 이미 해결한 문제를 다시 만드는 것이다.
- Property 한 줄(`spring.modulith.runtime.flyway-enabled=true`)로 활성화되므로 설정 부담이 최소화된다.
- 모듈 의존성 그래프 기반 자동 실행 순서는 수동 관리에 비해 오류 가능성이 현저히 낮다.

### 2. 기존 마이그레이션 전략: Consolidation + 재출발

기존 23개 마이그레이션 파일을 유지하지 않고, **모듈별 `V1.0.0__init.sql`로 통합 재출발**한다.

| 항목 | 결정 |
|------|------|
| 기존 23개 파일 | 삭제 (별도 보관) |
| 새 버전 체계 | 모듈별 `V1.0.0`부터 독립 시작 |
| History 테이블 | 모듈별 `flyway_schema_history_{module}` 신규 생성 |
| Dev/Prod DB | DB 재생성 필요 (기존 history 무효화) |

**근거:**
- 기존 23개는 시간순 누적이라 모듈별로 분류하기 어려움
- 이미 운영 데이터가 없는 프로젝트 초기 단계이므로 재출발 비용이 낮음
- 모듈별 init으로 통합하면 각 모듈의 현재 스키마 상태를 단일 파일로 파악 가능

### 3. 인프라 테이블: `__root` 디렉토리

Spring Batch 메타 테이블(6개 + 시퀀스)과 Event Publication 테이블(2개)은
특정 비즈니스 모듈에 속하지 않으므로 `__root` 디렉토리에 배치한다.

`__root`는 Spring Modulith의 공식 예약 디렉토리로, **모든 모듈보다 먼저 실행**이 보장된다.

### 4. 테이블 네이밍 컨벤션 통일

마이그레이션 분리와 동시에 테이블 네이밍 컨벤션을 통일했다:

| 구분 | 규칙 | 예시 |
|------|------|------|
| 비즈니스 도메인 | 복수형 | `members`, `orders`, `payments` |
| 프레임워크/인프라 | 단수형 | `BATCH_JOB_INSTANCE`, `EVENT_PUBLICATION` |

**근거 — Spring Framework 팀의 공식 프로젝트 패턴:**

| 프로젝트 | 테이블명 | 규칙 |
|----------|----------|------|
| Spring PetClinic | `owners`, `pets`, `visits`, `vets` | 복수형 |
| Spring Security | `users`, `authorities` | 복수형 |
| Spring Batch | `BATCH_JOB_INSTANCE`, `BATCH_STEP_EXECUTION` | 단수형 (메타데이터) |
| Spring Session | `SPRING_SESSION` | 단수형 (인프라) |
| Spring Modulith | `EVENT_PUBLICATION` | 단수형 (인프라) |

프레임워크 팀이 비즈니스 테이블(PetClinic, Security)에는 복수형을,
인프라 메타데이터 테이블(Batch, Session, Modulith)에는 단수형을 사용하는 패턴이 일관된다.
이 패턴을 그대로 따른다.

### 5. Seed Data: 모듈별 Repeatable Migration (`R__seed.sql`)

Dev/Staging 환경의 테스트 데이터를 Flyway의 Repeatable migration으로 관리한다.
각 모듈 디렉토리에 `R__seed.sql`을 배치하여, 앱 기동 시 자동으로 seed data가 주입된다.

**고려한 대안과 기각 사유:**

| 대안 | 기각 사유 |
|------|----------|
| 수동 SQL 스크립트 (`psql -f seed.sql`) | 팀원이 DB 초기화 후 별도로 실행해야 함. 잊으면 빈 DB로 개발 |
| `__root/R__seed.sql` 단일 파일 | `__root`는 모든 모듈보다 먼저 실행 → 모듈 테이블 생성 전에 INSERT 시도하여 실패 |
| `V1.1.0__seed.sql` 버전 마이그레이션 | 한 번만 실행되고 수정 불가. seed 데이터 변경 시 새 버전 파일을 계속 추가해야 함 |
| `ApplicationRunner` Java 코드 | seed 데이터가 SQL이 아닌 Java에 흩어짐. 구현 비용 대비 이점 없음 |

**R__seed.sql을 채택한 근거:**

- Flyway의 Repeatable migration은 **체크섬 기반 재실행** — seed 데이터 수정 시 다음 기동에서 자동 반영
- Spring Modulith가 각 모듈별로 독립된 Flyway 인스턴스를 생성하므로,
  모듈 디렉토리의 `R__seed.sql`은 해당 모듈의 `V__` 마이그레이션 이후에 실행된다
- 모듈 의존성 순서 덕분에 cross-module FK 참조도 자연스럽게 해결된다
  (예: member seed → product seed 순서로 실행, product의 `member_replicas`가 member 이후에 INSERT)

**주의사항:**

- Spring Modulith 공식 문서에서 R__ prefix를 명시적으로 언급하지는 않는다.
  그러나 Spring Modulith의 module-aware 전략은 각 모듈별로 표준 Flyway 인스턴스를 생성하는 방식이므로,
  Flyway 자체가 지원하는 R__ prefix가 자연스럽게 동작한다.
- 실제 기동 테스트에서 모듈별 `flyway_schema_history_{module}` 테이블에
  R__seed.sql의 체크섬이 정상 기록되는 것을 확인했다.

---

## 구현 상세

### 디렉토리 구조

```
bootstrap/api-server/src/main/resources/db/migration/
├── __root/                           # 인프라 (항상 최우선)
│   ├── V1.0.0__spring_batch.sql
│   └── V1.1.0__event_publication.sql
├── member/
│   ├── V1.0.0__init.sql              # members
│   └── R__seed.sql                   # dev/staging seed data
├── friendship/
│   ├── V1.0.0__init.sql              # friendships
│   └── R__seed.sql
├── product/
│   ├── V1.0.0__init.sql              # products, member_replicas
│   └── R__seed.sql
├── cart/
│   ├── V1.0.0__init.sql              # carts, cart_items
│   └── R__seed.sql
├── wishlist/
│   ├── V1.0.0__init.sql              # wishlists, wishlist_items
│   └── R__seed.sql
├── funding/
│   ├── V1.0.0__init.sql              # fundings, funding_participant_members
│   └── R__seed.sql
├── order/
│   ├── V1.0.0__init.sql              # orders, order_items, core_member_replicas
│   └── R__seed.sql
├── payment/
│   ├── V1.0.0__init.sql              # payments, payment_histories, payment_cancels
│   └── R__seed.sql
├── wallet/
│   ├── V1.0.0__init.sql              # wallets, wallet_histories
│   └── R__seed.sql
├── settlement/
│   └── V1.0.0__init.sql              # settlement_items, settlement_queues, settlement_histories
└── notification/
    └── V1.0.0__init.sql              # notifications
```

### 환경별 설정

```
             Local (H2)           Dev (PostgreSQL)     Prod (PostgreSQL)
-----------  -------------------  ------------------   ------------------
DDL 관리     ddl-auto=create      Flyway module-aware  Flyway module-aware
Flyway       OFF                  ON (Modulith)        ON (Modulith)
Seed         data-local.sql       R__seed.sql (자동)   R__seed.sql (자동)
DB 초기화    자동 (매 실행)        docker volume 재생성  rolling migration
```

### 설정 변경 (dev/prod)

```yaml
spring:
  modulith:
    runtime:
      flyway-enabled: true        # 모듈별 Flyway 활성화

  flyway:
    enabled: true
    baseline-on-migrate: true
    baseline-version: "0"
    schemas: g7app
    default-schema: g7app
    create-schemas: true
    # locations 제거 - Spring Modulith가 자동 관리
    # out-of-order 제거 - 모듈별 독립 버전이므로 불필요
```

---

## 구현 과정에서 발견한 문제와 해결

### 1. 마이그레이션 SQL과 JPA Entity 간 정합성 불일치

모듈별 `V1.0.0__init.sql`을 생성한 후, 기존 마이그레이션 22개 및 JPA Entity와 대조 검증을 수행했다.

| 발견된 불일치 | 모듈 | 해결 |
|--------------|------|------|
| `seller_id` 인덱스 누락 | settlement | `idx_settlement_item_seller_id` 추가 |
| `history_id` 인덱스 누락 | settlement | `idx_settlement_item_history_id` 추가 |
| `status + created_at` 복합 인덱스 누락 | order | `idx_order_status_created_at` 추가 |
| `member_id` UNIQUE 제약조건 누락 | wishlist | DDL에 `unique` 추가 |
| `SettlementHistory`에 명시적 `@Table(name)` 없음 | settlement | `@Table(name = "settlement_histories")` 추가 |

**교훈:** Hibernate `ddl-auto=create`가 생성하는 스키마와 수동 작성한 마이그레이션 SQL은
시간이 지나면서 자연스럽게 drift가 발생한다. 마이그레이션 전환 시점에 전수 대조 검증이 필수적이다.

### 2. 테이블명 복수형 전환 시 연쇄 수정

테이블명을 복수형으로 통일하면서 다음 영역 모두를 동기화해야 했다:

- Flyway 마이그레이션 SQL 8개 (DDL, FK 참조, 인덱스명)
- JPA Entity `@Table(name)` 17개
- Native query SQL 문자열 2개 (settlement 모듈)
- Seed data (`data-local.sql`) INSERT/ALTER 문 24개+

특히 Native query에 하드코딩된 테이블명(`FROM settlement_item`, `TRUNCATE TABLE settlement_queue`)을
놓치기 쉬운데, Grep 기반 전수 검색으로 누락을 방지했다.

### 3. Spring Modulith의 모듈 자동 감지 범위

설계 시점에는 마이그레이션이 필요한 11개 모듈만 고려했으나,
실제로 Spring Modulith는 `app.giftify` 하위의 **모든 패키지**를 Application Module로 감지한다.

결과적으로 auth, facade, health, replica, security, shared, support 등
SQL 파일이 없는 모듈에 대해서도 빈 `flyway_schema_history_{module}` 테이블이 baseline으로 생성되었다.

이는 정상 동작이며, 향후 해당 모듈에 DDL 변경이 생기면 자연스럽게 활용된다.

---

## 팀 운영 영향

### Dev DB 초기화 절차 (전 팀원 필수)

Module-aware Flyway 전환 후 기존 DB는 호환되지 않는다. 전 팀원이 다음을 실행해야 한다:

```bash
docker rm -f giftify-postgres
docker volume rm giftify-be_postgres_data
docker compose up -d postgres
```

### 향후 마이그레이션 추가 방법

새 마이그레이션은 해당 모듈 디렉토리에 추가한다:

```
db/migration/{module}/V1.1.0__add_column.sql
```

- 버전 번호는 **모듈 내에서만 순차적**이면 된다 (다른 모듈과 독립)
- 팀원 간 버전 충돌 범위가 해당 모듈로 한정되므로 충돌 확률이 대폭 감소한다

### 새 모듈 추가 시

1. `db/migration/{module}/` 디렉토리 생성
2. `V1.0.0__init.sql` 작성
3. 별도 설정 불필요 — Spring Modulith가 자동 감지

---

## 검증 결과

### Build + Test

- `./gradlew clean build` — BUILD SUCCESSFUL (80 tasks, 1m 8s)
- 전체 테스트 통과

### Dev PostgreSQL 기동

- `__root`: `V1.0.0 - spring batch`, `V1.1.0 - event publication` 정상 적용
- 마이그레이션 있는 11개 모듈: 각 `V1.0.0 - init` 정상 적용
- 마이그레이션 없는 모듈: baseline만 생성
- Hibernate `validate` 통과
- `Started GiftifyApplication in 10.888 seconds`
- Spring Batch `expirationJob` 정상 실행 (COMPLETED)

### R__seed.sql 동작 확인

- 9개 모듈(member, friendship, product, cart, wishlist, funding, order, payment, wallet)의 `R__seed.sql` 정상 실행
- 각 모듈별 `flyway_schema_history_{module}` 테이블에 체크섬 기록 확인
- 실행 순서: 모듈별 `V1.0.0__init.sql` → `R__seed.sql` (Flyway 표준 순서 준수)
- 모듈 의존성 순서로 seed 실행 → cross-module FK 참조 정상 해소

---

## 리스크 관리

| Risk | Mitigation | 상태 |
|------|------------|------|
| FlywayValidateException (Issue #1440) | Spring Modulith 2.0.3에서 검증 → 문제 없음 | 해소 |
| Connection pool 순간 사용량 증가 (17개 모듈) | HikariCP 기본값(10)으로 충분. 필요 시 증설 | 모니터링 |
| 기존 Dev DB 호환 불가 | docker volume 재생성 절차 문서화 | 해소 |
| 모듈 의존성 순서 오류 | 기동 로그에서 실행 순서 확인 완료 | 해소 |

---

## 관련 문서

- [ADR-0001: Modular Monolith 아키텍처](./0001-modular-monolith-architecture.md)
- [Flyway Module-Aware Design (상세 설계)](../plans/2026-03-10-flyway-module-aware-design.md)
- [Flyway Migration Consolidation Design (초기 설계)](../plans/2026-02-23-flyway-migration-consolidation-design.md)
- [Spring Modulith Runtime - Flyway Integration](https://docs.spring.io/spring-modulith/reference/runtime.html)

---

## 변경 이력

| 버전 | 날짜 | 작성자 | 변경 내용 |
|:-----|:-----|:------|:---------|
| 1.0 | 2026-03-10 | 팀 201 | 초안 작성 — 설계, 구현, 검증 완료 |
| 1.1 | 2026-03-11 | 팀 201 | Seed Data 전략 추가 — 모듈별 R__seed.sql Repeatable migration 채택 |
