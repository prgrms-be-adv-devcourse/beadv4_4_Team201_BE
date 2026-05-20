# Module Flattening Design

> 작성: 2026-05-21 / MS4 W10 T10.24-T10.29 의 입력
> 사전 분석: `docs/specs/2026-05-20-T10.24-flatten-pre-analysis.md`
> 결정 입력 (Saga): `docs/specs/2026-05-21-saga-pattern-decision.md`

## 0. 결정 요약

| 항목 | 결정 |
|-----|------|
| Range | R3 (facade + replica 둘 다) |
| Depth | D4 ((C) + replica → readmodel 재정의) |
| 실행 전략 | 전략 1 (Big-Bang 단일 PR) |
| 작업 워크트리 | `.worktrees/staging/flattening` (브랜치 `staging/module-flattening`) |
| Base | `staging/post-deploy-cleanup` HEAD (`ddad2dbe`) |
| 최종 머지 대상 | `develop` (cherry-pick 워크트리의 35 commit 머지 이후) |
| 보상 이벤트 라인 (E) | 본 spec 범위 외 — MS4 W12 와 통합 |

## 1. 목적

1. **논리 모듈 = 디렉터리 = Modulith 인식 단위** 세 가지를 정렬한다.
2. `CoreFacade` 의 *분산 트랜잭션 코디네이터* 책임을 유스케이스로 옮기고
   해당 클래스를 제거한다.
3. `bc/catalog/replica/member` 를 *consumer 도메인 언어* 의 read-model
   (`CatalogSellerView` 또는 동등) 로 재정의한다.
4. 분리 서버 (settlement-server, payment-server) 도출 시 코드 이동 단위가
   명확해진다.

## 2. 현재 구조의 문제

```
bc/catalog/   ← cart + product + wishlist + image + replica  (5 개 논리 모듈을 묶음)
bc/core/      ← facade + order + payment + funding + wallet (+ shared)
```

`bc/catalog`, `bc/core` 는 Gradle 모듈 단위로 묶인 *인공 경계* 이며
Bounded Context 가 아니다. `ApplicationModules.verify()` 가 인식하는 *논리
모듈* 과 어긋나 검증이 헐거워진다.

추가로 두 가지 안티패턴이 존재한다:

- **`bc/core/facade/CoreFacade`** — `@Transactional` 을 갖고 Order, Payment,
  Funding 세 모듈을 한 트랜잭션으로 묶는다. 이름은 facade 지만 본질은 분산
  트랜잭션 코디네이터.
- **`bc/catalog/replica/member`** — 방향 (이벤트로 동기화) 은 옳으나 *패키지
  이름* 과 *모양* 이 잘못. 의미적으로 `CatalogSellerView` 인데 "replica" 라는
  이름이 entity drift 를 유발한다.

## 3. 타깃 구조 — 원칙

### 3.1 모듈 디렉터리 평탄화

**디렉터리 컨벤션**: `bc/` 컨테이너 디렉터리를 *유지* 하고, 그 안의 *멀티 도메인
컨테이너* 인 `bc/catalog`, `bc/core` 만 해체한다. settlement / notification /
member / shared 처럼 *이미 평탄한 단일 도메인 모듈* 은 변경하지 않는다.

**Spring Modulith 인식 단위**: Modulith 는 *Java package* (`app.giftify.<module>`)
기준으로 모듈을 인식한다. Gradle 디렉터리 path (`bc/cart`) 와 무관. 단 한
Modulith 모듈의 클래스는 *같은 Gradle 모듈* 안에 있어야 `verify()` 가
classpath 를 정상 인식한다 (옵션 I 으로 충족).

대원칙:
- 한 논리 모듈 = 한 Gradle 모듈 = `app.giftify.<module>` 패키지 루트.
- 모듈 간 의존성은 `package-info.java` 의 `@NamedInterface("api")` 가 명시한
  *공개 API 면* 만 import 가능.
- `ApplicationModules.verify()` 가 *유효* 한 상태로 통과되어야 한다 (현재는
  `@Disabled` 상태).

#### 3.1.a Target Module Matrix

본 spec 의 의무 작업. *patch matrix* — 9 개 신규 모듈 + 2 개 해체 + 1 개 잔류
판단 = 12 행. 각 행은 새 Gradle 모듈로 *반드시 존재* 또는 *반드시 삭제* 가
DoD 의 합격 신호.

