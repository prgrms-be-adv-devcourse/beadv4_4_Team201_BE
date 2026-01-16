# ==============================================================================
# Giftify API Server Dockerfile
# ==============================================================================
# 현재 위치: 프로젝트 루트
#
# [현재 구조]
# - 단일 서버 구조 (bootstrap:api-server)
# - 생산성을 위해 docker-compose.yml과 함께 루트에 배치
#
# [향후 멀티 서버 전환 시]
# - 각 서버별 Dockerfile을 해당 모듈 내로 이동 필요
#   예시:
#   - bootstrap/api-server/Dockerfile
#
# - docker-compose.yml에서 dockerfile 경로 지정 필요
#   예시: dockerfile: bootstrap/api-server/Dockerfile
# ==============================================================================

# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Gradle wrapper와 설정 파일 먼저 복사 (캐싱 최적화)
COPY gradlew .
COPY gradle gradle
COPY settings.gradle.kts .
COPY build.gradle.kts .

# 의존성 다운로드 (캐싱 레이어)
RUN ./gradlew dependencies --no-daemon || true

# 소스 코드 복사
COPY . .

# 빌드
RUN ./gradlew :bootstrap:api-server:bootJar --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 빌드된 JAR 복사
COPY --from=builder /app/bootstrap/api-server/build/libs/*.jar app.jar

# 헬스체크
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
