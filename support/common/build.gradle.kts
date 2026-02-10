// Support Common - 공통 유틸리티

dependencies {
    implementation(project(":bc:shared"))
    // Minimal Spring
    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.data.redis)

    // Retry
    implementation("org.springframework.retry:spring-retry:2.0.2")
    implementation("org.springframework.boot:spring-boot-starter-aop")

    testImplementation(libs.archunit)
    testImplementation(libs.spring.boot.starter.test)

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
