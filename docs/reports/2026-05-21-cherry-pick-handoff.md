# Worktree → 본 레포 Cherry-pick 핸드오프

> 작성: 2026-05-21
> 워크트리: `.worktrees/staging/post-deploy-cleanup`
> base: `origin/develop`
> 총 commit: 34

## 0. 목적
본 워크트리는 Plan 0/1/2/3 의 구현 산출물을 *원자적 commit 단위* 로 누적한 격리 작업 공간.
사용자는 각 commit 을 본 레포 `develop` 또는 feature 브랜치로 cherry-pick 하여 PR 생성.

## 1. Commit 분류

### Plan 0 — Post-deploy 잔여 정리 (Sprint 1-3 일부)
| ID | Commit | 분류 | PR 분리 권장 |
|----|--------|------|-------------|
| P0-1 | `73f67044` docs: T10.24 평탄화 사전분석 | 문서 | 평탄화 PR과 함께 |
| P0-2 | `47430aba` refactor: bc/notification @Slf4j 제거 (5 파일) | Lombok-P1 | 단독 |
| P0-3 | `bb664c26` refactor: bc/member @Slf4j 제거 (14 파일) | Lombok-P1 | 단독 |
| P0-4 | `c249ac21` refactor: bc/settlement @Slf4j 제거 (9 파일) | Lombok-P1 | 단독 |
| P0-5 | `f65784c9` refactor: bc/catalog @Slf4j 제거 (17 파일) | Lombok-P1 | 단독 |
| P0-6 | `7b52caaf` refactor: bc/core @Slf4j 제거 (42 파일) | Lombok-P1 | 단독 |
| P0-7 | `5347e943` docs: InternalApiOnly Case A 확정 | 문서 | InternalApi PR과 함께 |
| P0-8 | `f3d62081` feat: InternalApiAuthFilter 도입 | E | 단독 |
| P0-9 | `1a68a29f` refactor: InternalApiOnly marker 전환 | E | 단독 |
| P0-10 | `bf7fbfa5` refactor: Notification 도메인 Lombok 제거 (P0) | P0 | 단독 |

### Plan 1 (MS2 W4-W6) — 캐시 + 이미지 + 로깅
| ID | Commit | 분류 | PR 분리 권장 |
|----|--------|------|-------------|
| P1-1 | `392808d8` feat: Spring Cache + RedisCacheManager | 캐시 인프라 | 단독 |
| P1-2 | `41f2cb50` docs: 캐시 전략 명세 | 문서 | 단독 (또는 P1-1 과 묶음) |
| P1-3 | `698c53ad` feat: ProductService 단건 조회 캐시 | 캐시 적용 | 단독 |
| P1-4 | `50ce75c2` refactor: jwks 캐시 제거 + Cycle 1 측정 템플릿 | 정정 | 단독 |
| P1-5 | `c4b0f7c6` docs: 이미지 저장소 결정 (MinIO + AWS SDK v2) | 문서 | W5 묶음 |
| P1-6 | `5c1b49fb` feat: S3Config 도입 | W5 | 단독 |
| P1-7 | `4c4d82fa` feat: 이미지 도메인 포트 + Service | W5 | 단독 |
| P1-8 | `722dab85` feat: AwsS3ImageStorageAdapter | W5 | 단독 |
| P1-9 | `785d7e56` feat: ImageController | W5 | 단독 |
| P1-10 | `1a04c20d` feat: application.yml giftify.storage.s3 | W5 config | 단독 |
| P1-11 | `3f99806d` chore: logstash-logback-encoder 9.0 dep | W6 | W6 묶음 권장 |
| P1-12 | `42d95521` feat: prod JSON 로깅 + slow query 임계값 | W6 | 묶음 |
| P1-13 | `94b45714` docs: W6 측정 보고서 템플릿 | 문서 | 묶음 |

**권장 PR 묶음 (Plan 1)**:
- PR-A: "Spring Cache 도입 + 상품 단건 캐시 적용" (P1-1 → P1-4)
- PR-B: "이미지 객체 저장소 — Presigned URL 패턴" (P1-5 → P1-10)
- PR-C: "JSON 로깅 + slow query 임계값" (P1-11 → P1-13)

### Plan 2 (MS3 W7-W9) — HA + Scale-out
| ID | Commit | 분류 | PR 분리 권장 |
|----|--------|------|-------------|
| P2-1 | `6cffc21e` docs: PostgreSQL HA 의사결정 | 문서 | P2-2 와 묶음 |
| P2-2 | `3e20cb54` feat: bitnami/postgresql HA values | infra | 묶음 |
| P2-3 | `3981d553` feat: AbstractRoutingDataSource | code | 단독 |
| P2-4 | `080b1cff` feat: HikariCP 다중 풀 | code | P2-3 과 묶거나 단독 |
| P2-5 | `c10e8a9f` refactor: readOnly 보강 (CartService + MemberService) | code | 단독 |
| P2-6 | `4359b76d` feat: api-server replicas 2 + RollingUpdate | infra | 단독 |
| P2-7 | `2badaa60` feat: application-staging 프로파일 | config | 단독 |
| P2-8 | `015b4ccb` docs: MS3 측정 보고서 템플릿 4종 | 문서 | 단독 |

**권장 PR 묶음 (Plan 2)**:
- PR-D: "PostgreSQL HA 도입 + DataSource Routing" (P2-1 → P2-5)
- PR-E: "api-server scale-out + staging 프로파일" (P2-6 → P2-7)
- PR-F: "MS3 측정 보고서 자리" (P2-8)

