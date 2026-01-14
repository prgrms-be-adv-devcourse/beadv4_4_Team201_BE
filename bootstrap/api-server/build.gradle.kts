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
    // implementation(project(":bc:money:adapter"))  // 주석 처리 (향후 활성화)

    // Support 모듈들
    implementation(project(":support:common"))
    implementation(project(":support:logging"))
    implementation(project(":support:security"))
    implementation(project(":support:web"))

    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Database
    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("com.h2database:h2")

    // Monitoring
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    // Testing
    testImplementation("io.rest-assured:rest-assured")
}

// 실행 가능한 JAR 생성
tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
}
