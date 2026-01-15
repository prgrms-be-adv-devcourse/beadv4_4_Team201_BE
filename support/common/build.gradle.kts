// Support Common - 공통 유틸리티

dependencies {
    implementation(project(":bc:shared"))
    // Minimal Spring
    implementation("org.springframework.boot:spring-boot-starter")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
