  // money Application - 유스케이스 오케스트레이션

  dependencies {
      // 같은 부모 모듈의 core 의존 (경로 수정!)
      implementation(project(":bc:money:core"))

      // 최소한의 Spring만 허용
      implementation("org.springframework.boot:spring-boot-starter")
      implementation("org.springframework:spring-tx")
      implementation("org.projectlombok:lombok")

      // 테스트 시에도 Lombok 어노테이션 처리
      testImplementation("org.projectlombok:lombok")
      testAnnotationProcessor("org.projectlombok:lombok")
      annotationProcessor("org.projectlombok:lombok")
  }
