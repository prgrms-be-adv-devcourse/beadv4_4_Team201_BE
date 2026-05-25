// Support Security - 보안 유틸리티

dependencies {
    compileOnly("org.springframework.modulith:spring-modulith-api:${libs.versions.springModulith.get()}")

    implementation(project(":support:common"))
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.oauth2.resource.server)

    // Test
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.security.test)
}
