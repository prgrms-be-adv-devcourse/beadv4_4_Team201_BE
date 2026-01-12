  // Payment Adapter - 인프라 구현 (Web, DB)

  dependencies {
      // 같은 부모 모듈의 core, application 의존
      implementation(project(":bc:payment:core"))
      implementation(project(":bc:payment:application"))

      // 모든 인프라 기술 허용
      implementation("org.springframework.boot:spring-boot-starter-web")
      implementation("org.springframework.boot:spring-boot-starter-data-jpa")
      implementation("org.springframework.boot:spring-boot-starter-validation")
  }
