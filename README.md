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

### 5. k3d (Kubernetes) 로 실행

로컬에서 k3d(Docker 내 K3s)를 사용하여 Kubernetes 환경으로 배포할 수 있습니다.

#### 사전 요구사항

| 도구 | 설치 방법 | 확인 명령 |
|------|----------|----------|
| Docker | [docker.com](https://docs.docker.com/get-docker/) 또는 OrbStack | `docker version` |
| k3d | `brew install k3d` | `k3d version` |
| kubectl | `brew install kubectl` | `kubectl version --client` |

#### Quick Start

```bash
# 1. k3d 클러스터 생성
./infra/k3s/scripts/k3s-local-create.sh

# 2. secrets.yaml 생성 (최초 1회)
cp infra/k3s/base/secrets.yaml.template infra/k3s/base/secrets.yaml
#    -> secrets.yaml 열어서 <REPLACE> 부분을 .env 파일의 값으로 교체
#    -> SPRING_PROFILES_ACTIVE 를 "dev" 로 변경

# 3. 빌드 + 배포
./infra/k3s/scripts/k3s-local-deploy.sh

# 4. 확인
kubectl get pods -n giftify
curl http://localhost:8080/actuator/health
```

#### secrets.yaml 작성 가이드

`secrets.yaml.template`을 복사한 뒤 프로젝트 루트의 `.env` 파일을 참고하여 값을 채웁니다.

```
secrets.yaml.template 항목         .env 대응 키
------------------------------    ----------------------
DB_PASSWORD                       DB_PASSWORD
REDIS_PASSWORD                    REDIS_PASSWORD
AUTH0_DOMAIN                      AUTH0_DOMAIN
AUTH0_CLIENT_ID                   AUTH0_CLIENT_ID
AUTH0_CLIENT_SECRET               AUTH0_CLIENT_SECRET
AUTH0_AUDIENCE                    AUTH0_AUDIENCE
TOSSPAYMENTS_SECRET_KEY           TOSSPAYMENTS_SECRET_KEY
PAYMENT_ENCRYPTION_PASSWORD       PAYMENT_ENCRYPTION_PASSWORD
PAYMENT_ENCRYPTION_SALT           PAYMENT_ENCRYPTION_SALT
POSTGRES_PASSWORD                 DB_PASSWORD (동일 값)
```

주의사항:
- `SPRING_PROFILES_ACTIVE`를 `"dev"`로 설정해야 합니다 (템플릿 기본값은 `"prod"`)
- `TUNNEL_TOKEN`은 로컬 테스트 시 아무 값이나 넣어도 됩니다 (cloudflared가 비활성화됨)
- `secrets.yaml`은 `.gitignore`에 등록되어 있으므로 커밋되지 않습니다

#### 디렉토리 구조

```
infra/k3s/
  base/                          # 환경 공통 매니페스트
    namespace.yaml               #   giftify 네임스페이스
    secrets.yaml.template        #   시크릿 템플릿
    kustomization.yaml           #   Kustomize 리소스 목록
    apps/
      backend/                   #   API Server (Deployment + Service)
      postgres/                  #   PostgreSQL 16 (StatefulSet + Headless Service)
      redis/                     #   Redis 7 (Deployment + Service)
      prometheus/                #   Prometheus (Deployment + Service + ConfigMap)
      grafana/                   #   Grafana (Deployment + Service + ConfigMap + Dashboards)
      k6/                        #   k6 부하 테스트 (Job + ConfigMap)
    traefik/
      ingress-routes/api.yaml    #   Traefik IngressRoute (/ -> backend:8080)
      middleware/rate-limit.yaml  #   Rate Limit (100req/s, burst 50)
    cloudflare/
      tunnel-deployment.yaml     #   Cloudflare Tunnel (prod 전용)
  overlays/
    dev-k3s/                     # 로컬 k3d 환경 오버레이
    prod/                        # EC2 K3s 운영 환경 오버레이
  scripts/
    k3s-local-create.sh          # k3d 클러스터 생성
    k3s-local-deploy.sh          # 이미지 빌드 + k3d 배포
    k3s-local-delete.sh          # k3d 클러스터 삭제
```

#### 스크립트 상세

**k3s-local-create.sh** - k3d 클러스터를 생성합니다. 포트 매핑:

```
호스트 8080 -> 클러스터 80  (HTTP, Traefik)
호스트 8443 -> 클러스터 443 (HTTPS, Traefik)
```

```bash
# 기본 클러스터명 "giftify"
./infra/k3s/scripts/k3s-local-create.sh

# 커스텀 클러스터명
./infra/k3s/scripts/k3s-local-create.sh my-cluster
```

**k3s-local-deploy.sh** - Docker 이미지를 빌드하고 k3d 클러스터에 배포합니다.

```
1. ./gradlew :bootstrap:api-server:bootJar (JAR 빌드)
2. docker build -t giftify-backend:local
3. k3d image import (이미지를 k3d 클러스터로 전송)
4. kubectl apply -f secrets.yaml
5. kubectl apply -k overlays/dev-k3s
6. kubectl rollout status (배포 완료 대기)
```

**k3s-local-delete.sh** - 클러스터를 삭제합니다. PVC 데이터도 함께 삭제됩니다.

```bash
./infra/k3s/scripts/k3s-local-delete.sh
```

#### 주요 명령어

```bash
# Pod 상태 확인
kubectl get pods -n giftify

# 특정 Pod 로그
kubectl logs -f <pod-name> -n giftify

# Pod 재시작 (이미지 업데이트 없이)
kubectl rollout restart deployment/backend -n giftify

# 코드 변경 후 재배포
./infra/k3s/scripts/k3s-local-deploy.sh

# Kustomize 결과 미리보기 (적용 없이)
kubectl kustomize infra/k3s/overlays/dev-k3s/

# PostgreSQL 접속
kubectl exec -it postgres-0 -n giftify -- psql -U giftify -d giftify_db

# 모니터링 대시보드 접속 (Traefik IngressRoute, port-forward 불필요)
# Grafana:    http://grafana.localhost:8080
# Prometheus: http://prometheus.localhost:8080
# Redpanda:         http://redpanda.localhost:8080
# Redpanda Console: http://redpanda-console.localhost:8080
```

#### docker-compose와의 관계

k3d 환경과 기존 docker-compose 환경은 같은 포트(8080)를 사용하므로 동시에 실행할 수 없습니다.
k3d를 사용할 때는 docker-compose를 먼저 중지하세요.

```bash
# docker-compose 중지
docker-compose down

# k3d 클러스터 생성 및 배포
./infra/k3s/scripts/k3s-local-create.sh
./infra/k3s/scripts/k3s-local-deploy.sh
```

두 환경 모두 `SPRING_PROFILES_ACTIVE=dev`를 사용하며, 동일한 `.env` 값을 참조합니다.

#### Troubleshooting

**Spring Boot 기동이 느린 경우** - dev-k3s 오버레이에 `startupProbe`가 설정되어 있어 최대 약 10분까지 기동을 기다립니다.
`kubectl logs -f <pod-name> -n giftify`로 기동 진행 상황을 확인하세요.

**OrbStack 사용 시 네트워크 문제** - OrbStack의 프록시 설정이 k3d 컨테이너의 DNS 해석을 방해할 수 있습니다.
OrbStack 설정에서 프록시를 비활성화하거나, 문제 지속 시 OrbStack을 재시작하세요.

**Pod가 CrashLoopBackOff인 경우**

```bash
# 로그 확인
kubectl logs <pod-name> -n giftify

# 이전 컨테이너 로그 (재시작된 경우)
kubectl logs <pod-name> -n giftify --previous
```

흔한 원인:
- `secrets.yaml` 미적용 또는 값 누락
- PostgreSQL이 아직 기동 중 (backend보다 먼저 Ready 상태여야 함)
- Docker 이미지가 k3d에 임포트되지 않음 (`k3d image import` 확인)

### 6. Health Check

```bash
# Root Health Check
curl http://localhost:8080/health

# Member Health Check
curl http://localhost:8080/member/health

# Auth Health Check
curl http://localhost:8080/auth/health

```

### 7. 모니터링

Docker Compose와 k3d 환경 모두에서 Prometheus + Grafana 모니터링을 제공합니다.

#### 접속 URL

| 서비스         | Docker Compose                            | k3d                                        | 설명                  |
|-------------|-------------------------------------------|--------------------------------------------|---------------------|
| Grafana     | http://localhost:3000                     | http://grafana.localhost:8080              | 대시보드              |
| Prometheus  | http://localhost:9090                     | http://prometheus.localhost:8080           | 메트릭 수집/조회       |
| Redpanda    | -                                         | http://redpanda.localhost:8080             | 메시지 브로커 REST API |
| Redpanda Console | -                                    | http://redpanda-console.localhost:8080     | 메시지 브로커 웹 UI    |
| API Metrics | http://localhost:8080/actuator/prometheus | http://localhost:8080/actuator/prometheus   | Spring Actuator 메트릭 |

#### Grafana 대시보드

아래 대시보드가 자동으로 프로비저닝됩니다.

| 대시보드 | 설명 |
|---------|------|
| Spring Boot 3.x Statistics | JVM Micrometer 메트릭 (ID: 19004) |
| Token Blacklist Security | 토큰 블랙리스트 보안 메트릭 |

1. http://grafana.localhost:8080 접속 (admin/admin), Docker Compose 시 http://localhost:3000
2. Dashboards 메뉴에서 대시보드 확인

#### k6 부하 테스트

```bash
# Docker Compose 환경 - Homebrew로 설치한 경우
k6 run infra/monitoring/k6/scripts/smoke-test.js

# Docker Compose 환경 - Docker로 실행하는 경우
docker run --rm \
  -v $(pwd)/infra/monitoring/k6/scripts:/scripts \
  --add-host=host.docker.internal:host-gateway \
  grafana/k6 run /scripts/smoke-test.js

# k3d 환경 - 클러스터 내 Job으로 실행
kubectl apply -f infra/k3s/base/apps/k6/job.yaml -n giftify
kubectl logs job/k6-smoke-test -n giftify
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

N/A - Git 브랜치 전략- Github Flow 이 작성될 예정입니다.

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
