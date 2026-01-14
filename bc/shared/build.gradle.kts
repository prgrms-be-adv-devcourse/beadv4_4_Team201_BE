plugins {
    `java-library`
}

dependencies {
    // Lombok 제거됨 (Pure Java)
}

  dependencies {
      compileOnly("org.projectlombok:lombok")
      annotationProcessor("org.projectlombok:lombok")
      // EventPublisher 및 공통 기능을 위한 의존성
      implementation("org.springframework.boot:spring-boot-starter")
  }
