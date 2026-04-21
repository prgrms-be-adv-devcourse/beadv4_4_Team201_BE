// Support Web - 웹 유틸리티 (필터, 컨버터 등)

plugins {
    `java-library`
}

dependencies {
    implementation(project(":support:common"))
    implementation(project(":support:security"))
    implementation(project(":bc:shared"))

    implementation(libs.spring.boot.starter.web)

    // Apache HttpClient 5 (HC5) - RestClient connection pooling
    implementation("org.apache.httpcomponents.client5:httpclient5")
    implementation("org.springframework.boot:spring-boot-restclient")

    // Retry
    implementation(libs.spring.retry)
    implementation("org.springframework.boot:spring-boot-starter-aspectj")

    // Redis
    implementation(libs.spring.boot.starter.data.redis)

    testImplementation(libs.archunit)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.data.redis.test)
    testImplementation(testFixtures(project(":support:common")))

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    api(libs.springdoc.openapi)
}
