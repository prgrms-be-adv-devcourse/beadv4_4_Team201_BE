dependencies {
    implementation(project(":bc:shared"))
    implementation(project(":support:common"))
    implementation(project(":support:jpa"))
    implementation(project(":support:web"))
    implementation(project(":support:security"))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.modulith.core)
    implementation(libs.spring.modulith.jpa)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.modulith.test)
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
}
