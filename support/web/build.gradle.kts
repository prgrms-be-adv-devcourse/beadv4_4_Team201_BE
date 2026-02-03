// Support Web - 웹 유틸리티 (필터, 컨버터 등)

plugins {
    `java-library`
}

dependencies {
    implementation(project(":support:common"))
    implementation(project(":bc:shared"))

    implementation(libs.spring.boot.starter.web)

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    api(libs.springdoc.openapi)
}
