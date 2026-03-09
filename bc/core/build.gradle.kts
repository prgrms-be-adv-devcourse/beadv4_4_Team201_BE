plugins {
    id("java")
}

group = "app.giftify"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":bc:shared"))
    implementation(project(":support:security"))
    implementation(project(":support:jpa"))
    implementation(project(":support:common"))

    // Spring Boot Starters
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    // Spring Modulith
    implementation(libs.spring.modulith.core)
    implementation(libs.spring.modulith.jpa)

    // API Documentation
    implementation(libs.springdoc.openapi)

    // QueryDSL annotation processing
    annotationProcessor("com.querydsl:querydsl-apt:${libs.versions.querydsl.get()}:jakarta")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Retry
    implementation(libs.spring.retry)
    implementation("org.springframework.boot:spring-boot-starter-aop")

    // Batch
    implementation("org.springframework.boot:spring-boot-starter-batch")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.spring.modulith.test)
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation(libs.archunit)
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.batch:spring-batch-test")
    testRuntimeOnly(libs.h2)
}

tasks.test {
    useJUnitPlatform()
}
