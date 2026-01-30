// Member 모듈

dependencies {
    // BC 모듈
    implementation(project(":bc:shared"))

    // Support 모듈
    implementation(project(":support:common"))
    implementation(project(":support:security"))
    implementation(project(":support:jpa"))
    implementation(project(":support:web"))

    // Spring Boot Starters
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.oauth2.resource.server)
    implementation(libs.spring.boot.starter.oauth2.client)

    // Auth Utils (JWT, Dotenv)
    implementation(libs.dotenv)
    implementation(libs.java.jwt)

    // Spring Modulith
    implementation(libs.spring.modulith.core)
    implementation(libs.spring.modulith.jdbc)

    // Lombok
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    // Test
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.modulith.test)
    testImplementation(libs.spring.security.test)
    testImplementation(libs.archunit)
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
}