| # | 현 위치 (패키지) | 새 Gradle 모듈 | 새 source root | Java package root | settings.gradle.kts include | api-server 의존성 |
|---|----------------|--------------|----------------|-------------------|----------------------------|------------------|
| 1 | `bc/catalog/cart/`     | `bc/cart`     | `bc/cart/src/main/java/app/giftify/cart/`         | `app.giftify.cart`     | `":bc:cart"`     | 추가 |
| 2 | `bc/catalog/product/`  | `bc/product`  | `bc/product/src/main/java/app/giftify/product/`   | `app.giftify.product`  | `":bc:product"`  | 추가 |
| 3 | `bc/catalog/wishlist/` | `bc/wishlist` | `bc/wishlist/src/main/java/app/giftify/wishlist/` | `app.giftify.wishlist` | `":bc:wishlist"` | 추가 |
| 4 | `bc/catalog/image/`    | `bc/image`    | `bc/image/src/main/java/app/giftify/image/`       | `app.giftify.image`    | `":bc:image"`    | 추가 |
| 5 | `bc/catalog/replica/`  | (삭제) → 각 consumer 모듈의 `readmodel/` 으로 분산 | — | — | — | — |
| 6 | `bc/core/order/`       | `bc/order`    | `bc/order/src/main/java/app/giftify/order/`       | `app.giftify.order`    | `":bc:order"`    | 추가 |
| 7 | `bc/core/payment/`     | `bc/payment`  | `bc/payment/src/main/java/app/giftify/payment/`   | `app.giftify.payment`  | `":bc:payment"`  | 추가 |
| 8 | `bc/core/funding/`     | `bc/funding`  | `bc/funding/src/main/java/app/giftify/funding/`   | `app.giftify.funding`  | `":bc:funding"`  | 추가 |
| 9 | `bc/core/wallet/`      | `bc/wallet`   | `bc/wallet/src/main/java/app/giftify/wallet/`     | `app.giftify.wallet`   | `":bc:wallet"`   | 추가 |
| 10 | `bc/core/facade/`     | (삭제) → `bootstrap/api-server/usecase/` 로 이동 | — | — | — | — |
| 11 | `bc/core/shared/` (`config/`, `scheduler/`) | *plan 단계 결정* — (i) `bc/shared` 흡수 또는 (ii) `bootstrap/api-server/config/` 로 이동 | — | — | — | — |
| 12 | `bc/catalog`, `bc/core` aggregate | (삭제) — settings.gradle.kts 에서 `":bc:catalog"`, `":bc:core"` 제거 | — | — | 제거 | 제거 |

- 11 번 행 (bc/core/shared) 의 결정은 plan 의 *첫 step* 에서 grep 후 확정.
  실제 코드 분포 (config 와 scheduler 가 *core 모듈 내부용* 인지 *전역용* 인지)
  에 따라 (i) 또는 (ii). 결정 결과는 plan 안에서 본 matrix 의 행 갱신으로 반영.
- 5 번 행 (replica) 의 *각 consumer 모듈의 readmodel* 분산은 §3.3 의 결정에
  따른다 (모듈별 readmodel 분리 = Major 2 옵션 a).

### 3.2 facade 해체

```
삭제 대상:
    bc/core/src/main/java/app/giftify/facade/  (전체)

신규:
    bootstrap/api-server/src/main/java/app/giftify/usecase/
        ParticipateFundingUseCase.java   (구 CoreFacade.participateFunding)
        command/, vo/                    (구 facade/command, facade/vo)
```

규칙:
- 유스케이스 클래스에는 `@Transactional` 을 *걸지 않는다*. 각 모듈 호출이
  각자의 트랜잭션을 갖는다.
- 유스케이스가 *직접 호출* 하는 것은 1 단계 (place order, create payment)
  까지. 2 단계 이후 (markOrderAsPaid, processFundingActions) 는 `@ApplicationModuleListener`
  에 위임.
- 트랜잭션 간 정합성 확보는 *이벤트 + 보상 이벤트* — 단, 보상 이벤트 라인의
  완전 구현은 본 spec 범위 외 (MS4 W12 와 통합).

### 3.3 replica → readmodel 재정의 (모듈별 분리)

**결정 (Major 2 옵션 a)**: 단일 host 가 아닌 *각 소비 모듈이 자기 readmodel
보유*. MemberRepository 의 consumer 가 product/cart/wishlist 에 분산되어
있으므로, 단일 host 채택 시 *다른 모듈이 host 의 private readmodel 을 cross-
module import* 하는 안티패턴이 재발한다.

