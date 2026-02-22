// Shared 모듈 - 도메인 공통 타입, 이벤트, VO

plugins {
    `java-library`
}

dependencies {
    implementation(libs.spring.modulith.events.api)
    implementation("com.fasterxml.jackson.core:jackson-annotations")

    // Test
    testImplementation(libs.spring.boot.starter.test)
}
