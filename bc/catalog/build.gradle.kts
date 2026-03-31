plugins {
    id("java")
}

group = "app.giftify"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":support:jpa"))
    implementation(project(":support:security"))
    implementation(project(":support:web"))
    implementation(project(":support:common"))
    implementation(project(":bc:shared"))

    // Spring Boot Starters
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.security)
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework:spring-tx")

    // Spring Modulith
    implementation(libs.spring.modulith.core)
    implementation(libs.spring.modulith.jpa)

    // QueryDSL
    implementation("com.querydsl:querydsl-jpa:${libs.versions.querydsl.get()}:jakarta")
    annotationProcessor("com.querydsl:querydsl-apt:${libs.versions.querydsl.get()}:jakarta")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")

    // ElasticSearch
    implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Apache Commons
    implementation(libs.commons.lang3)

    // Test
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.webmvc.test)
    testImplementation(libs.spring.security.test)
    testImplementation(libs.archunit)
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
    testRuntimeOnly(libs.h2)
    testImplementation(libs.testcontainers.elasticsearch)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.spring.boot.testcontainers)

    // Mock Data Generator
    testImplementation("net.datafaker:datafaker:2.1.0")

    // Retry
    implementation(libs.spring.retry)
    implementation(libs.spring.boot.starter.aspectj)
}

tasks.test {
    useJUnitPlatform()
}
