val springModulithVersion = "1.3.1"

dependencies {
    implementation(project(":bc:shared"))
    implementation(project(":support:common"))
    implementation(project(":support:jpa"))
    implementation(project(":support:web"))
    implementation(project(":support:security"))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation("org.springframework.modulith:spring-modulith-starter-core:$springModulithVersion")
    implementation("org.springframework.modulith:spring-modulith-starter-jpa:$springModulithVersion")

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation("org.springframework.modulith:spring-modulith-starter-test:$springModulithVersion")
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
}
