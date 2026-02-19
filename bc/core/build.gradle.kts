plugins {
    id("java")
}

group = "app.giftify"
version = "1.0.0-SNAPSHOT"

val springModulithVersion = "1.3.1"

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

    // Spring Modulith (버전 명시)
    implementation("org.springframework.modulith:spring-modulith-starter-core:$springModulithVersion")
    implementation("org.springframework.modulith:spring-modulith-starter-jdbc:$springModulithVersion")

    // API Documentation
    implementation(libs.springdoc.openapi)

    // QueryDSL annotation processing
    annotationProcessor("com.querydsl:querydsl-apt:${libs.versions.querydsl.get()}:jakarta")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.modulith:spring-modulith-starter-test:$springModulithVersion")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.3.0")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.test {
    useJUnitPlatform()
}
