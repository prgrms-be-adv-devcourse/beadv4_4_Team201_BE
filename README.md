# Giftify Backend

[![CI - Build & Test](https://github.com/prgrms-be-adv-devcourse/beadv4_4_Team201_BE/actions/workflows/ci.yml/badge.svg)](https://github.com/prgrms-be-adv-devcourse/beadv4_4_Team201_BE/actions/workflows/ci.yml)

N/A - 프로젝트 설명이 추가될 예정입니다.

---

## 프로젝트 목표

1. **N/A** - 비즈니스 목표가 추가될 예정입니다.
2. **N/A**
3. **N/A**

---

## 주요 기능

### MVP (Minimum Viable Product)

| 우선순위 | 기능 | 설명 | 구현 상태 |
|:----:|----|----|:----:|
| N/A | N/A | N/A | 미구현 |

### 주요 기능 구현률

```
전체 기능: ░░░░░░░░░░░░░░░░░░░░░  0% (0/TBD)
```

---

## 기술 스택

### Backend

| 구분 | 기술 | 버전 |
|----|----|-----|
| Language | Java | 21 |
| Framework | Spring Boot | 3.2.2 |
| Security | N/A | - |
| ORM | N/A | - |
| Query | N/A | - |

### Database

| 구분 | 기술 | 용도 |
|----|----|----|
| RDBMS | N/A | 메인 데이터베이스 |
| In-Memory | N/A | 테스트용 |
| Migration | N/A | - |

### Testing

| 구분 | 기술 |
|----|----|
| Framework | JUnit 5 |
| Assertion | N/A |
| Mocking | N/A |
| Container | N/A |

### Build & Infra

| 구분 | 기술 |
|----|----|
| Build | Gradle 8.5 (Kotlin DSL) |
| Container | N/A |
| Monitoring | N/A |
| API Docs | N/A |

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

### 2. 데이터베이스 실행

N/A - Docker Compose 설정이 추가될 예정입니다.

### 3. 애플리케이션 실행

```bash
# 빌드 (테스트 제외)
./gradlew clean build -x test

# 애플리케이션 실행
./gradlew :bootstrap:api-server:bootRun
```

### 4. Health Check

```bash
# Root Health Check
curl http://localhost:8080/health

# Member Health Check
curl http://localhost:8080/member/health

# Auth Health Check
curl http://localhost:8080/auth/health

# Payment Health Check
curl http://localhost:8080/payment/health
```

---

## API 문서 (Swagger UI)

N/A - API 문서 설정이 추가될 예정입니다.

---

## API 엔드포인트

### Health Check (개발용)

| Method | Endpoint | 설명 |
|--------|----------|-----|
| GET | `/health` | 루트 헬스체크 |
| GET | `/member/health` | 회원 모듈 헬스체크 |
| GET | `/auth/health` | 인증 모듈 헬스체크 |
| GET | `/payment/health` | 결제 모듈 헬스체크 |

### 비즈니스 API

N/A - 비즈니스 API가 구현될 예정입니다.


---

## 문서

| 문서 | 설명 |
|-----|-----|
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
N/A - 커버리지 도구가 설정될 예정입니다
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