```
삭제:
    bc/catalog/src/main/java/app/giftify/replica/member/  (전체)
    Table MEMBER_REPLICAS

각 consumer 모듈이 자기 readmodel 신설 (실제 consumer 인 모듈만):
    bc/product/src/main/java/app/giftify/product/readmodel/
        ProductSellerView.java         (Entity, @Table = product_seller_views)
        ProductSellerViewRepository.java
        ProductSellerViewSyncListener.java  (@ApplicationModuleListener)

    bc/cart/src/main/java/app/giftify/cart/readmodel/
        (필요 시 — plan 단계 grep 으로 실제 nickname 사용 여부 확인)

    bc/wishlist/src/main/java/app/giftify/wishlist/readmodel/
        (필요 시 — plan 단계 grep 으로 실제 nickname 사용 여부 확인)
```

규칙:
- 패키지명은 *consumer 도메인 언어* — `<own_module>.readmodel.<View>`.
- 클래스명은 *읽기 모델 view 임* 을 드러내는 접미사 (`*View`, `*Snapshot`).
- 필드는 *consumer 가 실제 쓰는 것* 만. 원본 entity 의 축소판 ✗.
- readmodel 패키지는 *모듈의 private internal* — 다른 모듈이 직접 import 금지.
  (다른 모듈이 같은 데이터가 필요하면 *자기 readmodel* 을 신설.)
- 같은 이벤트 (`MemberSignedEvent` 등) 를 *여러 모듈 listener 가 동시 처리* —
  Modulith outbox 가 모듈별 1 회씩 dispatch.
- `SellerNicknameChangedEvent` 발행 라인 (ES 인덱스 갱신용) 은 *한 모듈에서만*
  발행해야 중복 방지 — plan 단계에서 product 모듈로 확정 (가장 큰 consumer 가
  발행 주체).

비용 트레이드오프:
- 데이터 중복: 같은 nickname 이 product_seller_views / cart_seller_views /
  wishlist_seller_views 에 각각 저장. 현재 측정 규모 (회원 수 × 닉네임 길이)
  로 무시 가능.
- Outbox 부담: `MemberUpdatedEvent` 1 건당 N 개 listener (N = consumer 모듈 수).
  현재 N = 1~3, 즉 outbox row 수가 1 ~ 3 배. 부담 무시 가능.
- 이득: 모듈 자율성 + cross-module readmodel import 0 — *재발 방지* 의 핵심.

### 3.4 잠재적 replica 후보 식별 의무

implementation plan 의 *식별 보고서* 단계에서:
- notification 모듈, settlement 모듈 등에서 cross-module 데이터 의존성을 grep
- 각 의존성에 대해 *§4 의 판단 표* 를 적용
  - "이벤트 payload 박제" 가능 → 그쪽
  - "식별자만 + 동기 조회" 가능 → 그쪽
  - "반복 조인 필요" 인 경우만 readmodel 신설
- 신설 시 동일 네이밍 원칙 적용 + §3.3 의 *자기 모듈 안* 원칙 적용:
  소비하는 모듈이 자기 readmodel 보유, *다른 모듈의 readmodel 은 import 금지*.

본 spec 은 *원칙* 만 결정한다. 신설 readmodel 추가는 plan 안에서 *식별된 만큼*만.

## 4. read-model 판단 원칙

```
       NO read-model 필요                       YES read-model 필요
+----------------------------+         +-------------------------------+
| 이벤트 payload 에 박제      |         | 한 모듈이 다른 모듈 데이터를   |
| (immutable snapshot)       |         | 자기 쿼리에서 반복 조인 필요   |
+----------------------------+         +-------------------------------+

+----------------------------+         +-------------------------------+
| 식별자만 들고있고 표시 시점  |         | 명명: <host>.readmodel.<View>  |
| 동기 호출 (소량/관리자 화면)  |         | (consumer 도메인 언어)         |
+----------------------------+         +-------------------------------+
```

## 5. Hexagonal 적용 범위

전부에 강요하지 않는다.

**Port + Adapter 강제 적용:** 외부 시스템 경계만 (PG, S3/MinIO, Kafka producer,
Email/SMS).

**Spring Modulith Named Interface 만으로 충분:** 내부 모듈 대다수 — `api/`
하위만 외부 노출, `domain/ + application/ + infrastructure/` 은 모듈 내부.

**최상위 (api-server) 디렉터리:**
```
bootstrap/api-server/src/main/java/app/giftify/
    GiftifyApplication.java
    controller/    ← 모든 REST controller
    usecase/       ← ParticipateFundingUseCase 등 (구 CoreFacade)
    config/        ← Security, DataSource, Kafka 등
```

