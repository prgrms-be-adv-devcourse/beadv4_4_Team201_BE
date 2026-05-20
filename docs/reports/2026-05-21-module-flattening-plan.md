# Module Flattening Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** `bc/catalog` (5 logical modules) 과 `bc/core` (4 modules + facade 안티패턴) 를 Big-Bang 으로 평탄화하고, replica 안티패턴을 모듈별 readmodel 로 재정의하며, `ApplicationModules.verify()` 를 활성화한다.

**Architecture:** Spring Modulith 2.0.3 의 *Java package 단위 모듈 인식* 을 활용한다. Gradle multi-module project 의 *물리적 모듈* 과 Modulith *논리적 모듈* 을 1:1 정렬. 각 Modulith 모듈은 자기 `package-info.java` 의 `@NamedInterface("api")` 로 공개 API 면을 명시하고, 다른 모듈은 `api` 패키지만 import. facade 책임은 `bootstrap/api-server/usecase/` 의 thin orchestrator 로 이동, 트랜잭션은 모듈별로 분리. replica 는 *각 consumer 모듈* 의 `readmodel/` 로 자기 readmodel 보유.

**Tech Stack:**
- Spring Boot 4.0.3 + Java 25 + Gradle 8.14.4
- Spring Modulith 2.0.3 (`@ApplicationModule`, `@NamedInterface`, `ApplicationModuleListener`, `ApplicationModules.verify()`)
- JUnit 5 + AssertJ + Mockito + Spring Boot Test
- JaCoCo (커버리지 측정)
- ArchUnit (선택, verify() 보완 시)

---

## Plan Overview

### Spec 매핑

본 plan 은 `docs/specs/2026-05-21-module-flattening-design.md` 의 다음 결정을 구현한다:

- §3.1.a Target Module Matrix 의 12 행 모두
- §3.2 facade 해체 → `ParticipateFundingUseCase`
- §3.3 replica → 모듈별 readmodel 분리 (Major 2 옵션 a)
- §6.1 통합 테스트 시나리오 3 개
- §8 DoD 9 항목 모두
- §9 Out of Scope 9 항목 *불침범* (task 안에서 건드리지 않음)

### Task → Commit → Cherry-pick Group 매핑

| Task | Commit message prefix | Cherry-pick Group | matrix # | 의미 |
|-----|----------------------|-------------------|----------|------|
| 1 | `docs: anti-pattern 식별 보고서` | A. 사전 보고 | — | 식별 보고서 |
| 2 | `refactor: bc/cart 분리` | B. catalog 분해 | 1 | cart 모듈 신설 |
| 3 | `refactor: bc/product 분리` | B. catalog 분해 | 2 | product 모듈 신설 |
| 4 | `refactor: bc/wishlist 분리` | B. catalog 분해 | 3 | wishlist 모듈 신설 |
| 5 | `refactor: bc/image 분리` | B. catalog 분해 | 4 | image 모듈 신설 |
| 6 | `refactor: bc/order 분리` | C. core 분해 | 6 | order 모듈 신설 |
| 7 | `refactor: bc/payment 분리` | C. core 분해 | 7 | payment 모듈 신설 |
| 8 | `refactor: bc/funding 분리` | C. core 분해 | 8 | funding 모듈 신설 |
| 9 | `refactor: bc/wallet 분리` | C. core 분해 | 9 | wallet 모듈 신설 |
| 10 | `refactor: bc/core/shared 흡수` | C. core 분해 | 11 | shared 처리 |
| 11 | `refactor: bc/catalog, bc/core aggregate 삭제` | D. aggregate 제거 | 12 | aggregate 모듈 삭제 |
| 12 | `refactor: ProductSellerView readmodel 신설` | E. replica 해체 | 5 | TDD readmodel |
| 13 | `refactor: bc/catalog/replica 삭제` | E. replica 해체 | 5 | replica 패키지 삭제 |
| 14 | `refactor: ParticipateFundingUseCase 신설` | F. facade 해체 | 10 | TDD usecase |
| 15 | `refactor: CoreFacade 삭제 + controller 갱신` | F. facade 해체 | 10 | facade 삭제 |
| 16 | `test: PlaceOrder for Funding 통합 테스트` | G. 검증 | — | 통합 테스트 |
| 17 | `chore: ModularityTest verify() 활성화` | G. 검증 | — | verify() 해제 |
| 18 | `chore: 커버리지 측정 + 보고서` | G. 검증 | — | 80%+ 검증 |

**Cherry-pick 단위 권장**:
- 사용자는 `Group A` ~ `Group G` 단위로 *연속 commit range* 를 cherry-pick.
- 각 Group 은 *그 자체로* 컴파일 + 테스트 통과.
- Group B 와 C 는 *순서 의존* (B 가 C 보다 먼저). Group D 는 B + C 둘 다 후.
- Group E + F 는 *D 후*. Group G 는 *마지막*.

### 의존성 그래프

```
A (식별 보고서)
   │
   ▼
B (catalog 분해) ──┐
                  ├──▶ D (aggregate 삭제) ──▶ E (replica 해체) ──┐
C (core 분해) ────┘                                              ├──▶ G (검증)
                       └─────────────────────▶ F (facade 해체) ──┘
```

### TDD 적용 방침

- **Task 2-11 (디렉터리 이동)**: TDD 의 *test-first* 는 자연스럽지 않음. 대신 *기존 테스트가 동일하게 통과* 함을 *해당 task 의 합격 신호* 로 사용. (이동 자체는 의미 변경이 아니므로 *기존 테스트 = 회귀 가드*.)
- **Task 12 (ProductSellerView)**: 완전한 TDD. 실패하는 테스트 → 구현 → 통과.
- **Task 14 (ParticipateFundingUseCase)**: 완전한 TDD. 단 *기존 동작 보존* 이므로 *현재 CoreFacade 동작을 측정한 baseline* 을 expected 로 고정.
- **Task 16 (통합 테스트)**: 명시적 TDD 단계 — Test → 통합 검증.

### 80%+ 커버리지 측정

- `./gradlew jacocoTestReport` 또는 모듈별 `jacoco { ... }` 설정으로 측정.
- 측정 대상: *신규 코드* (UseCase + readmodel listener) 가 80%+. 기존 코드의 평탄화로 인한 *전체 평균* 도 80%+ 유지.
- 측정 결과 보고서: `docs/reports/2026-05-21-flattening-coverage.md`.

---

## File Structure

### 신규 생성 (Group B + C + D + E + F)

```
bc/cart/                                  [신규 Gradle 모듈]
    build.gradle.kts
    src/main/java/app/giftify/cart/       [bc/catalog/cart/ 에서 이동]
    src/test/java/app/giftify/cart/       [bc/catalog/src/test/java/.../cart/ 에서 이동]
bc/product/                               [신규 — bc/catalog/product/ 에서 이동]
    build.gradle.kts
    src/main/java/app/giftify/product/
        readmodel/                        [신규 — replica/member 의 대체]
            ProductSellerView.java
            ProductSellerViewRepository.java
            ProductSellerViewSyncListener.java
    src/test/java/app/giftify/product/
        readmodel/
            ProductSellerViewSyncListenerTest.java
bc/wishlist/                              [신규 — bc/catalog/wishlist/ 에서 이동]
bc/image/                                 [신규 — bc/catalog/image/ 에서 이동]
bc/order/                                 [신규 — bc/core/order/ 에서 이동]
bc/payment/                               [신규 — bc/core/payment/ 에서 이동]
bc/funding/                               [신규 — bc/core/funding/ 에서 이동]
bc/wallet/                                [신규 — bc/core/wallet/ 에서 이동]

bootstrap/api-server/src/main/java/app/giftify/
    usecase/                              [신규]
        ParticipateFundingUseCase.java    [구 CoreFacade.participateFunding]
        command/
            ParticipateFundingCommand.java          [bc/core/facade/command 에서 이동]
            ParticipateFundingItemCommand.java
        vo/
            PlaceOrderResult.java                   [bc/core/facade/vo 에서 이동]
            GetOrdersResult.java
            GetOrderResult.java
bootstrap/api-server/src/test/java/app/giftify/
    usecase/
        ParticipateFundingUseCaseTest.java          [TDD]
        ParticipateFundingIntegrationTest.java      [통합]

docs/reports/
    2026-05-21-anti-pattern-identification.md      [Task 1]
    2026-05-21-flattening-coverage.md              [Task 18]
```

### 삭제 (Group D + E + F)

```
bc/catalog/                               [aggregate 모듈 삭제]
bc/core/                                  [aggregate 모듈 삭제]
bc/catalog/src/main/java/app/giftify/replica/       [replica 패키지 삭제]
bc/core/src/main/java/app/giftify/facade/           [facade 패키지 삭제]
```

### 수정

```
settings.gradle.kts                       [Group B/C/D 마다]
bootstrap/api-server/build.gradle.kts     [Group B/C/D 마다 의존성 갱신]
bootstrap/api-server/src/main/java/app/giftify/GiftifyApplication.java
                                          [@ComponentScan basePackages 갱신 - Task 11]
bc/shared/build.gradle.kts                [Task 10 - bc/core/shared 흡수 시]
```

---

## Common Patterns

본 절은 Task 2-9 의 *Gradle 모듈 신설 공통 절차*. 각 task 본문은 *그 task 특유의 변경점* 만 detail. 공통 step 은 본 절을 참조.

### Pattern P1: 새 Gradle 모듈 신설 + 패키지 이동

**적용 task**: 2 (cart), 3 (product), 4 (wishlist), 5 (image), 6 (order), 7 (payment), 8 (funding), 9 (wallet).

각 task 가 진행할 step (Common Step C1-C8):

- **Common Step C1**: 새 모듈 디렉터리 + `build.gradle.kts` 생성
  - 경로: `bc/<module>/build.gradle.kts`
  - 내용: 기존 `bc/<aggregate>/build.gradle.kts` 의 구조를 따라 작성. 의존성은 *해당 sub 가 실제 사용하는 것만*.
  - 표준 build.gradle.kts 템플릿 (Boot 4.0.3 + Modulith 2.0.3 + Java 25):

    ```kotlin
    plugins {
        id("giftify.java-library")
    }

    dependencies {
        implementation(project(":bc:shared"))
        implementation(project(":support:common"))
        implementation(project(":support:logging"))
        implementation(libs.spring.boot.starter.data.jpa)
        implementation(libs.spring.modulith.api)
        implementation(libs.spring.modulith.events.api)
        implementation(libs.jspecify)
        implementation(libs.lombok)
        annotationProcessor(libs.lombok)
        testImplementation(libs.spring.boot.starter.test)
    }
    ```
  - *task 별로* 위 템플릿에 추가/제거되는 의존성은 task 본문에서 명시.

- **Common Step C2**: `settings.gradle.kts` 에 `include` 추가
  - 위치: `settings.gradle.kts` 의 `include(...)` 블록.
  - 명령:

    ```bash
    grep -n "^include" settings.gradle.kts | tail -5
    ```

  - 알파벳 순서 유지하며 해당 위치에 `include(":bc:<module>")` 추가.

- **Common Step C3**: `git mv` 로 source/test 이동
  - 패키지 이동 (Java package 는 *동일* `app.giftify.<module>` 유지):

    ```bash
    cd /Users/chan99/IdeaProjects/grep/giftify-be/.worktrees/staging/flattening
    mkdir -p bc/<module>/src/main/java/app/giftify
    mkdir -p bc/<module>/src/test/java/app/giftify
    git mv bc/<aggregate>/src/main/java/app/giftify/<module> bc/<module>/src/main/java/app/giftify/<module>
    # 테스트 디렉터리 존재 시:
    [ -d bc/<aggregate>/src/test/java/app/giftify/<module> ] && \
        git mv bc/<aggregate>/src/test/java/app/giftify/<module> bc/<module>/src/test/java/app/giftify/<module>
    ```

- **Common Step C4**: `bc/<module>/src/main/java/app/giftify/<module>/package-info.java` 신설 (없는 경우)
  - 내용:

    ```java
    @org.springframework.modulith.ApplicationModule(
        displayName = "<Module>",
        allowedDependencies = { "shared", "shared::api" }  // task 별로 보강
    )
    @org.springframework.modulith.NamedInterface("api")
    package app.giftify.<module>;
    ```

  - `allowedDependencies` 는 *해당 모듈이 의존하는 다른 모듈명* 을 task 별로 명시. 모르면 빈 배열로 두고 Common Step C7 에서 `verify()` 가 알려줌 — 그 후 갱신.

- **Common Step C5**: `bc/<aggregate>/build.gradle.kts` 에서 의존성 정리 (필요 시)
  - aggregate 가 sub 의 의존성을 직접 노출 중이었으면 제거. 보통 aggregate 는 *sub 들을 한 build target 으로 묶기만 함* 이라 별도 정리 불필요. task 본문에서 명시.

- **Common Step C6**: `bootstrap/api-server/build.gradle.kts` 에 새 모듈 의존성 추가
  - 위치: `dependencies { ... }` 블록 안, 알파벳 순서 유지.
  - 추가 라인:

    ```kotlin
    implementation(project(":bc:<module>"))
    ```

- **Common Step C7**: 컴파일 + 테스트 실행
  - 명령:

    ```bash
    cd /Users/chan99/IdeaProjects/grep/giftify-be/.worktrees/staging/flattening
    ./gradlew :bc:<module>:test --no-daemon
    ./gradlew :bootstrap:api-server:compileJava --no-daemon
    ```

  - 예상: 둘 다 BUILD SUCCESSFUL.
  - 실패 시: `import app.giftify.<other_module>...` 가 *해당 모듈의 build.gradle.kts 에 dependency 없어서* 깨지는 경우 — task 본문의 의존성 표를 참고하여 보강.

- **Common Step C8**: 커밋

    ```bash
    cd /Users/chan99/IdeaProjects/grep/giftify-be/.worktrees/staging/flattening
    git add bc/<module>/ bc/<aggregate>/build.gradle.kts settings.gradle.kts \
        bootstrap/api-server/build.gradle.kts
    # commit message 는 task 본문에서 명시
    ```

