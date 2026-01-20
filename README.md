# Giftify Backend

![main](https://github.com/github/docs/actions/workflows/main.yml/badge.svg?branch=main)
![develop](https://github.com/github/docs/actions/workflows/main.yml/badge.svg?branch=develop) 
[![CI - Build & Test](https://github.com/prgrms-be-adv-devcourse/beadv4_4_Team201_BE/actions/workflows/ci.yml/badge.svg)](https://github.com/prgrms-be-adv-devcourse/beadv4_4_Team201_BE/actions/workflows/ci.yml) [![CodeQL](https://github.com/prgrms-be-adv-devcourse/beadv4_4_Team201_BE/actions/workflows/github-code-scanning/codeql/badge.svg)](https://github.com/prgrms-be-adv-devcourse/beadv4_4_Team201_BE/actions/workflows/github-code-scanning/codeql) [![PR Pipeline](https://github.com/prgrms-be-adv-devcourse/beadv4_4_Team201_BE/actions/workflows/pr.yml/badge.svg)](https://github.com/prgrms-be-adv-devcourse/beadv4_4_Team201_BE/actions/workflows/pr.yml) [![Pull Request Labeler](https://github.com/prgrms-be-adv-devcourse/beadv4_4_Team201_BE/actions/workflows/pr-labeler.yml/badge.svg)](https://github.com/prgrms-be-adv-devcourse/beadv4_4_Team201_BE/actions/workflows/pr-labeler.yml) 

N/A - 프로젝트 설명이 추가될 예정입니다.

---

## 프로젝트 목표

1. **N/A** - 비즈니스 목표가 추가될 예정입니다.
2. **N/A**
3. **N/A**

---

## 주요 기능

### MVP (Minimum Viable Product)

| 우선순위 | 기능  | 설명  | 구현 상태 |
|:----:|-----|-----|:-----:|
| N/A  | N/A | N/A |  미구현  |

### 주요 기능 구현률

```
전체 기능: ░░░░░░░░░░░░░░░░░░░░░  0% (0/TBD)
```

---

## 기술 스택

### Backend

| 구분        | 기술              | 버전    |
|-----------|-----------------|-------|
| Language  | Java            | 21    |
| Framework | Spring Boot     | 3.2.2 |
| Security  | Spring Security | -     |
| ORM       | JPA             | -     |
| Query     | QueryDSL        | -     |

### Database

| 구분        | 기술         | 용도        |
|-----------|------------|-----------|
| RDBMS     | PostgreSQL | 메인 데이터베이스 |
| In-Memory | H2         | 로컬 테스트용   |
| Migration | N/A        | -         |

### Testing

| 구분        | 기술      |
|-----------|---------|
| Framework | JUnit 5 |
| Assertion | N/A     |
| Mocking   | N/A     |
| Container | N/A     |

### Build & Infra

| 구분         | 기술                      |
|------------|-------------------------|
| Build      | Gradle 8.5 (Kotlin DSL) |
| Container  | Docker Container        |
| Monitoring | Grafana / Prometheus    |
| API Docs   | SpringDoc OpenAPI 2.3.0 |

---

## Architecture

### 아키텍처 다이어그램

N/A - 아키텍쳐 다이어그램이 추가될 예정입니다.

### ER 다이어그램

N/A - 데이터베이스 스키마가 설계될 예정입니다.

---

## 실행 방법

### 사전 요구사항

- Java 21+
- Gradle 8.5+

### 1. 저장소 클론

```bash
git clone https://github.com/prgrms-be-adv-devcourse/beadv4_4_Team201_BE.git
cd giftify-be
```

### 2. 환경 변수 설정

```bash
# .env.sample을 복사하여 .env 파일 생성
cp .env.sample .env

# .env 파일을 편집하여 필요한 환경 변수 설정
# - PostgreSQL 데이터베이스 설정
# - Auth0 인증 설정
```

### 3. Docker Compose로 실행

#### 옵션 A: 로컬 빌드

```bash
# Docker Compose로 PostgreSQL + API 서버 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f api-server

# 중지
docker-compose down
```

### 4. 애플리케이션 직접 실행 (Docker 없이)

```bash
# 빌드 (테스트 제외)
./gradlew clean build -x test

# 애플리케이션 실행
./gradlew :bootstrap:api-server:bootRun
```

### 5. Health Check

```bash
# Root Health Check
curl http://localhost:8080/health

# Member Health Check
curl http://localhost:8080/member/health

# Auth Health Check
curl http://localhost:8080/auth/health

```

### 6. 모니터링

Docker Compose에 Prometheus와 Grafana가 포함되어 있습니다.

#### 접속 URL

| 서비스         | URL                                       | 설명                  |
|-------------|-------------------------------------------|---------------------|
| Prometheus  | http://localhost:9090                     | 메트릭 수집/조회           |
| Grafana     | http://localhost:3000                     | 대시보드                |
| API Metrics | http://localhost:8080/actuator/prometheus | Spring Actuator 메트릭 |

#### Grafana 대시보드

JVM Micrometer 대시보드(ID: 19004)가 자동으로 프로비저닝됩니다.

1. http://localhost:3000 접속
2. Dashboards 메뉴에서 `Spring Boot 3.x Statistics` 대시보드 확인

#### k6 부하 테스트 예시 _(사용자 10명, 헬스체크 엔드포인트 호출)_

```bash
# Homebrew로 설치한 경우
k6 run infra/monitoring/k6/scripts/smoke-test.js

# Docker로 실행하는 경우
docker run --rm \
  -v $(pwd)/infra/monitoring/k6/scripts:/scripts \
  --add-host=host.docker.internal:host-gateway \
  grafana/k6 run /scripts/smoke-test.js
```

---

#### 로컬에서 이미지 빌드

```bash
# Dockerfile로 직접 빌드
docker build -t giftify-api-server:local .

# 또는 docker-compose로 빌드
docker-compose build
docker-compose up -d
```

---

## API 문서 (Swagger UI)

애플리케이션 실행 후 아래 URL에서 API 문서를 확인할 수 있습니다.

| 엔드포인트        | URL                                    | 설명                    |
|--------------|----------------------------------------|-----------------------|
| Swagger UI   | http://localhost:8080/swagger-ui.html  | 인터랙티브 API 문서          |
| OpenAPI JSON | http://localhost:8080/v3/api-docs      | OpenAPI 3.0 스펙 (JSON) |
| OpenAPI YAML | http://localhost:8080/v3/api-docs.yaml | OpenAPI 3.0 스펙 (YAML) |

> **인증**이 필요한 API 요청 시 `Authorization: Bearer {JWT_TOKEN}` 헤더가 필요합니다.
> Swagger UI에서 상단의 `Authorize` 버튼을 클릭하여 토큰을 설정할 수 있습니다.

---

## API 엔드포인트

### Health Check _(개발용 - 추후 삭제 예정)_

| Method | Endpoint         | 설명         |
|--------|------------------|------------|
| GET    | `/health`        | 루트 헬스체크    |
| GET    | `/member/health` | 회원 모듈 헬스체크 |
| GET    | `/auth/health`   | 인증 모듈 헬스체크 |

---

## 문서

| 문서  | 설명               |
|-----|------------------|
| N/A | 추가 문서가 작성될 예정입니다 |
| N/A | 추가 문서가 작성될 예정입니다 |
| N/A | 추가 문서가 작성될 예정입니다 |

---

## 브랜치 전략

N/A - Git 브랜치 전략이 작성될 예정입니다.

---

## 테스트 실행

```bash
# 전체 테스트
./gradlew test

# 특정 모듈 테스트
./gradlew :bc:member:test

# 테스트 커버리지 리포트
./gradlew jacocoAggregatedReport
```

---

## 라이선스

N/A

---

## 기여자

N/A

---

## 연락처

N/A
