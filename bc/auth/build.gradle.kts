  // Auth 모듈

  dependencies {
      // 1. Internal Project Dependencies
      // (bc:shared는 부모(bc)에서 자동 추가됨)

      // 2. External Libraries (Spring Boot Starters)
      implementation(libs.spring.boot.starter.web)
      implementation(libs.spring.boot.starter.data.jpa)
      implementation(libs.spring.boot.starter.validation)
      implementation(project(":support:security"))

      implementation("org.springframework.boot:spring-boot-starter-web")
      implementation("org.springframework.boot:spring-boot-starter-data-jpa")
      implementation("org.springframework.boot:spring-boot-starter-validation")

      // 3. Security & OAuth2
      implementation(libs.spring.boot.starter.security)
      implementation(libs.spring.boot.starter.oauth2.client)
      implementation(libs.spring.boot.starter.oauth2.resource.server)

      // 4. Utils (JWT, Dotenv)
      implementation(libs.dotenv.java)
      implementation(libs.java.jwt)

      // 5. Annotation Processors & CompileOnly
      compileOnly("org.projectlombok:lombok")
      annotationProcessor("org.projectlombok:lombok")
      testCompileOnly("org.projectlombok:lombok")
      testAnnotationProcessor("org.projectlombok:lombok")
  }
