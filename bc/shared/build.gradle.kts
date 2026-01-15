  // BC Shared Kernel - 모든 BC 모듈이 공유하는 도메인 개념
  // 순수 Java, 외부 의존성 최소화

  dependencies {
      compileOnly("org.projectlombok:lombok")
      annotationProcessor("org.projectlombok:lombok")
      // EventPublisher 및 공통 기능을 위한 의존성
      implementation("org.springframework.boot:spring-boot-starter")
  }
