# 부하테스트 결과 보고서 참고용 템플릿

## 테스트 환경

| 항목 | 스펙 |
|------|------|
| App VM | GCP e2-standard-2 (2vCPU/8GB) |
| k6 VM | GCP e2-medium (2vCPU/4GB) |
| DB | PostgreSQL (StatefulSet, PVC 5Gi) |
| API Server | replica 1, k3s cluster |
| Rate Limit | 해제 (loadtest IngressRoute) |
| 인증 방식 | Mock Auth (DynamicMockAuthFilter) |
| 테스트 도구 | k6 v0.x |

## 테스트 시나리오 커버리지

k6 부하테스트 시나리오와 GFT(Giftify Feature Test) 사용자 여정 매핑:

| 시나리오 | 커버하는 GFT | 설명 |
|---------|-------------|------|
| **funding-scenario** | GFT-2, GFT-3, GFT-4, GFT-7 | 펀딩 참여 주요 흐름 (검색 → 장바구니 → 펀딩 목록 → 주문) |
| **friend-scenario** | GFT-1, GFT-6 | 친구 관리 및 친구 위시리스트 조회 |
| **seller-scenario** | GFT-5, GFT-13 | 판매자 상품 등록 및 정산 관리 |
| **buyer-supplement-scenario** | GFT-8, GFT-11, GFT-12 | 위시리스트 관리 및 주문 내역 조회 |

### GFT 시나리오 전체 목록

| GFT ID | 사용자 여정 | 시나리오 |
|--------|------------|---------|
| GFT-1 | 펀딩 선물 결정 (친구 목록 → 친구 위시리스트 → 상품 상세) | friend-scenario |
| GFT-2 | 상품 검색 및 조회 | funding-scenario |
| GFT-3 | 위시리스트 조회 | funding-scenario |
| GFT-4 | 장바구니 담기 | funding-scenario |
| GFT-5 | 상품 판매 (판매자 상품 등록 → 내 상품 목록) | seller-scenario |
| GFT-6 | 친구 추가 (친구 요청 → 수락) | friend-scenario |
| GFT-7 | 펀딩 참여 (펀딩 목록 → 주문 생성) | funding-scenario |
| GFT-8 | 위시리스트 상품 등록 | buyer-supplement-scenario |
| GFT-11 | 주문 목록 조회 | buyer-supplement-scenario |
| GFT-12 | 주문 상세 조회 | buyer-supplement-scenario |
| GFT-13 | 정산 목록 조회 (판매자) | seller-scenario |

**미커버 시나리오**: GFT-9 (결제), GFT-10 (환불), GFT-14 (알림) - 외부 시스템 의존성으로 제외

## 시나리오별 결과

### Funding Scenario (펀딩 참여 주요 흐름)

**테스트 구성**:
- VU: {TBD}
- Duration: {TBD}
- 실행 모드: {TBD} (읽기 전용 / 전체 여정)

**성능 지표**:

| API | p50 | p95 | p99 | SLO | 통과 여부 |
|-----|-----|-----|-----|-----|-----------|
| 상품 검색 | {TBD}ms | {TBD}ms | {TBD}ms | 200ms | {TBD} |
| 상품 상세 | {TBD}ms | {TBD}ms | {TBD}ms | 200ms | {TBD} |
| 위시리스트 조회 | {TBD}ms | {TBD}ms | {TBD}ms | 200ms | {TBD} |
| 장바구니 추가 | {TBD}ms | {TBD}ms | {TBD}ms | 500ms | {TBD} |
| 펀딩 목록 조회 | {TBD}ms | {TBD}ms | {TBD}ms | 200ms | {TBD} |
| 주문 생성 | {TBD}ms | {TBD}ms | {TBD}ms | 1000ms | {TBD} |

**RPS**: {TBD}

**Error Rate**: {TBD}%

**주요 발견사항**:
- {TBD}

---

### Friend Scenario (친구 관리 및 위시리스트)

**테스트 구성**:
- VU: {TBD}
- Duration: {TBD}
- Seed data: Givers(1001-1050) ↔ Receivers(1051-1060) = 500 ACCEPTED friendships

**성능 지표**:

| API | p50 | p95 | p99 | SLO | 통과 여부 |
|-----|-----|-----|-----|-----|-----------|
| 친구 목록 조회 | {TBD}ms | {TBD}ms | {TBD}ms | 200ms | {TBD} |
| 친구 위시리스트 조회 | {TBD}ms | {TBD}ms | {TBD}ms | 200ms | {TBD} |
| 상품 상세 조회 | {TBD}ms | {TBD}ms | {TBD}ms | 200ms | {TBD} |
| 친구 요청 (10% 확률) | {TBD}ms | {TBD}ms | {TBD}ms | 500ms | {TBD} |
| 친구 수락 (10% 확률) | {TBD}ms | {TBD}ms | {TBD}ms | 500ms | {TBD} |

**RPS**: {TBD}

**Error Rate**: {TBD}%

**주요 발견사항**:
- {TBD}

---

### Seller Scenario (판매자 상품 등록 및 정산)

**테스트 구성**:
- VU: {TBD}
- Duration: {TBD}
- Seller accounts: 1101-1110 (10명 라운드로빈)
- 상품 등록: 50% 확률 제한

**성능 지표**:

| API | p50 | p95 | p99 | SLO | 통과 여부 |
|-----|-----|-----|-----|-----|-----------|
| 상품 등록 (50% 확률) | {TBD}ms | {TBD}ms | {TBD}ms | 500ms | {TBD} |
| 내 상품 목록 조회 | {TBD}ms | {TBD}ms | {TBD}ms | 200ms | {TBD} |
| 정산 목록 조회 | {TBD}ms | {TBD}ms | {TBD}ms | 200ms | {TBD} |

**RPS**: {TBD}

**Error Rate**: {TBD}%

**주요 발견사항**:
- {TBD}

---

### Buyer Supplement Scenario (위시리스트 관리 및 주문 내역)

**테스트 구성**:
- VU: {TBD}
- Duration: {TBD}
- Buyer accounts: 1001-1060
- 위시리스트 등록: 20% 확률 제한

**성능 지표**:

| API | p50 | p95 | p99 | SLO | 통과 여부 |
|-----|-----|-----|-----|-----|-----------|
| 상품 검색 | {TBD}ms | {TBD}ms | {TBD}ms | 200ms | {TBD} |
| 상품 상세 조회 | {TBD}ms | {TBD}ms | {TBD}ms | 200ms | {TBD} |
| 위시리스트 중복 확인 | {TBD}ms | {TBD}ms | {TBD}ms | 200ms | {TBD} |
| 위시리스트 등록 (20% 확률) | {TBD}ms | {TBD}ms | {TBD}ms | 500ms | {TBD} |
| 주문 목록 조회 | {TBD}ms | {TBD}ms | {TBD}ms | 200ms | {TBD} |
| 주문 상세 조회 | {TBD}ms | {TBD}ms | {TBD}ms | 200ms | {TBD} |

**RPS**: {TBD}

**Error Rate**: {TBD}%

**주요 발견사항**:
- {TBD}

---

## 부하 테스트 결과

### Stress Test (점진적 부하 증가 테스트)

**목적**: 시스템의 최대 처리 용량 및 한계점 파악

**테스트 구성**:
- Duration: {TBD}
- VU 증가 패턴: {TBD}
- Target VU: {TBD}

**결과**:

| 지표 | 값 |
|------|-----|
| Peak RPS | {TBD} |
| Peak VU | {TBD} |
| p95 응답시간 | {TBD}ms |
| Error Rate | {TBD}% |
| 한계 도달 시점 | {TBD} VU |

**병목 원인**:
- {TBD}

**Grafana 스크린샷**:
- {TBD}

---

### Soak Test (장시간 안정성 테스트)

**목적**: 메모리 누수, 리소스 고갈 등 장시간 운영 시 발생 가능한 문제 검증

**테스트 구성**:
- Duration: {TBD}
- Constant VU: {TBD}
- Total Requests: {TBD}

**결과**:

| 지표 | 초기 | 종료 시점 | 변화율 |
|------|------|-----------|--------|
| p95 응답시간 | {TBD}ms | {TBD}ms | {TBD}% |
| RPS | {TBD} | {TBD} | {TBD}% |
| JVM Heap 사용량 | {TBD}MB | {TBD}MB | {TBD}% |
| DB Connection Pool | {TBD} | {TBD} | {TBD}% |
| Error Rate | {TBD}% | {TBD}% | {TBD}% |