## 6. 테스트 전략

### 6.1 회귀 방지 — 통합 테스트

facade 해체 시 트랜잭션 경계가 변경된다. 다음 시나리오를 통합 테스트로
*facade 해체 전후 동일 결과* 검증:

1. **PlaceOrder for Funding 정상 흐름** — Order 생성, Payment 성공,
   markOrderAsPaid, FundingParticipated 까지 도달.
2. **Payment 생성 실패** — Order 가 *생성된 채로 남거나* 보상 이벤트로
   취소되는 동작. *현재 동작* (해체 전 baseline) 은 implementation plan
   의 첫 step 에서 기존 코드를 *그대로 두고* 테스트로 측정하여 expected
   outcome 으로 고정 → 해체 후에도 동일 outcome 유지.
3. **markOrderAsPaid 리스너 실패** — 자기 트랜잭션 격리로 PaymentSucceeded
   의 outbox 항목이 살아있어 재시도 가능함을 확인.

### 6.2 ArchUnit / Modulith verify()

- `ApplicationModules.of(GiftifyApplication.class).verify()` — `@Disabled` 해제
  목표. 평탄화 후 위반이 0 이어야 PR merge.
- `Documenter(...).writeModulesAsPlantUml()` 산출물은 PR 리뷰 시 첨부.

### 6.3 readmodel 동작 검증

- `MemberSignedEvent` 발행 → `CatalogSellerView` 행 생성 확인 (@ApplicationModuleListener
  의 동기화 동작).
- `MemberUpdatedEvent` 발행 → nickname 갱신 + `SellerNicknameChangedEvent`
  체이닝 확인 (ES 인덱스 갱신 라인 보존).

## 7. 실행 전략 — 전략 1 (Big-Bang)

### 7.1 작업 단위

**단일 작업 단위 (Big-Bang).** 본 spec 의 *모든* 변경을 하나의 논리적
단위로 묶어 처리한다 — 워크트리 내부에서는 가독성을 위해 여러 atomic commit
으로 나눌 수 있으나, 사용자가 본 프로젝트로 가져갈 때는 *하나의 단위* 로
취급한다. 포함 항목:

- 디렉터리 이동 + `@ApplicationModule` + `package-info.java`
- facade/replica 식별 보고서 (`docs/reports/2026-05-21-anti-pattern-identification.md`)
- `CoreFacade` 해체 → `ParticipateFundingUseCase`
- replica → readmodel 재정의 (host module 1 개 이상)
- 통합 테스트 보강 (회귀 baseline 측정 포함, §6.1 참조)
- `ApplicationModules.verify()` 의 `@Disabled` 해제

### 7.2 워크플로우 (cherry-pick 기반)

```
[cherry-pick worktree]  ──fork──>  [flattening worktree]
staging/post-deploy-cleanup        staging/module-flattening
HEAD: ddad2dbe                     HEAD: ddad2dbe (initially)
                                     │
                                     ▼
                          (atomic commit 들 by plan)
                                     │
                                     ▼
                          [본 프로젝트로 cherry-pick]
                          사용자가 IDE 에서 직접 가져감
                                     │
                                     ▼
                          PR 작성은 *본 프로젝트* 측에서
                          (워크트리에서는 PR 만들지 않음)
```

### 7.3 base 머지 순서 가정

**Atomic unit 정의**: `ddad2dbe..staging/module-flattening` *commit range
전체* 가 본 작업의 단위. 다음 조건만 만족:

- *base 35 commits 가 develop 에 반영된 후*, 본 commit range *전체* 를
  cherry-pick (또는 merge) 했을 때 컴파일/테스트 통과.
- 워크트리 내부의 *각 중간 commit 의 독립 green* 은 *요구하지 않는다* (Big-Bang
  본질상). 워크트리 안의 atomic commit 분할은 *리뷰 가독성* 의 목적이며 *개별
  cherry-pick 단위가 아님*.

본 작업의 *최종 develop 도달 시점* 은 base 인 `staging/post-deploy-cleanup`
의 35 commits 가 *먼저* develop 에 (PR 들로 묶여) 머지된 *후*. 평탄화
commit 묶음은 base 의존성을 깨지 않는 atomic unit 으로 유지한다 — *range
전체* 가 cherry-pick 대상.

## 8. Definition of Done

1. **§3.1.a Target Module Matrix 의 9 개 신규 모듈** 이 *모두 생성* 되었고,
   `settings.gradle.kts` 의 `include` 에 등재되었다. `bc/catalog`, `bc/core`
   Gradle 모듈은 *삭제* 되었다 (matrix 12 번 행).
