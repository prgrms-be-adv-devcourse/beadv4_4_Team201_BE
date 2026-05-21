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
    implementation(project(":support:common"))
    implementation(project(":support:jpa"))
    implementation(project(":support:security"))
    implementation(project(":support:web"))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework:spring-tx")

    implementation(libs.spring.modulith.core)
    implementation(libs.spring.modulith.jpa)

    implementation(libs.spring.retry)
    implementation(libs.spring.boot.starter.aspectj)

    implementation(libs.spring.boot.starter.batch.jdbc)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.webmvc.test)
    testImplementation(libs.spring.modulith.test)
    testImplementation(libs.spring.security.test)
    testImplementation("org.springframework.batch:spring-batch-test")
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
    testRuntimeOnly(libs.h2)
}

tasks.test {
    useJUnitPlatform()
}
