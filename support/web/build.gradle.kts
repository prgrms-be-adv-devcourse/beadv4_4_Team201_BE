// Support Web - 웹 유틸리티 (필터, 컨버터 등)

plugins {
    `java-library`
}

dependencies {
    implementation(project(":support:common"))
    implementation(project(":support:security"))
    implementation(project(":bc:shared"))

    implementation(libs.spring.boot.starter.web)

    // Retry
    implementation(libs.spring.retry)
    implementation("org.springframework.boot:spring-boot-starter-aop")

    // Redis
    implementation(libs.spring.boot.starter.data.redis)

    testImplementation(libs.archunit)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(testFixtures(project(":support:common")))

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    api(libs.springdoc.openapi)
}
