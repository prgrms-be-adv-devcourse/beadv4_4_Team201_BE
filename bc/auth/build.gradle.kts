// Auth 모듈 - Vertical Slice Architecture

dependencies {
    // 1. Internal Project Dependencies
    implementation(project(":bc:shared"))
    implementation(project(":support:common"))
    implementation(project(":support:security"))

    // 2. External Libraries (Spring Boot Starters)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)

    // 3. Security & OAuth2
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.oauth2.client)
    implementation(libs.spring.boot.starter.oauth2.resource.server)

    // 4. Utils (JWT, Dotenv)
    implementation(libs.dotenv)
    implementation(libs.java.jwt)

    // 5. Annotation Processors & CompileOnly
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    // 6. Test
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.security.test)
}
