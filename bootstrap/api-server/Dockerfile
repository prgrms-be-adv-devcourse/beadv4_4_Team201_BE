# =============================================================================
# Giftify API Server Dockerfile
# =============================================================================
# Multi-stage build for optimized image size
#
# 사용법:
#   로컬 빌드:    docker build -t giftify-api-server -f bootstrap/api-server/Dockerfile .
#   CI 빌드:      docker build --target runtime-ci -t giftify-api-server -f bootstrap/api-server/Dockerfile .
#   컨테이너 실행: docker run -p 8080:8080 giftify-api-server
# =============================================================================

# -----------------------------------------------------------------------------
# Stage 1: Build (로컬 개발용 - 소스에서 빌드)
# -----------------------------------------------------------------------------
FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /app

# Gradle wrapper와 설정 파일 먼저 복사 (캐시 최적화)
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts gradle.properties ./

# 소스 코드 복사
COPY bc bc
COPY support support
COPY bootstrap bootstrap

# 실행 권한 부여 및 빌드
RUN chmod +x gradlew && \
    ./gradlew :bootstrap:api-server:bootJar --no-daemon -x test

# -----------------------------------------------------------------------------
# Stage 2: Runtime (로컬 개발용 - builder에서 JAR 복사)
# -----------------------------------------------------------------------------
FROM eclipse-temurin:25-jre-alpine AS runtime

# 보안: 비루트 사용자 생성
RUN addgroup -g 1000 spring && \
    adduser -u 1000 -G spring -D spring

WORKDIR /app

# builder 스테이지에서 빌드된 JAR 복사
COPY --from=builder /app/bootstrap/api-server/build/libs/api-server-*-SNAPSHOT.jar app.jar

# 소유권 변경
RUN chown spring:spring app.jar

# 비루트 사용자로 전환
USER spring:spring

# 헬스체크
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 포트 노출
EXPOSE 8080

# JVM 최적화 옵션
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# -----------------------------------------------------------------------------
# Stage 3: Runtime CI (CI용 - 호스트에서 빌드된 JAR 직접 복사)
# -----------------------------------------------------------------------------
FROM eclipse-temurin:25-jre-alpine AS runtime-ci

# 보안: 비루트 사용자 생성
RUN addgroup -g 1000 spring && \
    adduser -u 1000 -G spring -D spring

WORKDIR /app

# CI에서 빌드된 JAR를 호스트에서 직접 복사
COPY bootstrap/api-server/build/libs/api-server-*-SNAPSHOT.jar app.jar

# 소유권 변경
RUN chown spring:spring app.jar

# 비루트 사용자로 전환
USER spring:spring

# 헬스체크
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 포트 노출
EXPOSE 8080

# JVM 최적화 옵션
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
