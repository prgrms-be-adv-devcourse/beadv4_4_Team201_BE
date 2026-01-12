package app.giftify.payment.adapter.in.web;

  import org.springframework.web.bind.annotation.GetMapping;
  import org.springframework.web.bind.annotation.RequestMapping;
  import org.springframework.web.bind.annotation.RestController;

  import java.time.LocalDateTime;
  import java.util.HashMap;
  import java.util.Map;

  /**
   * Payment 모듈 Health Check Endpoint
   * Hexagonal Architecture - Inbound Web Adapter
   */
  @RestController
  @RequestMapping("/api/payment")
  public class PaymentHealthController {

      @GetMapping("/health")
      public Map<String, Object> health() {
          Map<String, Object> response = new HashMap<>();
          response.put("module", "payment");
          response.put("status", "UP");
          response.put("layers", Map.of(
              "core", "Domain Logic",
              "application", "Use Cases",
              "adapter", "Infrastructure"
          ));
          response.put("timestamp", LocalDateTime.now().toString());
          return response;
      }
  }
