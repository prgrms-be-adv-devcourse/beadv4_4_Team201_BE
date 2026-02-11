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
    implementation(project(":support:jpa"))
    implementation(project(":support:security"))
    implementation(project(":support:web"))
    implementation(project(":bc:shared"))

    // Spring Boot Starters
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.security)
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework:spring-tx")

    // QueryDSL
    implementation("com.querydsl:querydsl-jpa:${libs.versions.querydsl.get()}:jakarta")
    annotationProcessor("com.querydsl:querydsl-apt:${libs.versions.querydsl.get()}:jakarta")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Apache Commons
    implementation("org.apache.commons:commons-lang3:3.14.0")

    // Test
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.security.test)
    testImplementation(libs.archunit)
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
    testRuntimeOnly(libs.h2)

    // Retry
    implementation("org.springframework.retry:spring-retry")
    implementation("org.springframework.boot:spring-boot-starter-aop")
}

tasks.test {
    useJUnitPlatform()
}
