  // Member 모듈 - Pragmatic Layered Architecture

  dependencies {
      // bc:shared는 부모(bc)에서 자동 추가됨
      implementation(project(":support:security"))
      implementation(project(":support:common"))
      implementation(project(":support:jpa"))
      implementation(project(":bc:shared"))

      implementation("org.springframework.boot:spring-boot-starter-web")
      implementation("org.springframework.boot:spring-boot-starter-data-jpa")
      implementation("org.springframework.boot:spring-boot-starter-validation")

      implementation("org.springframework.boot:spring-boot-starter-security")
      implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

      compileOnly("org.projectlombok:lombok")
      annotationProcessor("org.projectlombok:lombok")
      testCompileOnly("org.projectlombok:lombok")
      testAnnotationProcessor("org.projectlombok:lombok")

      testImplementation("org.springframework.boot:spring-boot-starter-test")
      testImplementation("org.springframework.security:spring-security-test")
  }