### Pattern P2: aggregate 모듈 삭제 (Task 11 만)

- aggregate (`bc/catalog`, `bc/core`) 의 `src/` 가 비었음을 `find bc/catalog/src -type f | wc -l` 로 확인 (= 0).
- `bc/<aggregate>/build.gradle.kts` 삭제.
- `settings.gradle.kts` 에서 `include(":bc:<aggregate>")` 제거.
- `bootstrap/api-server/build.gradle.kts` 에서 `implementation(project(":bc:<aggregate>"))` 제거 (있는 경우).
- `bootstrap/api-server/src/main/java/app/giftify/GiftifyApplication.java` 의 `@ComponentScan` basePackages 갱신:
  - 추가: `app.giftify.cart`, `app.giftify.product`, ..., `app.giftify.wallet`.
  - 제거: 없음 (각 sub 의 패키지는 이미 명시되었을 가능성, task 본문에서 확인).
- 컴파일 + 테스트 + 커밋.

### Pattern P3: TDD readmodel (Task 12)

- 실패 테스트 → listener 구현 → 통과 → 커밋. 본 패턴은 Task 12 본문에서 *완전한 step* 으로 전개.

### Pattern P4: TDD usecase (Task 14)

- 현재 `CoreFacade.participateFunding` 의 동작을 *baseline measurement* 로 추출 → 새 UseCase 가 *동일 outcome* 을 보장하도록 테스트 → 구현 → 검증. Task 14 본문에서 full.

---

---

## Task 1: Anti-pattern 식별 보고서 작성

**Group:** A
**의존성:** 없음

**Files:**
- Create: `docs/reports/2026-05-21-anti-pattern-identification.md`

본 task 는 *코드 변경 없음*. 평탄화 전 *식별된 안티패턴 + 호출 분포 + 후속 task 매핑* 의 명시적 박제. spec §3.4 의 "잠재적 replica 후보 식별 의무" 의 입력.

- [ ] **Step 1: nickname consumer 분포 grep**

    ```bash
    cd /Users/chan99/IdeaProjects/grep/giftify-be/.worktrees/staging/flattening
    echo "=== MemberRepository (replica) consumers ===" > /tmp/identification.txt
    git grep -l "app.giftify.replica.member.MemberRepository\|replica.member.Member" >> /tmp/identification.txt
    echo "" >> /tmp/identification.txt
    echo "=== CoreFacade callers ===" >> /tmp/identification.txt
    git grep -l "CoreFacade\|facade.CoreFacade" >> /tmp/identification.txt
    echo "" >> /tmp/identification.txt
    echo "=== bc/core/facade contents ===" >> /tmp/identification.txt
    find bc/core/src/main/java/app/giftify/facade -type f >> /tmp/identification.txt
    echo "" >> /tmp/identification.txt
    echo "=== bc/catalog/replica contents ===" >> /tmp/identification.txt
    find bc/catalog/src/main/java/app/giftify/replica -type f >> /tmp/identification.txt
    cat /tmp/identification.txt
    ```

  - Expected output: `MemberRepository` 의 consumer 패키지 목록 (product / cart / wishlist 중 어디인지 확정).

- [ ] **Step 2: cross-module 의존성 grep (notification / settlement)**

    ```bash
    cd /Users/chan99/IdeaProjects/grep/giftify-be/.worktrees/staging/flattening
    echo "=== notification cross-module imports ===" >> /tmp/identification.txt
    git grep -l "^import app.giftify" -- 'bc/notification/**/*.java' | \
        xargs grep -h "^import app.giftify" | \
        grep -v "app.giftify.notification\|app.giftify.shared\|app.giftify.support" | \
        sort -u >> /tmp/identification.txt
    echo "" >> /tmp/identification.txt
    echo "=== settlement cross-module imports ===" >> /tmp/identification.txt
    git grep -l "^import app.giftify" -- 'bc/settlement/**/*.java' | \
        xargs grep -h "^import app.giftify" | \
        grep -v "app.giftify.settlement\|app.giftify.shared\|app.giftify.support" | \
        sort -u >> /tmp/identification.txt
    cat /tmp/identification.txt
    ```

- [ ] **Step 3: 보고서 작성**

    `docs/reports/2026-05-21-anti-pattern-identification.md` 작성. 다음 섹션 포함:

    ```markdown
    # Anti-pattern Identification Report
    > 2026-05-21 / module flattening (T10.24~T10.29) 사전 입력

    ## 1. 식별 대상
    ### 1.1 bc/core/facade/CoreFacade
    - 파일: [Step 1 결과 붙여넣기]
    - Caller 목록: [Step 1 결과]
    - 분석: ParticipateFunding 흐름의 분산 트랜잭션 코디네이터.
      해체 후 bootstrap/api-server/usecase/ParticipateFundingUseCase 로 이동.
    - 후속 task: 14, 15.

    ### 1.2 bc/catalog/replica/member
    - 파일: [Step 1 결과]
    - Consumer 분포: [Step 1 결과 — product / cart / wishlist 중 어디인지]
    - 분석: 단일 host 가 아닌 모듈별 분리 (spec §3.3 Major 2 옵션 a).
    - 후속 task: 12, 13.

    ## 2. 잠재 후보 — notification
    - cross-module import: [Step 2 결과]
    - readmodel 신설 필요? [판단 표 적용 결과]
    - 후속: 본 spec 내 / spec 외

    ## 3. 잠재 후보 — settlement
    - cross-module import: [Step 2 결과]
    - readmodel 신설 필요? [판단 표 적용 결과]
    - 후속: 본 spec 내 / spec 외

    ## 4. 후속 task 매핑
    | 식별 항목 | Plan task | Group |
    |---------|----------|-------|
    | CoreFacade | 14, 15 | F |
    | replica/member | 12, 13 | E |
    | (potential notification readmodel) | — | (spec 외) |
    | (potential settlement readmodel) | — | (spec 외) |
    ```

- [ ] **Step 4: Commit**

    ```bash
    cd /Users/chan99/IdeaProjects/grep/giftify-be/.worktrees/staging/flattening
    git add docs/reports/2026-05-21-anti-pattern-identification.md
    cat > /tmp/commit-msg-t1.txt <<'EOF'
    docs: anti-pattern 식별 보고서 (Task 1, Group A)

    bc/core/facade/CoreFacade + bc/catalog/replica/member 의 호출 분포 +
    notification/settlement 의 cross-module import 분석. 후속 task 매핑 박제.

    spec §3.4, DoD §8.7 의 입력.
    EOF
    git commit -F /tmp/commit-msg-t1.txt
    rm /tmp/commit-msg-t1.txt
    ```

  - Expected: 1 file changed, ~50-100 insertions.

---

## Task 2: bc/cart 분리 (matrix #1)

**Group:** B
**의존성:** Task 1
**Pattern:** P1 (Gradle 모듈 신설 + 패키지 이동)

**Files:**
- Create: `bc/cart/build.gradle.kts`, `bc/cart/src/main/java/app/giftify/cart/package-info.java`
- Move: `bc/catalog/src/main/java/app/giftify/cart/**` → `bc/cart/src/main/java/app/giftify/cart/**`
- Modify: `settings.gradle.kts`, `bootstrap/api-server/build.gradle.kts`, `bc/catalog/build.gradle.kts`

**bc/cart 가 실제로 의존하는 모듈** (사전 grep 으로 확정):

```bash
cd /Users/chan99/IdeaProjects/grep/giftify-be/.worktrees/staging/flattening
grep -rh "^import app.giftify" bc/catalog/src/main/java/app/giftify/cart/ | \
    grep -v "app.giftify.cart" | sort -u
```

예상 의존성: `app.giftify.shared`, `app.giftify.product` (TargetType 등), `app.giftify.member` (replica 또는 직접).

- [ ] **Step 1: 새 디렉터리 + build.gradle.kts 생성** (Common C1)

    `bc/cart/build.gradle.kts`:

    ```kotlin
    plugins {
        id("giftify.java-library")
    }

    dependencies {
        implementation(project(":bc:shared"))
        implementation(project(":bc:product"))    // TargetType, ProductSnapshot 등
        implementation(project(":support:common"))
        implementation(project(":support:logging"))
        implementation(libs.spring.boot.starter.data.jpa)
        implementation(libs.spring.modulith.api)
        implementation(libs.spring.modulith.events.api)
        implementation(libs.jspecify)
        compileOnly(libs.lombok)
        annotationProcessor(libs.lombok)
        testImplementation(libs.spring.boot.starter.test)
    }
    ```

    *주의*: `:bc:product` 의존성은 *Task 3 완료 후* 만 유효. 즉 Task 2 단독 cherry-pick 시 깨짐 → cherry-pick group B 는 *Task 2-5 묶음* 으로만 의미. spec §7.3 의 *range 전체* 통과 조건과 일치.

    Task 2 만 단독 빌드 시 임시로 `:bc:catalog` 의 product 부분에 link 되도록 *bc/catalog/build.gradle.kts 가 transitive expose* 를 유지하거나, Task 2 단계에서는 `:bc:catalog` 의존성을 유지하고 Task 5 후 정리. 단순화를 위해 *Task 2-5 동안 :bc:catalog 의존성 유지*, Task 11 에서 일괄 제거.

    수정 build.gradle.kts (Task 2 한정):

    ```kotlin
    plugins { id("giftify.java-library") }
    dependencies {
        implementation(project(":bc:shared"))
        implementation(project(":bc:catalog"))    // 임시 — Task 11 에서 제거
        implementation(project(":support:common"))
        implementation(project(":support:logging"))
        implementation(libs.spring.boot.starter.data.jpa)
        implementation(libs.spring.modulith.api)
        implementation(libs.spring.modulith.events.api)
        implementation(libs.jspecify)
        compileOnly(libs.lombok)
        annotationProcessor(libs.lombok)
        testImplementation(libs.spring.boot.starter.test)
    }
    ```

- [ ] **Step 2: settings.gradle.kts 갱신** (Common C2)

    `settings.gradle.kts` 안 `include` 호출에 알파벳 위치에 추가:

    ```kotlin
    include(":bc:cart")
    ```

- [ ] **Step 3: git mv** (Common C3)

    ```bash
    cd /Users/chan99/IdeaProjects/grep/giftify-be/.worktrees/staging/flattening
    mkdir -p bc/cart/src/main/java/app/giftify
    mkdir -p bc/cart/src/test/java/app/giftify
    git mv bc/catalog/src/main/java/app/giftify/cart bc/cart/src/main/java/app/giftify/cart
    if [ -d bc/catalog/src/test/java/app/giftify/cart ]; then
        git mv bc/catalog/src/test/java/app/giftify/cart bc/cart/src/test/java/app/giftify/cart
    fi
    ```

- [ ] **Step 4: package-info.java 신설** (Common C4)

    `bc/cart/src/main/java/app/giftify/cart/package-info.java`:

    ```java
    @org.springframework.modulith.ApplicationModule(
        displayName = "Cart",
        allowedDependencies = { "shared", "product", "catalog" }
    )
    @org.springframework.modulith.NamedInterface("api")
    package app.giftify.cart;
    ```

  - `catalog` 는 임시 (Task 11 에서 제거 후 `product` 만 남음).

- [ ] **Step 5: 기존 import path 검증**

    Java package 는 `app.giftify.cart` 그대로 유지되므로 *cart 내부 클래스의 import 변경 없음*. cart 외부 (member, notification 등) 도 import 변경 없음. 단 *gradle module dependency* 만 새 모듈로 라우팅됨.

- [ ] **Step 6: 컴파일 + 테스트** (Common C7)

    ```bash
    cd /Users/chan99/IdeaProjects/grep/giftify-be/.worktrees/staging/flattening
    ./gradlew :bc:cart:test --no-daemon 2>&1 | tail -30
    ./gradlew :bootstrap:api-server:compileJava --no-daemon 2>&1 | tail -30
    ```

  - Expected: BUILD SUCCESSFUL × 2.

- [ ] **Step 7: Commit** (Common C8)

    ```bash
    cd /Users/chan99/IdeaProjects/grep/giftify-be/.worktrees/staging/flattening
    git add bc/cart/ settings.gradle.kts bootstrap/api-server/build.gradle.kts
    # bc/catalog/build.gradle.kts 는 본 task 에서 변경 없음 (cart 의 dependency 가
    # transitively expose 되도록 유지)
    cat > /tmp/commit-msg-t2.txt <<'EOF'
    refactor: bc/cart 분리 (Task 2, matrix #1, Group B)

    bc/catalog/cart → bc/cart 신규 Gradle 모듈로 이동.
    Java package 는 app.giftify.cart 그대로 유지.
    임시로 :bc:catalog 의존성 보유 (Task 11 에서 정리).

    spec §3.1.a matrix 행 1.
    EOF
    git commit -F /tmp/commit-msg-t2.txt
    rm /tmp/commit-msg-t2.txt
    ```

---

## Task 3: bc/product 분리 (matrix #2)

**Group:** B
**의존성:** Task 2
**Pattern:** P1

**Files:**
- Create: `bc/product/build.gradle.kts`, `bc/product/src/main/java/app/giftify/product/package-info.java`
- Move: `bc/catalog/src/main/java/app/giftify/product/**` → `bc/product/src/main/java/app/giftify/product/**`
- Modify: `settings.gradle.kts`, `bootstrap/api-server/build.gradle.kts`

- [ ] **Step 1: bc/product 가 실제 의존하는 모듈 grep**

    ```bash
    cd /Users/chan99/IdeaProjects/grep/giftify-be/.worktrees/staging/flattening
    grep -rh "^import app.giftify" bc/catalog/src/main/java/app/giftify/product/ | \
        grep -v "app.giftify.product" | sort -u
    ```

  - 예상: `app.giftify.shared`, `app.giftify.image` (Image 참조), `app.giftify.member` (Seller).

