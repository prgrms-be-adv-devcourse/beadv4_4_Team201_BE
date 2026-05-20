// Support Logging - 로깅 설정

dependencies {
    implementation(project(":support:common"))
    implementation(libs.spring.boot.starter)
    implementation(libs.logstash.logback.encoder)
}
