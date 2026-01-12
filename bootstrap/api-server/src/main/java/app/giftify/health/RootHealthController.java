  package app.giftify.health;

  import org.springframework.web.bind.annotation.GetMapping;
  import org.springframework.web.bind.annotation.RestController;

  import java.time.LocalDateTime;
  import java.util.HashMap;
  import java.util.List;
  import java.util.Map;

  /**
   * Bootstrap Application Root Health Check
   */
  @RestController
  public class RootHealthController {

      @GetMapping("/")
      public Map<String, Object> root() {
          Map<String, Object> response = new HashMap<>();
          response.put("service", "giftify-be");
          response.put("status", "UP");
          response.put("modules", List.of(
              Map.of("name", "member", "endpoint", "/api/member/health"),
              Map.of("name", "auth", "endpoint", "/api/auth/health"),
              Map.of("name", "payment", "endpoint", "/api/payment/health")
          ));
          response.put("timestamp", LocalDateTime.now().toString());
          return response;
      }

      @GetMapping("/health")
      public Map<String, String> health() {
          Map<String, String> response = new HashMap<>();
          response.put("status", "UP");
          return response;
      }
  }
