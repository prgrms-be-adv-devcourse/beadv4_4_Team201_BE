  // Auth 모듈 - Vertical Slice Architecture

  dependencies {
      // bc:shared는 부모(bc)에서 자동 추가됨

      implementation("org.springframework.boot:spring-boot-starter-web")
      implementation("org.springframework.boot:spring-boot-starter-data-jpa")
      implementation("org.springframework.boot:spring-boot-starter-validation")

      implementation("io.github.cdimascio:dotenv-java:3.0.0")

      implementation("com.auth0:java-jwt:4.4.0")

      implementation("org.springframework.boot:spring-boot-starter-security")
      implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
      implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  }
