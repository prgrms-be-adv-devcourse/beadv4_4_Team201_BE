// Shared 모듈 - 도메인 공통 타입, 이벤트, VO

plugins {
    `java-library`
}

dependencies {
    compileOnly("org.springframework.modulith:spring-modulith-api:${libs.versions.springModulith.get()}")

    implementation(libs.spring.modulith.events.api)
    implementation(libs.jmolecules.events)
    implementation("com.fasterxml.jackson.core:jackson-annotations")
    // Test
    testImplementation(libs.spring.boot.starter.test)
}