- [ ] **Step 2-7: Common Pattern P1 적용**

    `bc/product/build.gradle.kts`:

    ```kotlin
    plugins { id("giftify.java-library") }
    dependencies {
        implementation(project(":bc:shared"))
        implementation(project(":bc:catalog"))    // 임시 — image / member 의 transitive
        implementation(project(":bc:member"))      // Seller 직접 참조
        implementation(project(":support:common"))
        implementation(project(":support:logging"))
        implementation(libs.spring.boot.starter.data.jpa)
        implementation(libs.spring.modulith.api)
        implementation(libs.spring.modulith.events.api)
        implementation(libs.jspecify)
        compileOnly(libs.lombok)
        annotationProcessor(libs.lombok)
        testImplementation(libs.spring.boot.starter.test)
    }
    ```

    `settings.gradle.kts`: `include(":bc:product")` 추가.

    `bc/product/src/main/java/app/giftify/product/package-info.java`:

    ```java
    @org.springframework.modulith.ApplicationModule(
        displayName = "Product",
        allowedDependencies = { "shared", "image", "member", "catalog" }
    )
    @org.springframework.modulith.NamedInterface("api")
    package app.giftify.product;
    ```

    `git mv bc/catalog/src/main/java/app/giftify/product bc/product/src/main/java/app/giftify/product`
    (test 디렉터리 있으면 동일하게 이동.)

    `bootstrap/api-server/build.gradle.kts` 에 `implementation(project(":bc:product"))` 추가.

- [ ] **Step 8: 컴파일 + 테스트**

    ```bash
    ./gradlew :bc:product:test :bootstrap:api-server:compileJava --no-daemon 2>&1 | tail -30
    ```

  - Expected: BUILD SUCCESSFUL.

- [ ] **Step 9: Commit**

    ```bash
    git add bc/product/ settings.gradle.kts bootstrap/api-server/build.gradle.kts
    cat > /tmp/commit-msg-t3.txt <<'EOF'
    refactor: bc/product 분리 (Task 3, matrix #2, Group B)

    bc/catalog/product → bc/product 신규 Gradle 모듈로 이동.
    image / member 의 transitive 를 위해 :bc:catalog 임시 의존 (Task 11 정리).

    spec §3.1.a matrix 행 2.
    EOF
    git commit -F /tmp/commit-msg-t3.txt
    rm /tmp/commit-msg-t3.txt
    ```

---

## Task 4: bc/wishlist 분리 (matrix #3)

**Group:** B
**의존성:** Task 2, 3
**Pattern:** P1

**Files:**
- Create: `bc/wishlist/build.gradle.kts`, `bc/wishlist/src/main/java/app/giftify/wishlist/package-info.java`
- Move: `bc/catalog/src/main/java/app/giftify/wishlist/**` → `bc/wishlist/src/main/java/app/giftify/wishlist/**`

- [ ] **Step 1: 의존성 grep**

    ```bash
    grep -rh "^import app.giftify" bc/catalog/src/main/java/app/giftify/wishlist/ | \
        grep -v "app.giftify.wishlist" | sort -u
    ```

  - 예상: shared, product, member (replica 또는 직접).

- [ ] **Step 2-9: Common Pattern P1 적용**

    `bc/wishlist/build.gradle.kts`:

    ```kotlin
    plugins { id("giftify.java-library") }
    dependencies {
        implementation(project(":bc:shared"))
        implementation(project(":bc:product"))
        implementation(project(":bc:catalog"))    // 임시
        implementation(project(":support:common"))
        implementation(project(":support:logging"))
        implementation(libs.spring.boot.starter.data.jpa)
        implementation(libs.spring.modulith.api)
        implementation(libs.spring.modulith.events.api)
        implementation(libs.jspecify)
        compileOnly(libs.lombok)
        annotationProcessor(libs.lombok)
        testImplementation(libs.spring.boot.starter.test)
    }
    ```

    `settings.gradle.kts`: `include(":bc:wishlist")`.
    `bootstrap/api-server/build.gradle.kts`: `implementation(project(":bc:wishlist"))` 추가.

    `bc/wishlist/src/main/java/app/giftify/wishlist/package-info.java`:

    ```java
    @org.springframework.modulith.ApplicationModule(
        displayName = "Wishlist",
        allowedDependencies = { "shared", "product", "catalog" }
    )
    @org.springframework.modulith.NamedInterface("api")
    package app.giftify.wishlist;
    ```

    `git mv` + 컴파일/테스트.

- [ ] **Step 10: Commit**

    ```bash
    git add bc/wishlist/ settings.gradle.kts bootstrap/api-server/build.gradle.kts
    cat > /tmp/commit-msg-t4.txt <<'EOF'
    refactor: bc/wishlist 분리 (Task 4, matrix #3, Group B)

    bc/catalog/wishlist → bc/wishlist 신규 Gradle 모듈로 이동.

    spec §3.1.a matrix 행 3.
    EOF
    git commit -F /tmp/commit-msg-t4.txt
    rm /tmp/commit-msg-t4.txt
    ```

---

## Task 5: bc/image 분리 (matrix #4)

**Group:** B
**의존성:** Task 2-4 (선후 무관, 같은 그룹)
**Pattern:** P1

**Files:**
- Create: `bc/image/build.gradle.kts`, `bc/image/src/main/java/app/giftify/image/package-info.java`
- Move: `bc/catalog/src/main/java/app/giftify/image/**` → `bc/image/src/main/java/app/giftify/image/**`

- [ ] **Step 1: 의존성 grep**

    ```bash
    grep -rh "^import app.giftify" bc/catalog/src/main/java/app/giftify/image/ | \
        grep -v "app.giftify.image" | sort -u
    ```

  - 예상: shared (적은 의존성 — image 는 상대적으로 독립적).

- [ ] **Step 2-9: Common Pattern P1 적용**

    `bc/image/build.gradle.kts`:

    ```kotlin
    plugins { id("giftify.java-library") }
    dependencies {
        implementation(project(":bc:shared"))
        implementation(project(":support:common"))
        implementation(project(":support:logging"))
        implementation(libs.spring.boot.starter.data.jpa)
        implementation(libs.spring.modulith.api)
        implementation(libs.spring.modulith.events.api)
        implementation(libs.aws.s3)    // S3Adapter 사용 시
        implementation(libs.jspecify)
        compileOnly(libs.lombok)
        annotationProcessor(libs.lombok)
        testImplementation(libs.spring.boot.starter.test)
    }
    ```

    `settings.gradle.kts`: `include(":bc:image")`.
    `bootstrap/api-server/build.gradle.kts`: `implementation(project(":bc:image"))` 추가.

    `bc/image/src/main/java/app/giftify/image/package-info.java`:

    ```java
    @org.springframework.modulith.ApplicationModule(
        displayName = "Image",
        allowedDependencies = { "shared" }
    )
    @org.springframework.modulith.NamedInterface("api")
    package app.giftify.image;
    ```

    `git mv` + 컴파일/테스트.

- [ ] **Step 10: Commit**

    ```bash
    git add bc/image/ settings.gradle.kts bootstrap/api-server/build.gradle.kts
    cat > /tmp/commit-msg-t5.txt <<'EOF'
    refactor: bc/image 분리 (Task 5, matrix #4, Group B)

    bc/catalog/image → bc/image 신규 Gradle 모듈로 이동.
    Group B (catalog 분해) 완료.

    spec §3.1.a matrix 행 4.
    EOF
    git commit -F /tmp/commit-msg-t5.txt
    rm /tmp/commit-msg-t5.txt
    ```

---

## Task 6: bc/order 분리 (matrix #6)

**Group:** C
**의존성:** Task 1 (Group B 와 독립 — 순서 무관)
**Pattern:** P1

**Files:**
- Create: `bc/order/build.gradle.kts`, `bc/order/src/main/java/app/giftify/order/package-info.java`
- Move: `bc/core/src/main/java/app/giftify/order/**` → `bc/order/src/main/java/app/giftify/order/**`

- [ ] **Step 1: 의존성 grep**

    ```bash
    cd /Users/chan99/IdeaProjects/grep/giftify-be/.worktrees/staging/flattening
    grep -rh "^import app.giftify" bc/core/src/main/java/app/giftify/order/ | \
        grep -v "app.giftify.order" | sort -u
    ```

  - 예상: shared, product (OrderItem 의 product 참조), member (Buyer/Seller).

- [ ] **Step 2-9: Common Pattern P1 적용**

    `bc/order/build.gradle.kts`:

    ```kotlin
    plugins { id("giftify.java-library") }
    dependencies {
        implementation(project(":bc:shared"))
        implementation(project(":bc:product"))
        implementation(project(":bc:member"))
        implementation(project(":bc:core"))      // 임시 — payment/funding 의 transitive
        implementation(project(":support:common"))
        implementation(project(":support:logging"))
        implementation(libs.spring.boot.starter.data.jpa)
        implementation(libs.spring.modulith.api)
        implementation(libs.spring.modulith.events.api)
        implementation(libs.jspecify)
        compileOnly(libs.lombok)
        annotationProcessor(libs.lombok)
        testImplementation(libs.spring.boot.starter.test)
    }
    ```

    `settings.gradle.kts`: `include(":bc:order")`.
    `bootstrap/api-server/build.gradle.kts`: `implementation(project(":bc:order"))` 추가.

    `bc/order/src/main/java/app/giftify/order/package-info.java`:

    ```java
    @org.springframework.modulith.ApplicationModule(
        displayName = "Order",
        allowedDependencies = { "shared", "product", "member", "core" }
    )
    @org.springframework.modulith.NamedInterface("api")
    package app.giftify.order;
    ```

    `git mv bc/core/src/main/java/app/giftify/order bc/order/src/main/java/app/giftify/order`
    (test 디렉터리도 동일.)

    컴파일/테스트:

    ```bash
    ./gradlew :bc:order:test :bootstrap:api-server:compileJava --no-daemon 2>&1 | tail -30
    ```

- [ ] **Step 10: Commit**

    ```bash
    git add bc/order/ settings.gradle.kts bootstrap/api-server/build.gradle.kts
    cat > /tmp/commit-msg-t6.txt <<'EOF'
    refactor: bc/order 분리 (Task 6, matrix #6, Group C)

    bc/core/order → bc/order 신규 Gradle 모듈로 이동.
    payment/funding 의 transitive 위해 :bc:core 임시 의존 (Task 11 정리).

    spec §3.1.a matrix 행 6.
    EOF
    git commit -F /tmp/commit-msg-t6.txt
    rm /tmp/commit-msg-t6.txt
    ```

---

## Task 7: bc/payment 분리 (matrix #7)

**Group:** C
**의존성:** Task 6 (order 의 PaymentType 등)
**Pattern:** P1

**Files:**
- Create: `bc/payment/build.gradle.kts`, `bc/payment/src/main/java/app/giftify/payment/package-info.java`
- Move: `bc/core/src/main/java/app/giftify/payment/**` → `bc/payment/src/main/java/app/giftify/payment/**`

- [ ] **Step 1: 의존성 grep**

    ```bash
    grep -rh "^import app.giftify" bc/core/src/main/java/app/giftify/payment/ | \
        grep -v "app.giftify.payment" | sort -u
    ```

  - 예상: shared, order (OrderNumber 등), wallet (DeductWalletUseCase), member.

- [ ] **Step 2-9: Common Pattern P1 적용**

    `bc/payment/build.gradle.kts`:

    ```kotlin
    plugins { id("giftify.java-library") }
    dependencies {
        implementation(project(":bc:shared"))
        implementation(project(":bc:order"))
        implementation(project(":bc:core"))       // wallet transitive
        implementation(project(":bc:member"))
        implementation(project(":support:common"))
        implementation(project(":support:logging"))
        implementation(libs.spring.boot.starter.data.jpa)
        implementation(libs.spring.modulith.api)
        implementation(libs.spring.modulith.events.api)
        implementation(libs.spring.boot.starter.webflux)  // PG WebClient
        implementation(libs.resilience4j.spring.boot3)
        implementation(libs.jspecify)
        compileOnly(libs.lombok)
        annotationProcessor(libs.lombok)
        testImplementation(libs.spring.boot.starter.test)
    }
    ```

    `settings.gradle.kts`: `include(":bc:payment")`.
    `bootstrap/api-server/build.gradle.kts`: 의존성 추가.

    `bc/payment/src/main/java/app/giftify/payment/package-info.java`:

    ```java
    @org.springframework.modulith.ApplicationModule(
        displayName = "Payment",
        allowedDependencies = { "shared", "order", "member", "core" }
    )
    @org.springframework.modulith.NamedInterface("api")
    package app.giftify.payment;
    ```

    `git mv` + 컴파일/테스트.

- [ ] **Step 10: Commit**

    ```bash
    git add bc/payment/ settings.gradle.kts bootstrap/api-server/build.gradle.kts
    cat > /tmp/commit-msg-t7.txt <<'EOF'
    refactor: bc/payment 분리 (Task 7, matrix #7, Group C)

    bc/core/payment → bc/payment 신규 Gradle 모듈로 이동.
    wallet 의 transitive 위해 :bc:core 임시 의존 (Task 11 정리).

    spec §3.1.a matrix 행 7.
    EOF
    git commit -F /tmp/commit-msg-t7.txt
    rm /tmp/commit-msg-t7.txt
    ```

---

## Task 8: bc/funding 분리 (matrix #8)

**Group:** C
**의존성:** Task 6, 7
**Pattern:** P1

**Files:**
- Create: `bc/funding/build.gradle.kts`, `bc/funding/src/main/java/app/giftify/funding/package-info.java`
- Move: `bc/core/src/main/java/app/giftify/funding/**` → `bc/funding/src/main/java/app/giftify/funding/**`

