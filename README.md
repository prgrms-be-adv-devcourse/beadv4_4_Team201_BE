# Giftify Backend

[![CI - develop](https://github.com/prgrms-be-adv-devcourse/beadv4_4_Team201_BE/actions/workflows/ci.yml/badge.svg?branch=develop)](https://github.com/prgrms-be-adv-devcourse/beadv4_4_Team201_BE/actions/workflows/ci.yml?query=branch%3Adevelop)
[![CI - Build & Test](https://github.com/prgrms-be-adv-devcourse/beadv4_4_Team201_BE/actions/workflows/ci.yml/badge.svg)](https://github.com/prgrms-be-adv-devcourse/beadv4_4_Team201_BE/actions/workflows/ci.yml)
[![CodeQL](https://github.com/prgrms-be-adv-devcourse/beadv4_4_Team201_BE/actions/workflows/github-code-scanning/codeql/badge.svg)](https://github.com/prgrms-be-adv-devcourse/beadv4_4_Team201_BE/actions/workflows/github-code-scanning/codeql)
[![Release](https://img.shields.io/github/v/release/prgrms-be-adv-devcourse/beadv4_4_Team201_BE?label=Release)](https://github.com/prgrms-be-adv-devcourse/beadv4_4_Team201_BE/releases)
![Java](https://img.shields.io/badge/Java-25-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-6DB33F)

---

## Project Overview

Giftify는 여러 친구가 함께 선물을 펀딩하는 플랫폼입니다. 친구의 위시리스트를 확인하고, 마음에 드는 상품에 공동으로 펀딩하여 특별한 선물을 만들 수 있습니다.

---

## Key Features

### Feature Implementation Status

```
전체 기능: █████████████████████  100% (57/57 UseCase)
```

| Feature | Description | Status |
|---------|-------------|--------|
| 회원 관리 | Auth0 기반 인증, Progressive Profiling | ✅ |
| 친구 관계 | 친구 요청/수락/거절, 친구 위시리스트 조회 | ✅ |
| 위시리스트 | 상품 담기, 공개/비공개 설정, 친구 공유 | ✅ |
| 상품 관리 | 판매자 상품 등록/승인, Elasticsearch 검색 | ✅ |
| 펀딩 | 친구 위시리스트 아이템에 공동 펀딩 참여 | ✅ |
| 결제 | TossPayments PG 연동, 예치금 충전 | ✅ |
| 지갑 | 충전/차감/출금, 잔액 관리, 이력 조회 | ✅ |
| 정산 | 펀딩 완료 → 판매자 정산, 배치 처리 | ✅ |
| 알림 | SSE 실시간 알림, 읽음 처리 | ✅ |
| 장바구니 | 위시리스트 아이템 장바구니 담기 | ✅ |

---

## Tech Stack

| Category | Technology | Version |
|----------|-----------|---------|
| Language | Java | 25 |
| Framework | Spring Boot | 4.0.3 |
| Architecture | Spring Modulith | 2.0.3 |
| Database | PostgreSQL | 16 |
| ORM | JPA + QueryDSL | 5.1.0 |
| Search | Elasticsearch | 9.2.4 |
| Cache | Redis | 7 |
| Auth | Auth0 (OAuth2 + JWT) | - |
| Payment | TossPayments | - |
| Messaging | Kafka (Redpanda) | - |
| Migration | Flyway (Module-aware) | - |
| Monitoring | Prometheus + Grafana | - |
| Load Test | k6 | 1.6.1 |
| Testing | JUnit 5, Mockito, Testcontainers | - |
| Build | Gradle 8.14 (Kotlin DSL) | - |
| API Docs | SpringDoc OpenAPI | 3.0.2 |
| CI/CD | GitHub Actions + ArgoCD | - |
| Infra | GCP + k3s + Cloudflare Tunnel | - |

---

## Architecture

### System Architecture

> Architecture diagram: [`docs/images/architecture.png`](docs/images/architecture.png) 

Modular Monolith 아키텍처를 채택하여 Spring Modulith와 Hexagonal Architecture를 결합했습니다. 각 BC(Bounded Context)는 독립적으로 개발 및 테스트 가능하며, BC 간 통신은 명확한 Port Interface와 Event로만 이루어집니다.

### Module Structure

```
bc/
├── shared/          # Shared kernel (events, VO, types)
├── member/          # Member, Auth, Friendship
├── catalog/         # Product, Wishlist, Cart
├── core/            # Order, Payment, Funding, Wallet
├── settlement/      # Settlement (batch)
└── notification/    # Notification (SSE)

bootstrap/
└── api-server/      # Spring Boot application entry point

support/
├── common/          # Utilities, exceptions, response format
├── security/        # Auth filters, principal
├── web/             # CORS, exception handler, idempotency
├── logging/         # Logging config
└── jpa/             # JPA auditing, QueryDSL config
```

### Inter-Module Communication

- **동기 통신**: Port Interface + Internal API Client (조회 Query)
- **비동기 통신**: `@ApplicationModuleListener` (명령 Command)
- **규칙**: BC 간 직접 import 금지, Cross-schema JOIN 금지 (ID 참조만)

---

## Load Test Results

### SLO Definition

| Metric | Current SLO | Target SLO | Google SRE |
|--------|:----------:|:----------:|:----------:|
| Read API p95 | < 500ms | < 200ms | < 100ms |
| Write API p95 | < 1000ms | < 500ms | < 250ms |
| Error Rate | < 5% | < 0.5% | < 0.1% |

### Baseline Results (2026-03-16, Cycle 0)

| Test | VU | RPS | p95 | Error Rate | Status |
|------|-----|-----|-----|-----------|--------|
| Stress | 120 | 177.76 | 270.25ms | 0.002% | ✅ PASS |
| Soak (33min) | 30 | 51 | 8.76ms | 0% | ✅ PASS |
| Spike | 200 | 95 | 1.34s | 0% | ⚠️ SLO 위반 |
| Breakpoint | 294 | 155 | 2.01s | 0% | ⚠️ ABORT |

Stress Test(120 VU) 기준 모든 SLO 충족. Spike/Breakpoint는 한계 탐색 목적으로 일부 위반 확인. 자세한 분석은 아래 보고서 참조.

→ Detailed report: [`docs/reports/2026-03-16-load-test-report.md`](docs/reports/2026-03-16-load-test-report.md)

---

## Getting Started

### Prerequisites

- Java 25+
- Gradle 8.14+
- Docker

### 1. Clone & Environment Setup

```bash
git clone https://github.com/prgrms-be-adv-devcourse/beadv4_4_Team201_BE.git
cd giftify-be
cp .env.sample .env
```

### 2. Run with Docker Compose

```bash
docker-compose up -d
docker-compose logs -f api-server
```

### 3. Run Directly (without Docker)

```bash
./gradlew clean build -x test
./gradlew :bootstrap:api-server:bootRun
```

### 4. Health Check

```bash
curl http://localhost:8080/actuator/health
```

---

## API Documentation

애플리케이션 실행 후 아래 URL에서 API 문서를 확인할 수 있습니다.

| Endpoint | URL | Description |
|----------|-----|-------------|
| Swagger UI | http://localhost:8080/swagger-ui.html | 인터랙티브 API 문서 |
| OpenAPI JSON | http://localhost:8080/v3/api-docs | OpenAPI 3.0 스펙 (JSON) |

인증이 필요한 API는 Swagger UI 상단의 `Authorize` 버튼을 클릭하여 JWT 토큰을 설정하세요.

---

## Testing

```bash
./gradlew test
./gradlew :bc:member:test
./gradlew jacocoAggregatedReport
```

---

## Project Structure

```
.
├── bc/                     # Bounded Contexts
│   ├── catalog/           # Product, Wishlist, Cart
│   ├── core/              # Order, Payment, Funding, Wallet
│   ├── member/            # Member, Auth, Friendship
│   ├── notification/      # Notification (SSE)
│   ├── settlement/        # Settlement (batch)
│   └── shared/            # Shared kernel
├── bootstrap/
│   └── api-server/        # Spring Boot entry point
├── support/               # Technical modules (no domain logic)
│   ├── common/
│   ├── jpa/
│   ├── logging/
│   ├── security/
│   └── web/
├── infra/                 # Infrastructure configurations
│   ├── k3s/              # Kubernetes manifests
│   ├── elasticsearch/    # Elasticsearch configuration
│   └── monitoring/       # Prometheus, Grafana, k6
└── docs/                 # Documentation
```

---

## Related Documents

| Document | Description |
|----------|-------------|
| [Load Test Report](docs/reports/2026-03-16-load-test-report.md) | Cycle 0 Baseline 부하테스트 결과 |
| [Swagger UI](http://localhost:8080/swagger-ui.html) | API 문서 (로컬 실행 후) |

### GitHub Wiki

| Page | Description |
|------|-------------|
| [Home](https://github.com/prgrms-be-adv-devcourse/beadv4_4_Team201_BE/wiki) | 프로젝트 위키 홈 |
| [Payment System](https://github.com/prgrms-be-adv-devcourse/beadv4_4_Team201_BE/wiki/Payment-System) | 결제 시스템 아키텍처 (TossPayments 연동, 멱등성) |
| [Funding Domain](https://github.com/prgrms-be-adv-devcourse/beadv4_4_Team201_BE/wiki/Funding-Domain) | 펀딩 도메인 설계 (동시성 제어, 상태 전이) |
| [Auth Architecture](https://github.com/prgrms-be-adv-devcourse/beadv4_4_Team201_BE/wiki/Auth-Architecture) | Auth0 인증 아키텍처 (SPA SDK, Token Blacklist) |
| [Event Architecture](https://github.com/prgrms-be-adv-devcourse/beadv4_4_Team201_BE/wiki/Event-Architecture) | Spring Modulith 이벤트 기반 통신 |
| [ADR: Modular Monolith](https://github.com/prgrms-be-adv-devcourse/beadv4_4_Team201_BE/wiki/ADR-Modular-Monolith) | 모듈러 모놀리스 아키텍처 결정 배경 |
| [ADR: Module-aware Flyway](https://github.com/prgrms-be-adv-devcourse/beadv4_4_Team201_BE/wiki/ADR-Module-aware-Flyway) | 모듈별 독립 Flyway 마이그레이션 전환 |
| [Load Test Guide](https://github.com/prgrms-be-adv-devcourse/beadv4_4_Team201_BE/wiki/Load-Test-Guide) | k6 부하테스트 실행 가이드 |

---

## Contributors

TBD

---

## License

TBD
