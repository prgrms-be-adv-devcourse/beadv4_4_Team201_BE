// Bootstrap API Server - 애플리케이션 진입점

plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    implementation(project(":bc:shared"))
    implementation(project(":bc:member"))
    implementation(project(":bc:catalog"))
    implementation(project(":bc:core"))
    implementation(project(":bc:settlement"))
    implementation(project(":bc:notification"))
    implementation(project(":support:common"))
    implementation(project(":support:logging"))
    implementation(project(":support:security"))
    implementation(project(":support:web"))
    implementation(project(":support:jpa"))
    implementation(libs.dotenv)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.restclient)
    implementation(libs.spring.boot.jackson2)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.flyway)
    implementation(libs.spring.retry)
    implementation("org.springframework.boot:spring-boot-starter-aspectj")
    implementation(libs.spring.modulith.kafka)
    implementation(libs.spring.modulith.jpa)
    implementation(libs.spring.modulith.runtime)
    implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")

    runtimeOnly(libs.h2)
    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.flyway.postgresql)
    runtimeOnly(libs.micrometer.prometheus)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.rest.assured)
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.awaitility:awaitility")
}
