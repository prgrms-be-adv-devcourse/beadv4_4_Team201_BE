// Funding 모듈

dependencies {
    // (bc:shared는 부모(bc)에서 자동 추가됨)
    implementation(project(":support:common"))
    implementation(project(":support:jpa"))
    implementation(project(":support:web"))
    implementation(project(":support:security"))

    // 2. External Libraries (Spring Boot Starters)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.security)

    // 3. QueryDSL Implementation
    implementation("com.querydsl:querydsl-jpa:${libs.versions.querydsl.get()}:jakarta")

    // 4. Annotation Processors & CompileOnly (Lombok, QueryDSL APT)
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("com.querydsl:querydsl-apt:${libs.versions.querydsl.get()}:jakarta")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")

    // 5. Test Dependencies
    testRuntimeOnly("com.h2database:h2")
}