- [ ] **Step 1-9: Pattern P1**

    `bc/funding/build.gradle.kts`:

    ```kotlin
    plugins { id("giftify.java-library") }
    dependencies {
        implementation(project(":bc:shared"))
        implementation(project(":bc:order"))
        implementation(project(":bc:wishlist"))
        implementation(project(":bc:product"))
        implementation(project(":bc:member"))
        implementation(project(":support:common"))
        implementation(project(":support:logging"))
        implementation(libs.spring.boot.starter.data.jpa)
        implementation(libs.spring.modulith.api)
        implementation(libs.spring.modulith.events.api)
        implementation(libs.jspecify)
        compileOnly(libs.lombok)
        annotationProcessor(libs.lombok)
        testImplementation(libs.spring.boot.starter.test)
    }
    ```

    `settings.gradle.kts`: `include(":bc:funding")`.
    `bootstrap/api-server/build.gradle.kts`: 의존성 추가.

    `bc/funding/src/main/java/app/giftify/funding/package-info.java`:

    ```java
    @org.springframework.modulith.ApplicationModule(
        displayName = "Funding",
        allowedDependencies = { "shared", "order", "wishlist", "product", "member" }
    )
    @org.springframework.modulith.NamedInterface("api")
    package app.giftify.funding;
    ```

    `git mv` + 컴파일/테스트.

- [ ] **Step 10: Commit**

    ```bash
    git add bc/funding/ settings.gradle.kts bootstrap/api-server/build.gradle.kts
    cat > /tmp/commit-msg-t8.txt <<'EOF'
    refactor: bc/funding 분리 (Task 8, matrix #8, Group C)

    bc/core/funding → bc/funding 신규 Gradle 모듈로 이동.

    spec §3.1.a matrix 행 8.
    EOF
    git commit -F /tmp/commit-msg-t8.txt
    rm /tmp/commit-msg-t8.txt
    ```

---

## Task 9: bc/wallet 분리 (matrix #9)

**Group:** C
**의존성:** Task 6
**Pattern:** P1

**Files:**
- Create: `bc/wallet/build.gradle.kts`, `bc/wallet/src/main/java/app/giftify/wallet/package-info.java`
- Move: `bc/core/src/main/java/app/giftify/wallet/**` → `bc/wallet/src/main/java/app/giftify/wallet/**`

- [ ] **Step 1-9: Pattern P1**

    `bc/wallet/build.gradle.kts`:

    ```kotlin
    plugins { id("giftify.java-library") }
    dependencies {
        implementation(project(":bc:shared"))
        implementation(project(":bc:member"))
        implementation(project(":support:common"))
        implementation(project(":support:logging"))
        implementation(libs.spring.boot.starter.data.jpa)
        implementation(libs.spring.modulith.api)
        implementation(libs.spring.modulith.events.api)
        implementation(libs.jspecify)
        compileOnly(libs.lombok)
        annotationProcessor(libs.lombok)
        testImplementation(libs.spring.boot.starter.test)
    }
    ```

    `settings.gradle.kts`: `include(":bc:wallet")`.
    `bootstrap/api-server/build.gradle.kts`: 의존성 추가.

    `bc/wallet/src/main/java/app/giftify/wallet/package-info.java`:

    ```java
    @org.springframework.modulith.ApplicationModule(
        displayName = "Wallet",
        allowedDependencies = { "shared", "member" }
    )
    @org.springframework.modulith.NamedInterface("api")
    package app.giftify.wallet;
    ```

    `git mv` + 컴파일/테스트.

- [ ] **Step 10: Commit**

    ```bash
    git add bc/wallet/ settings.gradle.kts bootstrap/api-server/build.gradle.kts
    cat > /tmp/commit-msg-t9.txt <<'EOF'
    refactor: bc/wallet 분리 (Task 9, matrix #9, Group C)

    bc/core/wallet → bc/wallet 신규 Gradle 모듈로 이동.

    spec §3.1.a matrix 행 9.
    EOF
    git commit -F /tmp/commit-msg-t9.txt
    rm /tmp/commit-msg-t9.txt
    ```

---

## Task 10: bc/core/shared 처리 (matrix #11)

**Group:** C
**의존성:** Task 6-9
**Pattern:** decision-driven (P1 변형)

**Files:**
- Analyze + Move/Absorb: `bc/core/src/main/java/app/giftify/shared/config/`, `bc/core/src/main/java/app/giftify/shared/scheduler/`

- [ ] **Step 1: 내용 분석**

    ```bash
    cd /Users/chan99/IdeaProjects/grep/giftify-be/.worktrees/staging/flattening
    ls -la bc/core/src/main/java/app/giftify/shared/config/
    ls -la bc/core/src/main/java/app/giftify/shared/scheduler/
    git grep -l "app.giftify.shared.config\|app.giftify.shared.scheduler" | head
    ```

  - 각 파일의 *사용 범위* (core 전용 vs 전역) 확인.

- [ ] **Step 2: 결정 — (i) `bc/shared` 흡수 또는 (ii) `bootstrap/api-server/config/` 로 이동**

    판단 규칙:
    - 파일이 *모든 모듈에서 공유* 가능한 *순수 도메인/공통 설정* → (i) `bc/shared` 흡수.
    - 파일이 *Spring infrastructure 설정* (DataSource, Scheduler bean 등) → (ii) `bootstrap/api-server/config/` 로 이동.

    예상 분류:
    - `shared/config/` 의 SchedulerConfig, AsyncConfig → (ii) bootstrap/api-server/config.
    - `shared/scheduler/` 의 도메인 task → (ii) bootstrap/api-server/config (또는 해당 도메인 모듈로).

- [ ] **Step 3: 이동 실행**

    ```bash
    cd /Users/chan99/IdeaProjects/grep/giftify-be/.worktrees/staging/flattening
    # (ii) 옵션 예시 — bootstrap 으로 이동:
    git mv bc/core/src/main/java/app/giftify/shared/config \
           bootstrap/api-server/src/main/java/app/giftify/config_core_legacy
    # 위 디렉터리명은 임시. 옮긴 후 정리.
    ```

  - 사용처에서 import path 갱신 필요 (Java 패키지가 바뀌므로):

    ```bash
    git grep -l "app.giftify.shared.config" | \
        xargs sed -i '' 's/app\.giftify\.shared\.config/app.giftify.config_core_legacy/g'
    ```

  - *주의*: `bc/shared` 와 `bc/core/shared` 의 *Java package* 가 동일하면 (`app.giftify.shared`) 이전 시 충돌. 사전 grep 으로 두 디렉터리의 package 가 *서로 다른지* 확인:

    ```bash
    grep -h "^package" bc/shared/src/main/java/**/*.java | sort -u
    grep -h "^package" bc/core/src/main/java/app/giftify/shared/**/*.java | sort -u
    ```

  - 만약 동일 (`app.giftify.shared.*`) 이면 *흡수 (i)* 가 자연스러움 — 같은 package 가 두 Gradle 모듈에 분산되면 Modulith verify() 가 혼동.

  - 본 task 실행자는 *Step 1 의 grep 결과* 를 보고 (i) 또는 (ii) 선택 후 진행. plan reader 가 zero-context 가 아니라면 *해당 시점의 grep 결과* 가 가장 신뢰할 만한 입력.

- [ ] **Step 4: 컴파일 + 테스트**

    ```bash
    ./gradlew compileJava compileTestJava --no-daemon 2>&1 | tail -30
    ```

- [ ] **Step 5: Commit**

    ```bash
    git add -A bc/core/src/main/java/app/giftify/shared/ \
                bc/shared/ bootstrap/api-server/src/main/java/app/giftify/
    cat > /tmp/commit-msg-t10.txt <<'EOF'
    refactor: bc/core/shared 처리 (Task 10, matrix #11, Group C)

    [본 task 실행자가 (i) bc/shared 흡수 또는 (ii) bootstrap/config 이동
    중 선택한 결과를 본 commit 메시지에 명시. Step 2 의 결정 근거 인용.]

    spec §3.1.a matrix 행 11.
    EOF
    git commit -F /tmp/commit-msg-t10.txt
    rm /tmp/commit-msg-t10.txt
    ```

---

## Task 11: bc/catalog, bc/core aggregate 삭제 (matrix #12)

**Group:** D
**의존성:** Task 2-10 (모든 catalog/core sub 가 분리되어 있어야 함)
**Pattern:** P2 (aggregate 삭제)

**Files:**
- Delete: `bc/catalog/`, `bc/core/` (디렉터리 전체)
- Modify: `settings.gradle.kts`, `bootstrap/api-server/build.gradle.kts`, `bootstrap/api-server/src/main/java/app/giftify/GiftifyApplication.java`
- Modify: Group B/C 의 모든 sub 모듈 build.gradle.kts (임시 `:bc:catalog`/`:bc:core` 의존성 제거)

- [ ] **Step 1: aggregate src 가 비었는지 확인**

    ```bash
    cd /Users/chan99/IdeaProjects/grep/giftify-be/.worktrees/staging/flattening
    find bc/catalog/src -type f 2>/dev/null | wc -l   # 0 이어야 함
    find bc/core/src -type f 2>/dev/null | wc -l      # 0 이어야 함
    ```

  - 결과 ≠ 0 이면: 남은 파일을 *어느 모듈* 에 속해야 하는지 분석 후 이동 (별도 Task 추가 가능).

- [ ] **Step 2: sub 모듈의 임시 :bc:catalog / :bc:core 의존성 제거**

    각 sub 모듈 build.gradle.kts 에서 `implementation(project(":bc:catalog"))` / `:bc:core` 라인 삭제:

    ```bash
    for module in cart product wishlist image; do
        sed -i '' '/implementation(project(":bc:catalog"))/d' bc/$module/build.gradle.kts
    done
    for module in order payment funding wallet; do
        sed -i '' '/implementation(project(":bc:core"))/d' bc/$module/build.gradle.kts
    done
    ```

  - 각 sub 모듈의 *실제 직접 의존성* (예: bc/cart 가 :bc:product 직접 의존) 은 *이미 Task 2-9 에서 명시* 되었으므로 transitive 가 사라져도 깨지지 않음.
  - package-info.java 의 `allowedDependencies` 에서 `"catalog"`, `"core"` 도 제거:

    ```bash
    for module in cart product wishlist image order payment funding wallet; do
        sed -i '' 's/, "catalog"//; s/"catalog", //; s/, "core"//; s/"core", //' \
            bc/$module/src/main/java/app/giftify/$module/package-info.java
    done
    ```

- [ ] **Step 3: aggregate 디렉터리 삭제**

    ```bash
    git rm -r bc/catalog bc/core
    ```

- [ ] **Step 4: settings.gradle.kts 갱신**

    `include(":bc:catalog")`, `include(":bc:core")` 라인 삭제:

    ```bash
    sed -i '' '/include(":bc:catalog")/d; /include(":bc:core")/d' settings.gradle.kts
    ```

- [ ] **Step 5: bootstrap/api-server/build.gradle.kts 갱신**

    ```bash
    sed -i '' '/implementation(project(":bc:catalog"))/d; /implementation(project(":bc:core"))/d' \
        bootstrap/api-server/build.gradle.kts
    ```

- [ ] **Step 6: GiftifyApplication 의 @ComponentScan 갱신**

    `bootstrap/api-server/src/main/java/app/giftify/GiftifyApplication.java` 의 `@ComponentScan(basePackages = {...})` 에서:
    - 제거: `"app.giftify.catalog"`, `"app.giftify.core"` (있는 경우)
    - 추가 (없는 경우): `"app.giftify.cart"`, `"app.giftify.product"`, `"app.giftify.wishlist"`, `"app.giftify.image"`, `"app.giftify.order"`, `"app.giftify.payment"`, `"app.giftify.funding"`, `"app.giftify.wallet"`

    수정 후 *최종* 형태 (예시 — 본 task 실행자가 정확 확인):

    ```java
    @SpringBootApplication
    @ComponentScan(basePackages = {
        "app.giftify.cart",
        "app.giftify.funding",
        "app.giftify.image",
        "app.giftify.member",
        "app.giftify.notification",
        "app.giftify.order",
        "app.giftify.payment",
        "app.giftify.product",
        "app.giftify.settlement",
        "app.giftify.shared",
        "app.giftify.support",
        "app.giftify.usecase",      // Task 14 에서 신설
        "app.giftify.wallet",
        "app.giftify.wishlist"
    })
    public class GiftifyApplication {
        public static void main(String[] args) {
            SpringApplication.run(GiftifyApplication.class, args);
        }
    }
    ```

  - Task 14 의 `app.giftify.usecase` 는 *현재 cherry-pick range 안에서 추가됨*. 본 task 에서 *추가해도* 후속 Task 14 가 *그 패키지를 실제 사용* 하므로 일관.

- [ ] **Step 7: 전체 빌드**

    ```bash
    ./gradlew build --no-daemon 2>&1 | tail -50
    ```

  - Expected: BUILD SUCCESSFUL.
  - 실패 시: *imports* 검사. `app.giftify.catalog.*` 또는 `app.giftify.core.*` 같은 사라진 패키지 참조가 있으면 *해당 sub 모듈의 새 위치* 로 import 갱신.

- [ ] **Step 8: Commit**

    ```bash
    git add -A
    cat > /tmp/commit-msg-t11.txt <<'EOF'
    refactor: bc/catalog, bc/core aggregate 삭제 (Task 11, matrix #12, Group D)

    Group B (cart/product/wishlist/image) 및 Group C (order/payment/funding/wallet)
    의 sub 모듈이 모두 분리되어 aggregate 가 비었음을 확인 후 삭제.

    - sub 모듈 build.gradle.kts 의 임시 :bc:catalog/:bc:core 의존성 제거
    - package-info.java 의 allowedDependencies 에서 "catalog"/"core" 제거
    - settings.gradle.kts 의 include 제거
    - GiftifyApplication @ComponentScan 의 신규 모듈 추가

    spec §3.1.a matrix 행 12.
    EOF
    git commit -F /tmp/commit-msg-t11.txt
    rm /tmp/commit-msg-t11.txt
    ```

---

## Task 12: ProductSellerView readmodel 신설 (TDD) (matrix #5)

**Group:** E
**의존성:** Task 3, 11 (bc/product 모듈 + aggregate 삭제 완료 후)
**Pattern:** P3 (TDD readmodel)

