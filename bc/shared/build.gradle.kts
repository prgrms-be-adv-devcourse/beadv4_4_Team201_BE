// BC Shared Kernel - 모든 BC 모듈이 공유하는 도메인 개념
  // 순수 Java, 외부 의존성 최소화

  dependencies {
      // JPA 의존성 (BaseEntity를 위해 필요)
      implementation("org.springframework.boot:spring-boot-starter-data-jpa")
      
      compileOnly("org.projectlombok:lombok")
      annotationProcessor("org.projectlombok:lombok")
  }