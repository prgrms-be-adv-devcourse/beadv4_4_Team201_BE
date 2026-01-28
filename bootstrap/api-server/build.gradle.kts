// Bootstrap API Server - 애플리케이션 진입점

plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    // BC 모듈들
    implementation(project(":bc:shared"))
    implementation(project(":bc:member"))
    implementation(project(":bc:payment"))

    // Support 모듈들
    implementation(project(":support:common"))
    implementation(project(":support:logging"))
    implementation(project(":support:security"))
    implementation(project(":support:web"))
    implementation(project(":support:jpa"))

    // Utilities
    implementation(libs.dotenv)

    // Spring Boot Starters
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.actuator)

    // Database
    runtimeOnly(libs.h2)
    runtimeOnly(libs.postgresql)

    // Monitoring
    runtimeOnly(libs.micrometer.prometheus)

    // Testing
    testImplementation(libs.rest.assured)
}