2. **`bc/core/facade/`** 패키지가 *삭제* 되었다.
3. **`bc/catalog/replica/`** 패키지가 *삭제* 되었고, 실제 nickname consumer
   인 모듈 (최소 product) 의 `<own>.readmodel/` 패키지로 *모듈별 분리* 되어
   있다. `MEMBER_REPLICAS` 테이블도 제거.
4. **`ParticipateFundingUseCase`** 가 `bootstrap/api-server/.../usecase/` 에
   존재하며 `@Transactional` 을 갖지 않는다.
5. **`ApplicationModules.of(GiftifyApplication.class).verify()`** 가 통과한다
   (`@Disabled` 해제).
6. **§6.1 의 통합 테스트 시나리오** 가 모두 통과한다.
7. **anti-pattern 식별 보고서** (`docs/reports/2026-05-21-anti-pattern-identification.md`)
   가 존재하고, 식별된 모든 항목이 *해결* 또는 *후속 항목으로 명시* 되었다.
8. **Cross-module readmodel import 가 0** — `git grep "import app.giftify.*.readmodel"`
   결과 중 *같은 모듈 내부* 가 아닌 import 가 *없다*. (즉 다른 모듈의
   private readmodel 패키지를 직접 참조하지 않는다.)
9. **Atomic unit (§7.3)** 의 통과 조건 — `develop` 위에 base 35 commits +
   본 range 전체를 적용한 상태에서 `./gradlew test` 가 통과한다.

## 9. 본 spec 의 범위 외 (Out of Scope)

- **보상 이벤트 라인 완전 구현** — Payment 실패 시 `OrderCanceled` 보상 이벤트로의
  안전한 데이터 정합 회복. → MS4 W12 T12.6-T12.10 와 통합.
- **EventPublisher 의 모든 사용처에 대한 타입 안전 마이그레이션** — `publish(Object)`
  를 완전히 제거하는 작업. 본 spec 은 *현 상태 (`publishDomainEvent` default
  + `@Deprecated publish`)* 를 유지한 채 진행.
- **PaymentEvent ↔ DomainEvent 인터페이스 통일** — 별도 도메인 리팩터링 spec.
- **분리 서버 (settlement-server, payment-server) 의 별 JVM 분리** — MS5
  W13 의 작업, 본 spec 은 *그 작업이 쉬워지도록* 모듈만 정돈.
- **읽기 모델 추가 신설** (notification, settlement 등) — 식별 보고서에서
  *필요* 로 판정된 경우만 본 spec 안에서 추가. 그 외는 후속.
- **`application*.yml` / profile 통합** — pre-analysis 의 22 개 yml 분포 정리,
  loadtest/staging/prod profile 의 group 재편 등은 *본 spec 에서 건드리지
  않는다*. 평탄화 후 후속 spec.
- **Legacy `@ComponentScan` 정리** — `GiftifyApplication` 의 명시적 `@ComponentScan(basePackages=...)`
  는 모듈 추가에 맞춰 *최소 갱신* 만 (새 모듈 base package 추가). 전체 제거
  또는 reorganize 는 후속.
- **Flyway migration path / module-aware Flyway 갱신** — PR #420 으로 진행된
  Module-Aware Flyway 의 *모듈별 V1.0.0 + R__seed.sql* 구조는 *본 spec 안에서
  새 모듈에 동일 패턴으로 신설* 만 (기존 구조 변경 없음). 통합 또는 재설계는 후속.
- **`support:common` 모듈 흡수 또는 분해** — 5 importers + 14 classes 의
  분포 분석 및 재배치는 *본 spec 외*. 평탄화에서는 *그대로 둔다*.

## 10. 참고

- 사전 분석: `docs/specs/2026-05-20-T10.24-flatten-pre-analysis.md`
- Saga 결정: `docs/specs/2026-05-21-saga-pattern-decision.md`
- 이벤트 외부화 대상: `docs/specs/2026-05-21-event-externalization-targets.md`
- 정산 이벤트 전달: `docs/specs/2026-05-21-settlement-event-delivery-decision.md`
- 옵시디언 briefing (배경 설명): `00-Inbox/260521-GIFTIFY-01-modulith-flattening-design-briefing.md`
- ROADMAP SSoT: `backend/docs/superpowers/specs/2026-03-15-post-deploy-roadmap-design.md`
  (MS4 W10 T10.24~T10.29)

---
**상태**: 결정 확정. implementation plan 작성 입력.