**메모리 누수 여부**: {TBD}

**주요 발견사항**:
- {TBD}

**Grafana 스크린샷**:
- {TBD}

---

### Spike Test (급격한 트래픽 증가 테스트)

**목적**: 갑작스런 트래픽 급증 상황에서의 시스템 회복력 검증

**테스트 구성**:
- Baseline VU: {TBD}
- Spike VU: {TBD}
- Spike Duration: {TBD}

**결과**:

| 구간 | RPS | p95 응답시간 | Error Rate |
|------|-----|-------------|-----------|
| Baseline | {TBD} | {TBD}ms | {TBD}% |
| Spike 시점 | {TBD} | {TBD}ms | {TBD}% |
| 회복 후 | {TBD} | {TBD}ms | {TBD}% |

**회복 시간**: {TBD}초

**주요 발견사항**:
- {TBD}

---

### Breakpoint Test (한계점 탐색 테스트)

**목적**: 시스템이 완전히 중단되는 지점 파악

**테스트 구성**:
- VU 증가 방식: {TBD}
- 중단 조건: {TBD}

**결과**:

| 지표 | 값 |
|------|-----|
| Breakpoint VU | {TBD} |
| Breakpoint RPS | {TBD} |
| 중단 시점 p95 | {TBD}ms |
| Error Rate | {TBD}% |
| 주요 에러 유형 | {TBD} |

**시스템 중단 원인**:
- {TBD}

---

## 동시성 테스트 결과

### Concurrency Test (동시성 제어 검증)

**목적**: 예치금 잔액 차감, 펀딩 목표 금액 업데이트 등 동시성 이슈 검증

**테스트 구성**:
- 시나리오: {TBD}
- VU: {TBD}
- Target Resource: {TBD}
- 예상 최종 상태: {TBD}

**결과**:

| 항목 | 예상값 | 실제값 | 일치 여부 |
|------|--------|--------|-----------|
| 최종 잔액/금액 | {TBD} | {TBD} | {TBD} |
| 생성된 트랜잭션 수 | {TBD} | {TBD} | {TBD} |
| Error Rate | {TBD}% | {TBD}% | {TBD} |

**verify.sh 검증 결과**:
```
{TBD}
```

**동시성 제어 방식**:
- {TBD} (e.g. Pessimistic Lock, Optimistic Lock, DB Constraint)

**주요 발견사항**:
- {TBD}

---

## Grafana 모니터링 결과

### JVM 메트릭

**Heap Memory**:
- 최대 사용량: {TBD}MB / {TBD}MB
- GC 빈도: {TBD}회/분
- GC 평균 시간: {TBD}ms

![JVM Heap Usage]({TBD})

**Thread Pool**:
- Active Threads (Peak): {TBD}
- Queued Tasks (Peak): {TBD}

---

### DB 메트릭

**PostgreSQL Connections**:
- Active Connections (Peak): {TBD}
- Idle Connections: {TBD}
- HikariCP Pool Size: {TBD}

![PostgreSQL Connections]({TBD})

**Query Performance**:
- Slowest Query: {TBD}ms
- Queries/sec (Peak): {TBD}

---

### 인프라 메트릭

**CPU Usage**:
- App VM: {TBD}%
- DB VM: {TBD}%

**Memory Usage**:
- App VM: {TBD}GB / 8GB
- DB VM: {TBD}GB / {TBD}GB

![CPU and Memory Usage]({TBD})

**Network I/O**:
- Ingress: {TBD}MB/s
- Egress: {TBD}MB/s

---

## 시스템 한계 및 병목 분석

### 확인된 한계점

| 지표 | 값 | 병목 원인 | 우선순위 |
|------|-----|----------|---------|
| Max Sustainable VU | {TBD} | {TBD} | {TBD} |
| Max RPS | {TBD} | {TBD} | {TBD} |
| p95 SLO 한계 VU | {TBD} | {TBD} | {TBD} |
| DB Connection Pool 고갈 | {TBD} VU | HikariCP maximumPoolSize=10 | {TBD} |
| JVM Heap 부족 | {TBD} VU | {TBD} | {TBD} |

### Iterative Bottleneck Elimination