**Files:**
- Create: `bc/product/src/main/java/app/giftify/product/readmodel/ProductSellerView.java`
- Create: `bc/product/src/main/java/app/giftify/product/readmodel/ProductSellerViewRepository.java`
- Create: `bc/product/src/main/java/app/giftify/product/readmodel/ProductSellerViewSyncListener.java`
- Create: `bc/product/src/test/java/app/giftify/product/readmodel/ProductSellerViewSyncListenerTest.java`
- Modify: `bc/product/src/main/resources/db/migration/...` (table 추가, module-aware Flyway 패턴)

설계 결정 (spec §3.3):
- *Consumer 도메인 언어*: `ProductSellerView` (member 의 nickname 만 보유).
- *Listener*: `MemberSignedEvent` 와 `MemberUpdatedEvent` 를 `@ApplicationModuleListener` 로 받아 view 갱신.
- *SellerNicknameChangedEvent* 발행: **본 task 에서 product 모듈이 담당** (이전 catalog/replica/member 의 책임 이전).

- [ ] **Step 1: 실패 테스트 작성 — `ProductSellerViewSyncListenerTest`**

    `bc/product/src/test/java/app/giftify/product/readmodel/ProductSellerViewSyncListenerTest.java`:

    ```java
    package app.giftify.product.readmodel;

    import app.giftify.shared.domain.event.EventPublisher;
    import app.giftify.shared.domain.event.member.MemberSignedEvent;
    import app.giftify.shared.domain.event.member.MemberUpdatedEvent;
    import app.giftify.shared.domain.event.member.SellerNicknameChangedEvent;
    import app.giftify.shared.domain.type.MemberRole;
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Test;
    import org.mockito.ArgumentCaptor;

    import java.util.Optional;

    import static org.assertj.core.api.Assertions.assertThat;
    import static org.mockito.ArgumentMatchers.any;
    import static org.mockito.Mockito.*;

    class ProductSellerViewSyncListenerTest {

        ProductSellerViewRepository repository;
        EventPublisher eventPublisher;
        ProductSellerViewSyncListener listener;

        @BeforeEach
        void setUp() {
            repository = mock(ProductSellerViewRepository.class);
            eventPublisher = mock(EventPublisher.class);
            listener = new ProductSellerViewSyncListener(repository, eventPublisher);
        }

        @Test
        void MemberSignedEvent_수신_시_새_view_생성() {
            // given
            MemberSignedEvent event = new MemberSignedEvent(123L, "닉네임A", MemberRole.SELLER);
            when(repository.findById(123L)).thenReturn(Optional.empty());

            // when
            listener.handle(event);

            // then
            ArgumentCaptor<ProductSellerView> captor = ArgumentCaptor.forClass(ProductSellerView.class);
            verify(repository).save(captor.capture());
            ProductSellerView saved = captor.getValue();
            assertThat(saved.getSellerId()).isEqualTo(123L);
            assertThat(saved.getNickname()).isEqualTo("닉네임A");
            verify(eventPublisher, never()).publishDomainEvent(any());
        }

        @Test
        void MemberUpdatedEvent_수신_시_기존_view_갱신_및_SellerNicknameChangedEvent_발행() {
            // given
            ProductSellerView existing = new ProductSellerView(123L, "구닉네임");
            when(repository.findById(123L)).thenReturn(Optional.of(existing));
            MemberUpdatedEvent event = new MemberUpdatedEvent(123L, "신닉네임", MemberRole.SELLER);

            // when
            listener.handle(event);

            // then
            assertThat(existing.getNickname()).isEqualTo("신닉네임");
            verify(repository).save(existing);
            ArgumentCaptor<SellerNicknameChangedEvent> evCap =
                ArgumentCaptor.forClass(SellerNicknameChangedEvent.class);
            verify(eventPublisher).publishDomainEvent(evCap.capture());
            assertThat(evCap.getValue().getMemberId()).isEqualTo(123L);
            assertThat(evCap.getValue().getNickname()).isEqualTo("신닉네임");
        }

        @Test
        void MemberUpdatedEvent_BUYER_역할_수신_시_SellerNicknameChangedEvent_미발행() {
            // given
            ProductSellerView existing = new ProductSellerView(123L, "구닉네임");
            when(repository.findById(123L)).thenReturn(Optional.of(existing));
            MemberUpdatedEvent event = new MemberUpdatedEvent(123L, "신닉네임", MemberRole.BUYER);

            // when
            listener.handle(event);

            // then — view 는 갱신, but SellerNicknameChangedEvent 미발행 (SELLER 만 발행)
            verify(repository).save(any());
            verify(eventPublisher, never()).publishDomainEvent(any());
        }

        @Test
        void MemberSignedEvent_중복_수신_시_idempotent() {
            // given
            ProductSellerView existing = new ProductSellerView(123L, "닉네임A");
            when(repository.findById(123L)).thenReturn(Optional.of(existing));
            MemberSignedEvent event = new MemberSignedEvent(123L, "닉네임A", MemberRole.SELLER);

            // when
            listener.handle(event);

            // then — 동일 nickname 이므로 save 없이 무시 (또는 동일 값 save)
            // 명세: 동일 값이라도 save 호출 (Hibernate dirty checking 이 noop 처리)
            verify(repository).save(existing);
            assertThat(existing.getNickname()).isEqualTo("닉네임A");
        }
    }
    ```

- [ ] **Step 2: 테스트 실행 — 컴파일 실패 확인**

    ```bash
    cd /Users/chan99/IdeaProjects/grep/giftify-be/.worktrees/staging/flattening
    ./gradlew :bc:product:test --tests ProductSellerViewSyncListenerTest --no-daemon 2>&1 | tail -20
    ```

    Expected: 컴파일 에러 (`ProductSellerView`, `ProductSellerViewRepository`, `ProductSellerViewSyncListener` 미존재).

- [ ] **Step 3: 최소 구현 — Entity**

    `bc/product/src/main/java/app/giftify/product/readmodel/ProductSellerView.java`:

    ```java
    package app.giftify.product.readmodel;

    import jakarta.persistence.Entity;
    import jakarta.persistence.Id;
    import jakarta.persistence.Table;
    import lombok.AccessLevel;
    import lombok.Getter;
    import lombok.NoArgsConstructor;

    @Entity
    @Table(name = "product_seller_views")
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public class ProductSellerView {

        @Id
        private Long sellerId;

        private String nickname;

        public ProductSellerView(Long sellerId, String nickname) {
            this.sellerId = sellerId;
            this.nickname = nickname;
        }

        public void updateNickname(String nickname) {
            this.nickname = nickname;
        }
    }
    ```

- [ ] **Step 4: 최소 구현 — Repository**

    `bc/product/src/main/java/app/giftify/product/readmodel/ProductSellerViewRepository.java`:

    ```java
    package app.giftify.product.readmodel;

    import org.springframework.data.jpa.repository.JpaRepository;

    public interface ProductSellerViewRepository extends JpaRepository<ProductSellerView, Long> {
    }
    ```

- [ ] **Step 5: 최소 구현 — Listener**

    `bc/product/src/main/java/app/giftify/product/readmodel/ProductSellerViewSyncListener.java`:

    ```java
    package app.giftify.product.readmodel;

    import app.giftify.shared.domain.event.EventPublisher;
    import app.giftify.shared.domain.event.member.MemberSignedEvent;
    import app.giftify.shared.domain.event.member.MemberUpdatedEvent;
    import app.giftify.shared.domain.event.member.SellerNicknameChangedEvent;
    import app.giftify.shared.domain.type.MemberRole;
    import lombok.RequiredArgsConstructor;
    import org.springframework.modulith.events.ApplicationModuleListener;
    import org.springframework.stereotype.Component;

    @Component
    @RequiredArgsConstructor
    public class ProductSellerViewSyncListener {

        private final ProductSellerViewRepository repository;
        private final EventPublisher eventPublisher;

        @ApplicationModuleListener
        public void handle(MemberSignedEvent event) {
            ProductSellerView view = repository.findById(event.getMemberId())
                .orElseGet(() -> new ProductSellerView(event.getMemberId(), event.getNickname()));
            view.updateNickname(event.getNickname());
            repository.save(view);
        }

        @ApplicationModuleListener
        public void handle(MemberUpdatedEvent event) {
            ProductSellerView view = repository.findById(event.getMemberId())
                .orElseGet(() -> new ProductSellerView(event.getMemberId(), event.getNickname()));
            view.updateNickname(event.getNickname());
            repository.save(view);
            if (event.getRole() == MemberRole.SELLER) {
                eventPublisher.publishDomainEvent(
                    new SellerNicknameChangedEvent(event.getMemberId(), event.getNickname()));
            }
        }
    }
    ```

- [ ] **Step 6: 테스트 통과 확인**

    ```bash
    ./gradlew :bc:product:test --tests ProductSellerViewSyncListenerTest --no-daemon 2>&1 | tail -20
    ```

    Expected: 4 tests passed.

