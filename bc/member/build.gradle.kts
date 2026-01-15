  // Member 모듈 - Pragmatic Layered Architecture

  dependencies {
      // bc:shared는 부모(bc)에서 자동 추가됨
      implementation(project(":support:security")) // 다른 모듈에도 추가해야 사용 가능.

      implementation("org.springframework.boot:spring-boot-starter-web")
      implementation("org.springframework.boot:spring-boot-starter-data-jpa")
      implementation("org.springframework.boot:spring-boot-starter-validation")

      compileOnly("org.projectlombok:lombok")
      annotationProcessor("org.projectlombok:lombok")
      testCompileOnly("org.projectlombok:lombok")
      testAnnotationProcessor("org.projectlombok:lombok")
  }
