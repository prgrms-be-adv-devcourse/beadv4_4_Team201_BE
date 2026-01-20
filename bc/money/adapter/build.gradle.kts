// money Adapter - 인프라 구현 (Web, DB)

dependencies {
    implementation(project(":support:jpa"))
    // 같은 부모 모듈의 core, application 의존
    implementation(project(":bc:money:core"))
    implementation(project(":bc:money:application"))

    // 인프라 기술 허용
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // 테스트 시에도 Lombok 어노테이션 처리
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
}
