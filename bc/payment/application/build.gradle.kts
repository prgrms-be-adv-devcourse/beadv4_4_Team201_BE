  // Payment Application - 유스케이스 오케스트레이션

  dependencies {
      // 같은 부모 모듈의 core 의존 (경로 수정!)
      implementation(project(":bc:payment:core"))

      // 최소한의 Spring만 허용
      implementation("org.springframework.boot:spring-boot-starter")
      implementation("org.springframework:spring-tx")
  }
