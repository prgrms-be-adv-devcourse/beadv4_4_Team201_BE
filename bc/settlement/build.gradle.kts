dependencies {
    // Support 모듈
    implementation(project(":support:common"))
//    implementation(project(":support:security"))
    implementation(project(":support:jpa"))
    implementation(project(":bc:shared"))

    // Spring Boot Starters
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)

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
