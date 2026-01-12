  // Auth 모듈 - Vertical Slice Architecture

  dependencies {
      // bc:shared는 부모(bc)에서 자동 추가됨

      implementation("org.springframework.boot:spring-boot-starter-web")
      implementation("org.springframework.boot:spring-boot-starter-data-jpa")
      implementation("org.springframework.boot:spring-boot-starter-validation")

      // Security는 나중에 필요할 때 활성화
      // implementation("org.springframework.boot:spring-boot-starter-security")
  }
