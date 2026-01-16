// Support Security - 보안 유틸리티

dependencies {
    implementation(project(":support:common"))
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.oauth2.resource.server)
}