현재 병목 제거 시 예상 개선치 (Little's Law: `L = λ × W`):

1. **1차 병목**: {TBD}
   - 현재 영향: {TBD}
   - 제거 시 예상 개선: {TBD}
   - 해결 방안: {TBD}

2. **2차 병목**: {TBD}
   - 현재 영향: {TBD}
   - 제거 시 예상 개선: {TBD}
   - 해결 방안: {TBD}

3. **3차 병목**: {TBD}
   - 현재 영향: {TBD}
   - 제거 시 예상 개선: {TBD}
   - 해결 방안: {TBD}

---

## 개선 로드맵

### Phase 1: 즉시 적용 가능 (단기)

| 항목 | 예상 효과 | 비용 | 우선순위 |
|------|-----------|------|---------|
| {TBD} | {TBD} | {TBD} | High |
| {TBD} | {TBD} | {TBD} | Medium |

### Phase 2: 아키텍처 개선 (중기)

| 항목 | 예상 효과 | 비용 | 우선순위 |
|------|-----------|------|---------|
| {TBD} | {TBD} | {TBD} | Medium |
| {TBD} | {TBD} | {TBD} | Low |

### Phase 3: 스케일 아웃 (장기)

| 항목 | 예상 효과 | 비용 | 우선순위 |
|------|-----------|------|---------|
| {TBD} | {TBD} | {TBD} | Low |

---

## 부록: 테스트 실행 가이드

### GCP k6 VM 접속 및 테스트 실행

**VM 접속**:
```bash
# SSH 접속
gcloud compute ssh k6-loadtest-vm --zone=asia-northeast3-a

# k6 디렉토리 이동
cd /home/chan99k_dev/k6
```

**시나리오별 실행 명령어**:

```bash
# Funding Scenario (읽기 전용)
k6 run --env BASE_URL=http://giftify-api-loadtest.chan99k.dev \
       --env MOCK_AUTH=true \
       scripts/funding-scenario.js

# Funding Scenario (주문 생성 포함)
k6 run --env BASE_URL=http://giftify-api-loadtest.chan99k.dev \
       --env MOCK_AUTH=true \
       --env TEST_RECEIVER_ID=1051 \
       scripts/funding-scenario.js

# Friend Scenario
k6 run --env BASE_URL=http://giftify-api-loadtest.chan99k.dev \
       --env MOCK_AUTH=true \
       scripts/friend-scenario.js

# Seller Scenario
k6 run --env BASE_URL=http://giftify-api-loadtest.chan99k.dev \
       --env MOCK_AUTH=true \
       scripts/seller-scenario.js

# Buyer Supplement Scenario
k6 run --env BASE_URL=http://giftify-api-loadtest.chan99k.dev \
       --env MOCK_AUTH=true \
       scripts/buyer-supplement-scenario.js
```

**부하 테스트 실행**:

```bash
# Stress Test
k6 run --env BASE_URL=http://giftify-api-loadtest.chan99k.dev \
       --env MOCK_AUTH=true \
       scripts/stress-test.js

# Soak Test
k6 run --env BASE_URL=http://giftify-api-loadtest.chan99k.dev \
       --env MOCK_AUTH=true \
       scripts/soak-test.js

# Concurrency Test
k6 run --env BASE_URL=http://giftify-api-loadtest.chan99k.dev \
       --env MOCK_AUTH=true \
       scripts/concurrency-test.js
```

**결과 검증**:

```bash
# DB 정합성 검증 (Concurrency Test 후)
./verify.sh
```

### Grafana 모니터링

- URL: {TBD}
- Dashboard: {TBD}
- 주요 패널:
  - JVM Metrics (Heap, Thread, GC)
  - PostgreSQL Metrics (Connections, Queries)
  - Infrastructure (CPU, Memory, Network)
  - k6 Results (RPS, Response Time, Error Rate)

---

## 참고 자료

- k6 부하테스트 스크립트: `/infra/monitoring/k6/scripts/`
- k6 공통 모듈: `/infra/monitoring/k6/modules/`
- Seed data: `/bootstrap/api-server/src/main/resources/db/migration/member/R__seed.sql`
- 관련 PR: #{TBD}

---

**작성일**: {TBD}
**작성자**: {TBD}
**버전**: v1.0
