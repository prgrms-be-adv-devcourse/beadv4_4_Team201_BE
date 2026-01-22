// Bootstrap API Server - 애플리케이션 진입점

plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    // BC 모듈들
    implementation(project(":bc:shared"))
    implementation(project(":bc:member"))
    implementation(project(":bc:auth"))
    implementation(project(":bc:funding"))
    implementation(project(":bc:money:adapter"))

    // Support 모듈들
    implementation(project(":support:common"))
    implementation(project(":support:logging"))
    implementation(project(":support:security"))
    implementation(project(":support:web"))

    implementation("io.github.cdimascio:dotenv-java:3.0.0")

    // Spring Boot Starters
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.actuator)

    // Database
    runtimeOnly(libs.h2)
    runtimeOnly("org.postgresql:postgresql")

    // Monitoring
    runtimeOnly(libs.micrometer.registry.prometheus)

    // Testing
    testImplementation(libs.rest.assured)
}

// 실행 가능한 JAR 생성
tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
}
