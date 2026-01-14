package app.giftify;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

  @SpringBootApplication
  public class GiftifyApplication {

      public static void main(String[] args) {
          loadEnvironmentVariables();

          SpringApplication.run(GiftifyApplication.class, args);
      }

      // 환경 변수 로딩 시점 개선 및 모듈별 의존성 최적화
      // .env를 우선 로드하여 주입 에러 해결
      private static void loadEnvironmentVariables() {
          Dotenv dotenv = Dotenv.configure()
                  .directory(System.getProperty("user.dir"))
                  .ignoreIfMissing()
                  .load();

          dotenv.entries().forEach(entry ->
                  System.setProperty(entry.getKey(), entry.getValue())
          );
      }
  }
