  package app.giftify.auth.common;

  import org.springframework.web.bind.annotation.GetMapping;
  import org.springframework.web.bind.annotation.RequestMapping;
  import org.springframework.web.bind.annotation.RestController;

  import java.time.LocalDateTime;
  import java.util.HashMap;
  import java.util.Map;

  /**
   * Auth 모듈 Health Check Endpoint
   * Vertical Slice에서 공통 기능은 common 패키지에 위치
   */
  @RestController
  @RequestMapping("/api/auth")
  public class AuthHealthController {

      @GetMapping("/health")
      public Map<String, Object> health() {
          Map<String, Object> response = new HashMap<>();
          response.put("module", "auth");
          response.put("status", "UP");
          response.put("timestamp", LocalDateTime.now().toString());
          return response;
      }
  }