### Plan 3 (MS4 W10-W12) — Architecture
| ID | Commit | 분류 | PR 분리 권장 |
|----|--------|------|-------------|
| P3-1 | `5ae636b6` feat: Spring Modulith verify() 테스트 | code+test | 단독 |
| P3-2 | `47e6384d` docs: MS4 의사결정 문서 5종 | 문서 | 단독 |
| P3-3 | `fb5ba002` refactor: EventPublisher 타입 안전 점진 마이그레이션 | refactor | 단독 |

**권장 PR 묶음 (Plan 3)**:
- PR-G: "Spring Modulith 도입 + 의사결정 문서" (P3-1 → P3-3)

## 2. 본 워크트리에서 **제외** 된 작업 (별도 PR 필요)

다음은 *원자적 cherry-pick 부적합* 으로 분리:

### 큰 리팩터링 (사용자 본 레포에서 IDE 기반 진행 권장)
| Task | 이유 |
|------|------|
| MS4 T10.5-T10.29 평탄화 (`bc/*` + `bootstrap/` → `api-server/`) | 전체 import 경로 변경. Plan 0 T10.24 사전분석(`73f67044`) 입력 사용 |
| MS4 T10.16 EventPublisher 강타입 적용 (모든 호출처 마이그레이션) | PaymentEvent 등 integration event 가 DomainEvent 구현하도록 광역 수정 |
| MS4 T10.17-T10.23 도메인 리팩터링 5건 (OrderSnapshot/Payment VO/walletDeducted/BaseAggregate/SecurityConfig) | 각 5건 모두 도메인 변경 — TDD 단위로 본 레포 진행 |
| MS4 T11.6 IdempotentConsumer 통합 | Redis idempotency key 패턴 도입 — 기존 패턴과 통합 작업 |
| MS4 T11.8 외부화 통합 테스트 (testcontainers Redpanda) | 의존성 + testcontainer 구동 |
| MS4 T12.3 PG cancel 호출 + T12.6 Saga 구현 + T12.7 CorrelationId + T12.8 PaymentCancelEventListener | Saga PoC — 별도 큰 PR |
| MS4 T12.11 Spring Retry → Framework 7 Resilience 전환 | 광역 변경 |
| Plan 0 P0 잔여 Aggregate Lombok 제거 (Product/Order/Snapshot 등) | bc 별 분할 진행 |
| Plan 0 Task 4.3 SecurityConfig 통합 | 본 레포에서 직접 진행 권장 |

### 인프라 적용 (사용자 클러스터 작업)
| 항목 | 이유 |
|------|------|
| Helm install bitnami/postgresql | `kubectl` + `helm` 명령 — 클러스터 접근 |
| Postgres data migration (pg_dump/restore) | Live data |
| 부하 테스트 실행 (k6 stress/soak/failover) | k6-runner VM |
| Auth0 dashboard staging application 생성 | 외부 서비스 |
| SOPS 시크릿 추가 | age key + 클러스터 sync |
| Redpanda topic 생성 (`rpk topic create`) | 클러스터 명령 |

## 3. 측정 결과 채우기 자리

다음 docs/reports 템플릿은 staging 부하 사이클 후 측정값을 채워야 합니다:
- `docs/reports/2026-05-20-cycle-1-cache-impact.md` (P1-4 동봉)
- `docs/reports/2026-05-20-w6-query-log-baseline.md` (P1-13 동봉)
- `docs/reports/2026-05-21-failover-test-template.md` (P2-8 동봉)
- `docs/reports/2026-05-21-routing-verification-template.md` (P2-8 동봉)
- `docs/reports/2026-05-21-stateless-verification-template.md` (P2-8 동봉)
- `docs/reports/2026-05-21-cycle-3-load-test-template.md` (P2-8 동봉)

## 4. 빌드/테스트 검증 현황

```
워크트리 종합:
- ./gradlew compileJava ............................ SUCCESS
- ./gradlew :support:common:test ................... SUCCESS (CacheConfig)
- ./gradlew :support:common:test (S3Config) ........ SUCCESS
- ./gradlew :bc:catalog:test ....................... SUCCESS (Cache slice + Image)
- ./gradlew :bootstrap:api-server:test ............. SUCCESS (RoutingDataSource + Modulith docs)
```

ModularityTest.verifyModuleBoundaries 는 `@Disabled` (위반 점진 수정 후 enable).

## 5. Cherry-pick 권장 순서

추천 적용 순서 (의존성 기준):

1. **Plan 0 잔여** (P0-1 → P0-10) — Lombok 제거 + InternalApi 정리
2. **Plan 1 캐시** (P1-1 → P1-4) — 다른 작업에 영향 없음, 즉시 staging 측정 가능
3. **Plan 1 이미지** (P1-5 → P1-10) — Plan 1 캐시와 독립
4. **Plan 1 로깅** (P1-11 → P1-13) — Plan 2/3 의 측정 사이클 입력
5. **Plan 2 HA + Routing** (P2-1 → P2-5) — 인프라 적용 후 검증
6. **Plan 2 Scale-out** (P2-6 → P2-7)
7. **Plan 2 보고서** (P2-8)
8. **Plan 3 Modulith + 문서** (P3-1 → P3-3) — 다른 작업과 무관, 마지막

## 6. 후속 PR 백로그
워크트리에서 제외된 작업은 `ROADMAP-backend.md` 의 해당 milestone 태스크 ID 로 추적.
큰 작업 (평탄화, Saga 구현, Resilience 전환) 은 별도 spec doc + plan 작성 권장.

---
**상태**: 워크트리 작업 완료 — 34 commits 누적. 사용자 cherry-pick 진행 가능.
