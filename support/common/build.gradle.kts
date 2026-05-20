// Support Common - 공통 유틸리티
plugins {
    `java-test-fixtures` // 또는 id("java-test-fixtures")
}

dependencies {
    implementation(project(":bc:shared"))
    // Minimal Spring
    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.boot.starter.cache)
    implementation(libs.spring.boot.jackson2)

    testImplementation(libs.archunit)
    testImplementation(libs.spring.boot.starter.test)

    testFixturesImplementation(libs.testcontainers.redis)
    testFixturesImplementation(libs.testcontainers.junit)
    testFixturesImplementation(libs.spring.boot.starter.test)

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