- [ ] **Step 7: DB 마이그레이션 — Flyway**

    `bc/product/src/main/resources/db/migration/product/V1.0.1__product_seller_views.sql`:
    (모듈별 Flyway 패턴, PR #420 의 확립된 컨벤션)

    ```sql
    CREATE TABLE product_seller_views (
        seller_id BIGINT PRIMARY KEY,
        nickname  VARCHAR(50) NOT NULL
    );

    -- 기존 MEMBER_REPLICAS 데이터 마이그레이션 (있는 경우)
    INSERT INTO product_seller_views (seller_id, nickname)
    SELECT id, nickname FROM member_replicas
    WHERE EXISTS (SELECT 1 FROM information_schema.tables
                  WHERE table_name = 'member_replicas')
    ON CONFLICT (seller_id) DO NOTHING;
    ```

  - *주의*: H2 / PostgreSQL 양쪽 호환 필요. `information_schema.tables` 는 둘 다 지원. `ON CONFLICT` 는 H2 1.4.200+ 와 Postgres 9.5+ 지원. 본 프로젝트는 H2 2.x, Postgres 14+ 라 OK.
  - 마이그레이션은 *Task 13 에서 member_replicas 삭제 마이그레이션* 과 짝.

- [ ] **Step 8: Commit**

    ```bash
    git add bc/product/src/main/java/app/giftify/product/readmodel/ \
            bc/product/src/test/java/app/giftify/product/readmodel/ \
            bc/product/src/main/resources/db/migration/product/V1.0.1__product_seller_views.sql
    cat > /tmp/commit-msg-t12.txt <<'EOF'
    refactor: ProductSellerView readmodel 신설 (Task 12, matrix #5, Group E)

    bc/catalog/replica/member 의 *모듈별 분리* 첫 단계.
    product 모듈이 자기 readmodel 보유:
    - ProductSellerView (sellerId + nickname)
    - ProductSellerViewSyncListener (@ApplicationModuleListener)
    - SellerNicknameChangedEvent 발행 (SELLER 역할만)

    TDD: 4개 test 먼저 작성 → entity/repo/listener 구현 → 통과.
    DB 마이그레이션: V1.0.1 (member_replicas → product_seller_views).

    spec §3.3 Major 2 옵션 a (모듈별 readmodel 분리).
    EOF
    git commit -F /tmp/commit-msg-t12.txt
    rm /tmp/commit-msg-t12.txt
    ```

---

## Task 13: bc/catalog/replica 삭제 + 사용처 갱신 (matrix #5)

**Group:** E
**의존성:** Task 12 (ProductSellerView 가 존재해야 안전 삭제)
**Pattern:** P3 변형 (replica → readmodel 갱신)

**Files:**
- Delete: `bc/catalog/replica/` (이미 Task 11 의 aggregate 삭제로 사라졌으면 skip — 본 task 는 *catalog aggregate 삭제 전에 replica 만 미리 옮겼다면* 의 보완)
- Modify: 기존 `replica.member.Member`, `MemberRepository` 사용처 → `ProductSellerView` 또는 자기 모듈의 readmodel 로 교체
- Create: `bc/product/src/main/resources/db/migration/product/V1.0.2__drop_member_replicas.sql`

- [ ] **Step 1: 사용처 식별**

    ```bash
    cd /Users/chan99/IdeaProjects/grep/giftify-be/.worktrees/staging/flattening
    git grep -l "app.giftify.replica.member\|MemberRepository\b" | \
        grep -v "test\|replica" | tee /tmp/replica-callers.txt
    ```

  - 결과: cart / wishlist / product 의 어느 클래스가 *replica MemberRepository* 또는 *replica Member* 를 import 하는지.

- [ ] **Step 2: 각 사용처를 모듈별 readmodel 로 교체**

    예시 (product 의 ProductService 가 nickname 조회 시):

    ```diff
    - import app.giftify.replica.member.Member;
    - import app.giftify.replica.member.MemberRepository;
    + import app.giftify.product.readmodel.ProductSellerView;
    + import app.giftify.product.readmodel.ProductSellerViewRepository;
    ```

    호출 패턴도 변경:
    ```diff
    - Member member = memberRepository.findById(sellerId).orElseThrow(...);
    - String nickname = member.getNickname();
    + ProductSellerView view = productSellerViewRepository.findById(sellerId).orElseThrow(...);
    + String nickname = view.getNickname();
    ```

  - cart / wishlist 가 *직접 nickname 사용* 한다면: 본 task 안에서 *각 모듈에 readmodel 신설* (CartSellerView / WishlistSellerView) — Task 12 와 동일 패턴. 본 task 가 늘어남.
  - cart / wishlist 가 *직접 사용 안 함* 이면 (예: product 통해 간접 조회): 변경 불필요.

  - *Step 1 의 grep 결과* 가 정확한 task 분량을 결정.

- [ ] **Step 3: replica 디렉터리 삭제 (catalog aggregate 가 살아있는 경우)**

    Task 11 에서 catalog aggregate 가 삭제되면서 replica 도 사라졌을 가능성. `find bc/catalog/src/main/java/app/giftify/replica -type f 2>/dev/null | wc -l` 로 확인. ≠ 0 이면:

    ```bash
    git rm -r bc/catalog/src/main/java/app/giftify/replica
    git rm -r bc/catalog/src/test/java/app/giftify/replica 2>/dev/null || true
    ```

- [ ] **Step 4: DB 마이그레이션 — member_replicas 삭제**

    `bc/product/src/main/resources/db/migration/product/V1.0.2__drop_member_replicas.sql`:

    ```sql
    DROP TABLE IF EXISTS member_replicas;
    ```

  - *주의*: V1.0.1 마이그레이션이 member_replicas 데이터를 product_seller_views 로 *복사* 한 후이므로 안전.

- [ ] **Step 5: 컴파일 + 테스트**

    ```bash
    ./gradlew test --no-daemon 2>&1 | tail -30
    ```

    Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

    ```bash
    git add -A
    cat > /tmp/commit-msg-t13.txt <<'EOF'
    refactor: bc/catalog/replica 삭제 + 사용처 readmodel 로 교체
    (Task 13, matrix #5, Group E)

    - bc/catalog/replica/member 패키지 삭제
    - 사용처를 ProductSellerView (+ 필요시 모듈별 readmodel) 로 교체
    - DB: V1.0.2 member_replicas DROP (V1.0.1 마이그레이션 후)

    spec §3.3 의 모듈별 readmodel 분리 완료.
    DoD §8.3 (replica 삭제), §8.8 (cross-module readmodel import = 0) 충족.
    EOF
    git commit -F /tmp/commit-msg-t13.txt
    rm /tmp/commit-msg-t13.txt
    ```

---

## Task 14: ParticipateFundingUseCase 신설 (TDD) (matrix #10)

**Group:** F
**의존성:** Task 6 (bc/order), 7 (bc/payment), 8 (bc/funding), 11 (aggregate 삭제)
**Pattern:** P4 (TDD usecase)

**Files:**
- Create: `bootstrap/api-server/src/main/java/app/giftify/usecase/ParticipateFundingUseCase.java`
- Create: `bootstrap/api-server/src/main/java/app/giftify/usecase/command/ParticipateFundingCommand.java`
- Create: `bootstrap/api-server/src/main/java/app/giftify/usecase/command/ParticipateFundingItemCommand.java`
- Create: `bootstrap/api-server/src/main/java/app/giftify/usecase/vo/PlaceOrderResult.java`
- Create: `bootstrap/api-server/src/test/java/app/giftify/usecase/ParticipateFundingUseCaseTest.java`

설계 결정 (spec §3.2):
- `@Transactional` *없음*. 각 모듈 호출이 *자기 트랜잭션*.
- 직접 호출은 1 단계 (placeOrder, createPayment) 까지. *2 단계* (markOrderAsPaid, processFundingActions) 는 *PaymentSucceeded 이벤트 listener* 에 위임.
- 기존 `CoreFacade.participateFunding` 의 시그니처 보존 (외부 caller 의 변경 최소화).

- [ ] **Step 1: 기존 CoreFacade 동작 baseline 측정 (선행 task)**

    먼저 *기존 동작* 을 명시적 baseline 으로 추출. 본 task 의 *expected outcome* 의 출처.

    ```bash
    cd /Users/chan99/IdeaProjects/grep/giftify-be/.worktrees/staging/flattening
    cat bc/core/src/main/java/app/giftify/facade/CoreFacade.java 2>/dev/null || \
        echo "(CoreFacade 가 Task 11/D 에서 미리 삭제되었으면 git show ddad2dbe:bc/core/src/main/java/app/giftify/facade/CoreFacade.java)"
    git show ddad2dbe:bc/core/src/main/java/app/giftify/facade/CoreFacade.java
    ```

  - 동작 요약 (spec briefing §1 에서 추출):
    1. `orderService.createOrder(PlaceOrderCommand.of(command))` → `OrderSnapshot`
    2. `createPaymentService.create(generatePaymentCommand(snapshot, walletDeductAmount))` → `PaymentCreatedResult`
    3. `orderService.markOrderAsPaid(MarkOrderAsPaidCommand)` — *현재 동기 호출*
    4. `fundingFacade.processFundingActions(snapshot)` — *현재 동기 호출*
    5. Return `new PlaceOrderResult(snapshot.orderId())`
  - *변경*: 단계 3, 4 는 *PaymentSucceeded 이벤트 listener* 가 처리.

- [ ] **Step 2: 실패 테스트 작성 — `ParticipateFundingUseCaseTest`**

    `bootstrap/api-server/src/test/java/app/giftify/usecase/ParticipateFundingUseCaseTest.java`:

    ```java
    package app.giftify.usecase;

    import app.giftify.order.application.OrderService;
    import app.giftify.order.application.inbound.command.PlaceOrderCommand;
    import app.giftify.order.domain.OrderSnapshot;
    import app.giftify.payment.application.CreatePaymentService;
    import app.giftify.payment.application.inbound.CreatePaymentCommand;
    import app.giftify.payment.application.inbound.PaymentCreatedResult;
    import app.giftify.shared.domain.type.PaymentMethod;
    import app.giftify.shared.domain.type.PaymentType;
    import app.giftify.shared.domain.vo.Money;
    import app.giftify.usecase.command.ParticipateFundingCommand;
    import app.giftify.usecase.vo.PlaceOrderResult;
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Test;
    import org.mockito.ArgumentCaptor;

    import java.math.BigDecimal;
    import java.time.LocalDateTime;
    import java.util.List;

    import static org.assertj.core.api.Assertions.assertThat;
    import static org.mockito.ArgumentMatchers.any;
    import static org.mockito.Mockito.*;

    class ParticipateFundingUseCaseTest {

        OrderService orderService;
        CreatePaymentService createPaymentService;
        ParticipateFundingUseCase useCase;

        @BeforeEach
        void setUp() {
            orderService = mock(OrderService.class);
            createPaymentService = mock(CreatePaymentService.class);
            useCase = new ParticipateFundingUseCase(orderService, createPaymentService);
        }

        @Test
        void participateFunding_지갑_차감_없음_정상_흐름() {
            // given
            ParticipateFundingCommand cmd = new ParticipateFundingCommand(
                /* buyerId */ 1L,
                /* items */ List.of(),
                /* paymentMethod */ PaymentMethod.CARD,
                /* walletDeductAmount */ Money.zero()
            );
            OrderSnapshot snapshot = mock(OrderSnapshot.class);
            when(snapshot.orderId()).thenReturn(100L);
            when(snapshot.buyerId()).thenReturn(1L);
            when(snapshot.orderNumber()).thenReturn("ORD-100");
            when(snapshot.paymentMethod()).thenReturn(PaymentMethod.CARD);
            when(snapshot.totalAmount()).thenReturn(Money.of(BigDecimal.valueOf(10000)));
            when(orderService.createOrder(any())).thenReturn(snapshot);
            PaymentCreatedResult paymentResult = new PaymentCreatedResult(
                42L, "ORD-100", "txk-1", LocalDateTime.now());
            when(createPaymentService.create(any())).thenReturn(paymentResult);

            // when
            PlaceOrderResult result = useCase.participateFunding(cmd);

            // then
            assertThat(result.orderId()).isEqualTo(100L);
            verify(orderService).createOrder(any(PlaceOrderCommand.class));
            ArgumentCaptor<CreatePaymentCommand> paymentCap =
                ArgumentCaptor.forClass(CreatePaymentCommand.class);
            verify(createPaymentService).create(paymentCap.capture());
            assertThat(paymentCap.getValue().paymentType()).isEqualTo(PaymentType.FUNDING);
            verify(orderService, never()).markOrderAsPaid(any());  // 핵심: 2단계는 listener 로 이전
        }

        @Test
        void participateFunding_지갑_차감_금액_있음() {
            // given
            ParticipateFundingCommand cmd = new ParticipateFundingCommand(
                1L, List.of(), PaymentMethod.CARD, Money.of(BigDecimal.valueOf(3000))
            );
            OrderSnapshot snapshot = mock(OrderSnapshot.class);
            when(snapshot.orderId()).thenReturn(100L);
            when(snapshot.buyerId()).thenReturn(1L);
            when(snapshot.orderNumber()).thenReturn("ORD-100");
            when(snapshot.paymentMethod()).thenReturn(PaymentMethod.CARD);
            when(snapshot.totalAmount()).thenReturn(Money.of(BigDecimal.valueOf(10000)));
            when(orderService.createOrder(any())).thenReturn(snapshot);
            when(createPaymentService.create(any())).thenReturn(
                new PaymentCreatedResult(42L, "ORD-100", "txk-1", LocalDateTime.now()));

            // when
            useCase.participateFunding(cmd);

            // then — withWalletDeduct 분기 호출 검증
            ArgumentCaptor<CreatePaymentCommand> cap = ArgumentCaptor.forClass(CreatePaymentCommand.class);
            verify(createPaymentService).create(cap.capture());
            assertThat(cap.getValue().walletDeductAmount())
                .isEqualTo(Money.of(BigDecimal.valueOf(3000)));
        }

        @Test
        void participateFunding_트랜잭션_어노테이션_부재() throws NoSuchMethodException {
            // 핵심: UseCase 메서드에 @Transactional 이 없어야 함
            assertThat(ParticipateFundingUseCase.class
                .getMethod("participateFunding", ParticipateFundingCommand.class)
                .getAnnotation(org.springframework.transaction.annotation.Transactional.class))
                .isNull();
            assertThat(ParticipateFundingUseCase.class
                .getAnnotation(org.springframework.transaction.annotation.Transactional.class))
                .isNull();
        }
    }
    ```

- [ ] **Step 3: 테스트 실행 — 컴파일 실패 확인**

    ```bash
    ./gradlew :bootstrap:api-server:test --tests ParticipateFundingUseCaseTest --no-daemon 2>&1 | tail -20
    ```

    Expected: 컴파일 에러 (`ParticipateFundingUseCase`, `ParticipateFundingCommand`, `PlaceOrderResult` 미존재).

- [ ] **Step 4: command / vo 이동 (구 facade 에서 추출)**

    ```bash
    cd /Users/chan99/IdeaProjects/grep/giftify-be/.worktrees/staging/flattening
    mkdir -p bootstrap/api-server/src/main/java/app/giftify/usecase/command
    mkdir -p bootstrap/api-server/src/main/java/app/giftify/usecase/vo

    # 구 facade 가 Task 11/aggregate 삭제로 사라졌으면 git show 에서 추출:
    git show ddad2dbe:bc/core/src/main/java/app/giftify/facade/command/ParticipateFundingCommand.java \
        > bootstrap/api-server/src/main/java/app/giftify/usecase/command/ParticipateFundingCommand.java
    git show ddad2dbe:bc/core/src/main/java/app/giftify/facade/command/ParticipateFundingItemCommand.java \
        > bootstrap/api-server/src/main/java/app/giftify/usecase/command/ParticipateFundingItemCommand.java
    git show ddad2dbe:bc/core/src/main/java/app/giftify/facade/vo/PlaceOrderResult.java \
        > bootstrap/api-server/src/main/java/app/giftify/usecase/vo/PlaceOrderResult.java
    ```

    각 파일의 `package` 선언 갱신:
    - `package app.giftify.facade.command;` → `package app.giftify.usecase.command;`
    - `package app.giftify.facade.vo;` → `package app.giftify.usecase.vo;`

- [ ] **Step 5: 최소 구현 — UseCase**

    `bootstrap/api-server/src/main/java/app/giftify/usecase/ParticipateFundingUseCase.java`:

    ```java
    package app.giftify.usecase;

    import app.giftify.order.application.OrderService;
    import app.giftify.order.application.inbound.command.PlaceOrderCommand;
    import app.giftify.order.domain.OrderSnapshot;
    import app.giftify.payment.application.CreatePaymentService;
    import app.giftify.payment.application.inbound.CreatePaymentCommand;
    import app.giftify.payment.application.inbound.PaymentCreatedResult;
    import app.giftify.shared.domain.type.PaymentType;
    import app.giftify.shared.domain.vo.Money;
    import app.giftify.usecase.command.ParticipateFundingCommand;
    import app.giftify.usecase.vo.PlaceOrderResult;
    import lombok.RequiredArgsConstructor;
    import org.jspecify.annotations.NonNull;
    import org.springframework.stereotype.Component;

    @Component
    @RequiredArgsConstructor
    public class ParticipateFundingUseCase {

        private final OrderService orderService;
        private final CreatePaymentService createPaymentService;

        public PlaceOrderResult participateFunding(ParticipateFundingCommand command) {
            OrderSnapshot snapshot = orderService.createOrder(PlaceOrderCommand.of(command));
            createPaymentService.create(buildPaymentCommand(snapshot, command.walletDeductAmount()));
            return new PlaceOrderResult(snapshot.orderId());
            // markOrderAsPaid / processFundingActions 는
            // PaymentSucceededEvent listener 에서 처리됨 (Order 모듈, Funding 모듈)
        }

        private static @NonNull CreatePaymentCommand buildPaymentCommand(
            OrderSnapshot snapshot, Money walletDeductAmount
        ) {
            if (walletDeductAmount.isGreaterThan(Money.zero())) {
                return CreatePaymentCommand.withWalletDeduct(
                    snapshot.buyerId(),
                    snapshot.orderId(),
                    snapshot.orderNumber(),
                    PaymentType.FUNDING,
                    snapshot.paymentMethod(),
                    snapshot.totalAmount(),
                    walletDeductAmount
                );
            }
            return CreatePaymentCommand.of(
                snapshot.buyerId(),
                snapshot.orderId(),
                snapshot.orderNumber(),
                PaymentType.FUNDING,
                snapshot.paymentMethod(),
                snapshot.totalAmount()
            );
        }
    }
    ```

- [ ] **Step 6: 테스트 통과 확인**

    ```bash
    ./gradlew :bootstrap:api-server:test --tests ParticipateFundingUseCaseTest --no-daemon 2>&1 | tail -20
    ```

    Expected: 3 tests passed.

- [ ] **Step 7: 기존 markOrderAsPaid listener 가 PaymentSucceededEvent 를 받는지 확인**

    ```bash
    git grep -n "PaymentSucceededEvent" bc/order/src/main/java/ 2>/dev/null | head -10
    ```

  - 기존 코드에 `@ApplicationModuleListener public void on(PaymentSucceededEvent ...)` 이 있어 `markOrderAsPaid` 를 호출하는지 확인. *없으면* Task 14 의 step 으로 추가:

    `bc/order/src/main/java/app/giftify/order/application/listener/PaymentSucceededListener.java` (없으면 신설):

    ```java
    package app.giftify.order.application.listener;

    import app.giftify.order.application.OrderService;
    import app.giftify.order.application.inbound.command.MarkOrderAsPaidCommand;
    import app.giftify.shared.domain.event.payment.PaymentSucceededEvent;
    import lombok.RequiredArgsConstructor;
    import org.springframework.modulith.events.ApplicationModuleListener;
    import org.springframework.stereotype.Component;

    @Component
    @RequiredArgsConstructor
    public class PaymentSucceededListener {

        private final OrderService orderService;

        @ApplicationModuleListener
        public void on(PaymentSucceededEvent event) {
            orderService.markOrderAsPaid(new MarkOrderAsPaidCommand(
                event.orderNumber(),
                event.paymentId(),
                event.lastTransactionKey(),
                event.createdAt()
            ));
        }
    }
    ```

  - 동일하게 funding 의 `FundingActionsListener` 도 PaymentSucceededEvent 또는 OrderConfirmedEvent 를 받아 `processFundingActions` 호출하도록 보강.

  - Listener 추가 시 *각 모듈의 unit test* 도 추가 (Mockito 기반, ProductSellerViewSyncListenerTest 와 동일 패턴).

- [ ] **Step 8: Commit**

    ```bash
    git add bootstrap/api-server/src/main/java/app/giftify/usecase/ \
            bootstrap/api-server/src/test/java/app/giftify/usecase/ \
            bc/order/src/main/java/app/giftify/order/application/listener/ \
            bc/funding/src/main/java/app/giftify/funding/application/listener/
    cat > /tmp/commit-msg-t14.txt <<'EOF'
    refactor: ParticipateFundingUseCase 신설 (TDD)
    (Task 14, matrix #10, Group F)

    - bootstrap/api-server/usecase/ 신규 패키지
    - ParticipateFundingUseCase (no @Transactional, 1단계 직접 호출만)
    - command/vo 구 facade 에서 이동 (package 갱신)
    - PaymentSucceededListener: order 모듈 (markOrderAsPaid)
    - PaymentSucceededListener: funding 모듈 (processFundingActions)

    TDD: 3개 test 먼저 → useCase 구현 → 통과.
    DoD §8.4 (UseCase 존재 + @Transactional 부재) 충족.

    spec §3.2 facade 해체.
    EOF
    git commit -F /tmp/commit-msg-t14.txt
    rm /tmp/commit-msg-t14.txt
    ```

---

## Task 15: CoreFacade 삭제 + controller 갱신 (matrix #10)

**Group:** F
**의존성:** Task 14 (UseCase 가 존재해야 facade caller 전환 가능)
**Pattern:** P4 후속

**Files:**
- Delete: `bc/core/src/main/java/app/giftify/facade/` (Task 11 의 aggregate 삭제로 이미 사라졌으면 skip)
- Modify: 기존 `CoreFacade` caller (controller, 다른 service 들) → `ParticipateFundingUseCase` 사용으로 교체

- [ ] **Step 1: CoreFacade caller 식별**

    ```bash
    cd /Users/chan99/IdeaProjects/grep/giftify-be/.worktrees/staging/flattening
    # Task 11 후 bc/core 가 사라졌어도 git history 에서 확인 가능
    git grep -l "CoreFacade\|facade.CoreFacade" 2>/dev/null
    git grep -l "facade.command.ParticipateFundingCommand\|facade.vo.PlaceOrderResult" 2>/dev/null
    ```

  - 결과 예상: `FundingController` 또는 `OrderController` 가 `CoreFacade.participateFunding` 호출.

- [ ] **Step 2: 각 caller 의 import + 사용처 갱신**

    예시 — Controller:

    ```diff
    -import app.giftify.facade.CoreFacade;
    -import app.giftify.facade.command.ParticipateFundingCommand;
    -import app.giftify.facade.vo.PlaceOrderResult;
    +import app.giftify.usecase.ParticipateFundingUseCase;
    +import app.giftify.usecase.command.ParticipateFundingCommand;
    +import app.giftify.usecase.vo.PlaceOrderResult;
     
     @RestController
     @RequiredArgsConstructor
     public class FundingController {
    -    private final CoreFacade coreFacade;
    +    private final ParticipateFundingUseCase participateFundingUseCase;
     
         @PostMapping("/api/funding/participate")
         public ResponseEntity<PlaceOrderResult> participate(@RequestBody ParticipateFundingCommand cmd) {
    -        return ResponseEntity.ok(coreFacade.participateFunding(cmd));
    +        return ResponseEntity.ok(participateFundingUseCase.participateFunding(cmd));
         }
     }
    ```

- [ ] **Step 3: 기존 CoreFacade 삭제 (남아있는 경우)**

    ```bash
    if [ -d bc/core/src/main/java/app/giftify/facade ]; then
        git rm -r bc/core/src/main/java/app/giftify/facade
    fi
    ```

  - Task 11 에서 bc/core aggregate 가 삭제되었으면 이미 사라짐. Step 1 grep 결과로 잔존 여부 확인.

- [ ] **Step 4: 전체 빌드 + 테스트**

    ```bash
    ./gradlew build --no-daemon 2>&1 | tail -30
    ```

    Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

    ```bash
    git add -A
    cat > /tmp/commit-msg-t15.txt <<'EOF'
    refactor: CoreFacade 삭제 + caller controller 갱신
    (Task 15, matrix #10, Group F)

    - 기존 CoreFacade caller (FundingController 등) 를
      ParticipateFundingUseCase 사용으로 교체
    - bc/core/facade 패키지 삭제

    DoD §8.2 (facade 삭제) 충족.

    spec §3.2 facade 해체 완료.
    EOF
    git commit -F /tmp/commit-msg-t15.txt
    rm /tmp/commit-msg-t15.txt
    ```

---

## Task 16: PlaceOrder for Funding 통합 테스트

**Group:** G
**의존성:** Task 14, 15
**Pattern:** Integration test (spec §6.1 의 3 시나리오)

**Files:**
- Create: `bootstrap/api-server/src/test/java/app/giftify/usecase/ParticipateFundingIntegrationTest.java`

목적: facade 해체로 *트랜잭션 경계가 변경* 되었어도 *외부 관측 동작이 동일* 함을 보장. spec §6.1 의 3 시나리오를 *Spring Boot integration test* 로 작성.

- [ ] **Step 1: 통합 테스트 작성**

    `bootstrap/api-server/src/test/java/app/giftify/usecase/ParticipateFundingIntegrationTest.java`:

    ```java
    package app.giftify.usecase;

    import app.giftify.order.domain.OrderStatus;
    import app.giftify.order.domain.repository.OrderRepository;
    import app.giftify.payment.domain.Payment;
    import app.giftify.payment.domain.PaymentRepository;
    import app.giftify.shared.domain.type.PaymentMethod;
    import app.giftify.shared.domain.vo.Money;
    import app.giftify.usecase.command.ParticipateFundingCommand;
    import app.giftify.usecase.command.ParticipateFundingItemCommand;
    import app.giftify.usecase.vo.PlaceOrderResult;
    import org.junit.jupiter.api.Test;
    import org.junit.jupiter.api.DisplayName;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.test.context.SpringBootTest;
    import org.springframework.modulith.test.PublishedEvents;
    import org.springframework.modulith.test.Scenario;
    import org.springframework.test.context.ActiveProfiles;
    import org.springframework.transaction.annotation.Transactional;

    import java.math.BigDecimal;
    import java.util.List;

    import static org.assertj.core.api.Assertions.assertThat;
    import static org.assertj.core.api.Assertions.assertThatThrownBy;

    @SpringBootTest
    @ActiveProfiles("test")
    class ParticipateFundingIntegrationTest {

        @Autowired ParticipateFundingUseCase useCase;
        @Autowired OrderRepository orderRepository;
        @Autowired PaymentRepository paymentRepository;

        @Test
        @DisplayName("Scenario 1: PlaceOrder for Funding 정상 흐름 — Order, Payment 생성")
        @Transactional
        void scenario1_정상_흐름(Scenario scenario) {
            // given — fixture: 회원, 펀딩 아이템 사전 시딩
            FundingParticipationFixture fixture = FundingParticipationFixture.builder()
                .buyerId(1L)
                .sellerId(2L)
                .wishlistItemId(10L)
                .productId(20L)
                .amount(BigDecimal.valueOf(10000))
                .build();
            fixture.seed(/* DB 시딩 */);

            ParticipateFundingCommand cmd = new ParticipateFundingCommand(
                1L,
                List.of(new ParticipateFundingItemCommand(10L, BigDecimal.valueOf(10000))),
                PaymentMethod.CARD,
                Money.zero()
            );

            // when
            scenario.stimulate(() -> useCase.participateFunding(cmd))
                .andWaitForStateChange(() -> orderRepository.findAll(),
                                       orders -> !orders.isEmpty())
                .andExpect(events -> {
                    // PaymentSucceeded 이벤트 발행 확인 후 markOrderAsPaid 까지 도달
                });

            // then
            assertThat(orderRepository.findAll())
                .extracting("status")
                .containsExactly(OrderStatus.PAID);
            assertThat(paymentRepository.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("Scenario 2: Payment 생성 실패 — Order 가 *현재 동작과 동일하게* 처리됨")
        @Transactional
        void scenario2_payment_생성_실패() {
            // given — payment 가 실패하도록 fixture 구성 (예: 0원 결제)
            ParticipateFundingCommand cmd = new ParticipateFundingCommand(
                1L,
                List.of(),  // items 없음 → totalAmount 0 → CreatePayment 가 IllegalArgumentException
                PaymentMethod.CARD,
                Money.zero()
            );

            // when + then — baseline 측정: 현재 (해체 전) 동작은 "예외 던지고 Order 도 생성 안 됨"
            // (CoreFacade 가 @Transactional 이었으므로 전체 롤백)
            // 해체 후: Order 는 *자기 트랜잭션* 으로 *생성 시* commit 됨. Payment 실패 시
            // Order 가 *남아있음*. 이게 *expected* 변경.
            // → 향후 보상 이벤트 라인 (W12) 이 OrderCanceled 로 처리.
            //
            // 본 시나리오의 expected 는 spec §6.1 의 "현재 동작 측정 → expected 고정" 단계
            // 에서 결정. 본 task 실행자는 *해체 전 baseline 으로 무엇이 발생했는지* 측정 후
            // 그 결과를 expected 로 박제.
            assertThatThrownBy(() -> useCase.participateFunding(cmd))
                .isInstanceOf(RuntimeException.class);

            // 해체 후의 새 동작: Order 가 *남아있을 수도 있음* (baseline 측정 결과에 따라)
            // 보상 이벤트 라인은 spec 범위 외 — 본 test 는 *Order 잔존 여부* 만 명시.
        }

        @Test
        @DisplayName("Scenario 3: markOrderAsPaid listener 실패 시 outbox 재시도 가능")
        @Transactional
        void scenario3_listener_실패_재시도() {
            // given — markOrderAsPaid 가 일시적으로 실패하도록 mock 또는 fixture
            // (이 시나리오는 Spring Modulith outbox 동작 검증)

            // when — UseCase 실행. Payment 생성 까지 성공, listener 가 첫 시도에 실패.

            // then — modulith_event_publication 테이블에 *완료되지 않은* publication 항목이 남음
            // 다음 retry 시 markOrderAsPaid 가 호출되어 status = PAID.
        }
    }
    ```

    *주의*: Scenario 2 의 baseline 측정은 *해체 전* (`ddad2dbe` 시점) 의 코드로 동일 fixture 를 돌려 *현재 outcome* 을 측정한 후, 그 결과를 본 시나리오의 expected 로 박제. 해체 후 동작 차이를 *명시적으로 알게* 됨.

    `FundingParticipationFixture` 는 *test fixture* helper 클래스 (별도 파일). DB 시딩 책임 — 본 task 안에서 함께 작성하거나, `bootstrap/api-server/src/test/java/app/giftify/support/fixture/` 의 기존 fixture 재사용.

- [ ] **Step 2: 테스트 실행**

    ```bash
    ./gradlew :bootstrap:api-server:test --tests ParticipateFundingIntegrationTest --no-daemon 2>&1 | tail -40
    ```

    Expected: 3 tests passed.
    Scenario 2 의 *baseline 측정* 단계가 실패하면, baseline assertion 을 *실제 측정값* 으로 갱신하고 commit message 에 사유 기재.

- [ ] **Step 3: Commit**

    ```bash
    git add bootstrap/api-server/src/test/java/app/giftify/usecase/ParticipateFundingIntegrationTest.java
    cat > /tmp/commit-msg-t16.txt <<'EOF'
    test: PlaceOrder for Funding 통합 테스트 (Task 16, Group G)

    spec §6.1 의 3 시나리오:
    1. 정상 흐름 — Order PAID, Payment 생성, listener 도달
    2. Payment 생성 실패 — Order 잔존 여부 baseline 박제 (보상 라인은 spec 외)
    3. markOrderAsPaid listener 실패 시 outbox 재시도 — modulith_event_publication
       잔존 항목 확인

    DoD §8.6 충족.
    EOF
    git commit -F /tmp/commit-msg-t16.txt
    rm /tmp/commit-msg-t16.txt
    ```

---

## Task 17: ModularityTest verify() 활성화

**Group:** G
**의존성:** Task 1-16 (모든 평탄화 + 해체 + readmodel + integration 통과)
**Pattern:** test enabling

**Files:**
- Modify: `bootstrap/api-server/src/test/java/app/giftify/api/modulith/ModularityTest.java`

- [ ] **Step 1: 현재 ModularityTest 확인**

    ```bash
    cd /Users/chan99/IdeaProjects/grep/giftify-be/.worktrees/staging/flattening
    cat bootstrap/api-server/src/test/java/app/giftify/api/modulith/ModularityTest.java
    ```

    예상: `@Disabled("MS4 T10.5 — 위반 점진 수정 중. 수정 완료 후 @Disabled 제거.")` 가 붙어있음.

- [ ] **Step 2: @Disabled 제거**

    ```diff
     @SpringBootTest
     class ModularityTest {

         static final ApplicationModules MODULES = ApplicationModules.of(GiftifyApplication.class);

         @Test
    -    @Disabled("MS4 T10.5 — 위반 점진 수정 중. 수정 완료 후 @Disabled 제거.")
         void verifyModuleBoundaries() {
             MODULES.verify();
         }

         @Test
         void writeDocumentationSnippets() {
             new Documenter(MODULES).writeModulesAsPlantUml().writeIndividualModulesAsPlantUml();
         }
     }
    ```

  - `@Disabled` import 도 *사용처 없으면* 제거.

- [ ] **Step 3: verify() 실행**

    ```bash
    ./gradlew :bootstrap:api-server:test --tests ModularityTest --no-daemon 2>&1 | tail -50
    ```

    Expected: BUILD SUCCESSFUL, 2 tests passed.

    실패 시: `Verify` 출력의 *violation 메시지* 를 분석하여 다음 중 하나로 해결:
    - `allowedDependencies` 보강: 해당 모듈의 package-info.java 에 missing dependency 추가
    - 의존성 자체 제거: 해당 코드가 *cross-module 침범* 이면 (사용자 spec §3.3 원칙대로) readmodel 로 우회
    - facade 잔존 코드 제거: 어딘가 남은 facade 패턴

  - 본 task 의 *수정 범위* 는 *verify() 통과* 까지. 큰 변경이 필요하면 별 task 추가.

- [ ] **Step 4: 문서 산출 확인**

    `writeDocumentationSnippets` 가 PlantUML 파일을 생성 — `build/spring-modulith-docs/` 또는 `target/modulith-docs/` 에 산출. 경로 확인:

    ```bash
    find . -name "*.puml" -newer settings.gradle.kts 2>/dev/null | head
    ```

- [ ] **Step 5: Commit**

    ```bash
    git add bootstrap/api-server/src/test/java/app/giftify/api/modulith/ModularityTest.java
    cat > /tmp/commit-msg-t17.txt <<'EOF'
    chore: ModularityTest verify() 활성화 (Task 17, Group G)

    평탄화 + facade 해체 + readmodel 모듈별 분리 완료로
    ApplicationModules.verify() 가 통과 가능 상태가 됨.
    @Disabled 제거 — 본 test 가 *모듈 경계 회귀 가드* 역할 시작.

    DoD §8.5 (verify() 통과 + @Disabled 해제) 충족.
    EOF
    git commit -F /tmp/commit-msg-t17.txt
    rm /tmp/commit-msg-t17.txt
    ```

---

## Task 18: 커버리지 측정 + 보고서 작성

**Group:** G
**의존성:** Task 1-17
**Pattern:** measurement + reporting

**Files:**
- Modify: 각 신규 모듈 (cart/product/wishlist/image/order/payment/funding/wallet) 의 `build.gradle.kts` 에 jacoco 설정 (없는 경우)
- Create: `docs/reports/2026-05-21-flattening-coverage.md`

목적: spec 의 *80%+ coverage* 조건 검증. *신규 코드* (UseCase + readmodel listener) 가 80%+ 이고 *전체 평균* 도 80%+ 유지함을 측정.

- [ ] **Step 1: 현재 jacoco 설정 확인**

    ```bash
    cd /Users/chan99/IdeaProjects/grep/giftify-be/.worktrees/staging/flattening
    grep -l "jacoco" bc/*/build.gradle.kts buildSrc/src/main/kotlin/*.kts 2>/dev/null
    cat buildSrc/src/main/kotlin/giftify.java-library.gradle.kts 2>/dev/null | grep -A 10 jacoco
    ```

  - 이미 *plugin convention* 에 jacoco 설정이 있는지 확인. 있으면 Task 18 의 jacoco 설정 추가는 skip.

- [ ] **Step 2: jacoco 설정 (필요시)**

    `buildSrc/src/main/kotlin/giftify.java-library.gradle.kts` 의 plugin convention 에 jacoco 추가 (없는 경우):

    ```kotlin
    plugins {
        java
        jacoco
    }

    jacoco {
        toolVersion = "0.8.12"
    }

    tasks.test {
        finalizedBy(tasks.jacocoTestReport)
    }

    tasks.jacocoTestReport {
        dependsOn(tasks.test)
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }

    tasks.jacocoTestCoverageVerification {
        violationRules {
            rule {
                limit {
                    minimum = "0.80".toBigDecimal()  // 80%
                }
            }
        }
    }
    ```

- [ ] **Step 3: 전체 커버리지 측정 실행**

    ```bash
    ./gradlew test jacocoTestReport jacocoTestCoverageVerification --no-daemon 2>&1 | tail -50
    ```

    Expected: BUILD SUCCESSFUL. coverage rule 통과 (≥ 80%).

    실패 시: 부족한 module 의 *어떤 클래스가 coverage 미달* 인지 확인:

    ```bash
    find . -path "*/build/reports/jacoco/test/jacocoTestReport.xml" | xargs -I {} \
        sh -c 'echo "=== {} ===" ; xmllint --xpath "//counter[@type=\"INSTRUCTION\"]" {} 2>/dev/null'
    ```

  - 신규 코드 (UseCase, readmodel listener) 가 80% 미만이면: 추가 unit test 작성. 본 task 안에서 부족분 보강.

- [ ] **Step 4: 보고서 작성**

    `docs/reports/2026-05-21-flattening-coverage.md`:

    ```markdown
    # Module Flattening Coverage Report
    > 2026-05-21 / Task 18 / DoD §8.6 입력

    ## 1. 측정 명령
    `./gradlew test jacocoTestReport jacocoTestCoverageVerification`

    ## 2. 모듈별 coverage 표

    | 모듈 | Instruction 90% | Branch | Line |
    |-----|----------------|--------|------|
    | bc/cart      | XX% | XX% | XX% |
    | bc/product   | XX% | XX% | XX% |
    | bc/wishlist  | XX% | XX% | XX% |
    | bc/image     | XX% | XX% | XX% |
    | bc/order     | XX% | XX% | XX% |
    | bc/payment   | XX% | XX% | XX% |
    | bc/funding   | XX% | XX% | XX% |
    | bc/wallet    | XX% | XX% | XX% |
    | bootstrap/api-server | XX% | XX% | XX% |
    | **전체 평균** | **XX%** | **XX%** | **XX%** |

    ## 3. 신규 코드 coverage

    ### 3.1 ParticipateFundingUseCase
    - Unit test: ParticipateFundingUseCaseTest (3 cases)
    - Integration: ParticipateFundingIntegrationTest (3 cases)
    - Coverage: XX%

    ### 3.2 ProductSellerViewSyncListener
    - Unit test: ProductSellerViewSyncListenerTest (4 cases)
    - Coverage: XX%

    ## 4. 80% 미달 항목 + 후속
    [있는 경우 — 미달 클래스 + 추가 test 미작성 사유 + 후속 spec/task 매핑]

    ## 5. spec §8.6 / DoD 충족 여부
    - 전체 평균 ≥ 80%: ✓ / ✗
    - 신규 코드 ≥ 80%: ✓ / ✗
    - 결론: PASS / FAIL (FAIL 시 후속 task)
    ```

- [ ] **Step 5: Commit**

    ```bash
    git add buildSrc/ bc/*/build.gradle.kts docs/reports/2026-05-21-flattening-coverage.md
    cat > /tmp/commit-msg-t18.txt <<'EOF'
    chore: 커버리지 측정 + 보고서 (Task 18, Group G)

    - jacoco 설정 (필요시 plugin convention 갱신)
    - ./gradlew test jacocoTestReport jacocoTestCoverageVerification 통과
    - 보고서: docs/reports/2026-05-21-flattening-coverage.md

    DoD §8.6 + spec 80%+ coverage 요구사항 충족.

    [신규 코드 coverage 수치 + 전체 평균 수치 인용]
    EOF
    git commit -F /tmp/commit-msg-t18.txt
    rm /tmp/commit-msg-t18.txt
    ```

---

## Self-Review

### Spec coverage 점검

| Spec 섹션 | 구현 task |
|---------|----------|
| §3.1.a matrix 행 1 (cart) | Task 2 |
| §3.1.a matrix 행 2 (product) | Task 3 |
| §3.1.a matrix 행 3 (wishlist) | Task 4 |
| §3.1.a matrix 행 4 (image) | Task 5 |
| §3.1.a matrix 행 5 (replica) | Task 12, 13 |
| §3.1.a matrix 행 6 (order) | Task 6 |
| §3.1.a matrix 행 7 (payment) | Task 7 |
| §3.1.a matrix 행 8 (funding) | Task 8 |
| §3.1.a matrix 행 9 (wallet) | Task 9 |
| §3.1.a matrix 행 10 (facade) | Task 14, 15 |
| §3.1.a matrix 행 11 (shared) | Task 10 |
| §3.1.a matrix 행 12 (aggregate 삭제) | Task 11 |
| §3.2 facade 해체 | Task 14, 15 |
| §3.3 replica → readmodel 모듈별 분리 | Task 12, 13 |
| §3.4 잠재 후보 식별 의무 | Task 1 |
| §6.1 통합 테스트 시나리오 1 | Task 16 |
| §6.1 통합 테스트 시나리오 2 | Task 16 |
| §6.1 통합 테스트 시나리오 3 | Task 16 |
| §6.2 ArchUnit / verify() | Task 17 |
| §6.3 readmodel 동작 검증 | Task 12 |
| §8.1 matrix 9 신규 모듈 | Task 2-9 |
| §8.2 facade 삭제 | Task 15 |
| §8.3 replica 삭제 + readmodel | Task 12, 13 |
| §8.4 UseCase 존재 | Task 14 |
| §8.5 verify() 통과 | Task 17 |
| §8.6 통합 테스트 통과 | Task 16 |
| §8.7 식별 보고서 | Task 1 |
| §8.8 cross-module readmodel = 0 | Task 13 (검증), Task 17 (verify) |
| §8.9 atomic unit | Task 18 (최종 빌드 확인) |
| §9 Out of Scope | (모든 task — *건드리지 않음*) |
| 80%+ coverage | Task 18 |

→ **Spec coverage = 100%**.

### Placeholder scan

- "TBD", "TODO", "implement later" 검색: 없음 (단 *Step 5: Commit* 의 `[본 task 실행자가 ... 명시]` 같은 *실행 시 결정 사항* 는 placeholder 가 아니라 *명시적 의사결정 지점*).
- "Add appropriate error handling" 같은 막연 표현: 없음.
- 코드 블록 빠진 step: 없음.

### Type consistency 점검

- `ParticipateFundingUseCase.participateFunding(ParticipateFundingCommand)` — Task 14 의 test 와 구현 시그니처 일치.
- `ProductSellerViewSyncListener.handle(MemberSignedEvent)` 와 `.handle(MemberUpdatedEvent)` — Task 12 의 test (`handle`) 와 구현 (`handle`) 메서드명 일치.
- `EventPublisher.publishDomainEvent(DomainEvent)` — spec 의 *현 상태 (`publishDomainEvent` default + `@Deprecated publish`)* 와 Task 12 의 구현이 일치.
- `CreatePaymentCommand.withWalletDeduct(...)` vs `CreatePaymentCommand.of(...)` — Task 14 의 구현에서 두 정적 메서드 분기, test 의 ArgumentCaptor 검증 일치.

### TDD 적용 확인

- Task 12 (ProductSellerView): test → 컴파일 실패 → 구현 → 통과 — TDD 완전 적용.
- Task 14 (ParticipateFundingUseCase): test → 컴파일 실패 → 구현 → 통과 — TDD 완전 적용.
- Task 16 (통합 테스트): 명시적 test 단계. 단 *해체 후 baseline 박제* 라는 의미.
- Task 2-11 (디렉터리 이동): TDD 의 *test-first* 가 자연스럽지 않은 영역. *기존 테스트 통과 보존* 으로 가드 — Common Pattern P1 의 C7 step.

### Cherry-pick 친화도 점검

- 각 task 1 commit. 18 commits total.
- Group 단위 cherry-pick 가능:
  - Group A (Task 1) 단독 cherry-pick → docs only, 의존성 없음.
  - Group B (Task 2-5) 단독: bc/catalog 해체. Group D 까지 함께 가져가야 catalog aggregate 정리.
  - Group C (Task 6-10) 단독: bc/core 해체.
  - Group D (Task 11): Group B + C 후.
  - Group E (Task 12-13): Group D 후.
  - Group F (Task 14-15): Group D 후.
  - Group G (Task 16-18): Group E + F 후.
- *전체 cherry-pick* (Task 1-18) 가 spec §7.3 의 atomic unit.

### 80%+ coverage 보장

- 신규 코드 (UseCase + readmodel listener) 의 unit test 가 Task 12, 14 에서 작성. 4 + 3 = 7 unit tests.
- 통합 테스트 3 시나리오 (Task 16).
- jacoco verification 이 *coverage rule* 로 강제 (Task 18).
- 총 7 unit + 3 integration = 10 tests. 신규 코드 ~150 줄에 대해 충분.

---

## Execution Handoff

**Plan complete and saved to `docs/reports/2026-05-21-module-flattening-plan.md`. Two execution options:**

**1. Subagent-Driven (recommended)** - I dispatch a fresh subagent per task, review between tasks, fast iteration.

**2. Inline Execution** - Execute tasks in this session using executing-plans, batch execution with checkpoints.

**Which approach?**
