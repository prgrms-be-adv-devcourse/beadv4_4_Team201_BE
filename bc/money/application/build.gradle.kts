  // money Application - 유스케이스 오케스트레이션

  dependencies {
      // 같은 부모 모듈의 core 의존 (경로 수정!)
      implementation(project(":bc:money:core"))

      // 최소한의 Spring만 허용
      implementation(libs.spring.boot.starter)
      implementation("org.springframework:spring-tx") // 이건 보통 starter-data-jpa 등에 포함되나 명시 필요시 유지. libs엔 없으니 일단 유지하거나 toml에 추가.
      
      compileOnly("org.projectlombok:lombok")
      annotationProcessor("org.projectlombok:lombok")
      testCompileOnly("org.projectlombok:lombok")
      testAnnotationProcessor("org.projectlombok:lombok")
  }
