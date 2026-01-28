// Support Web - 웹 유틸리티 (필터, 컨버터 등)

plugins {
    `java-library`
}

dependencies {
    implementation(project(":support:common"))
    implementation(libs.spring.boot.starter.web)
    api(libs.springdoc.openapi)
}
