plugins {
    `java-library`
}

dependencies {

}

  dependencies {
      compileOnly("org.projectlombok:lombok")
      annotationProcessor("org.projectlombok:lombok")
      // EventPublisher 및 공통 기능을 위한 의존성
      implementation("org.springframework.boot:spring-boot-starter")
  }
