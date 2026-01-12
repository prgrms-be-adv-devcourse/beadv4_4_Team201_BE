  package app.giftify.member.controller;

  import org.springframework.web.bind.annotation.GetMapping;
  import org.springframework.web.bind.annotation.RequestMapping;
  import org.springframework.web.bind.annotation.RestController;

  import java.time.LocalDateTime;
  import java.util.HashMap;
  import java.util.Map;

  /**
   * Member 모듈 Health Check Endpoint
   */
  @RestController
  @RequestMapping("/api/member")
  public class MemberHealthController {

      @GetMapping("/health")
      public Map<String, Object> health() {
          Map<String, Object> response = new HashMap<>();
          response.put("module", "member");
          response.put("status", "UP");
          response.put("timestamp", LocalDateTime.now().toString());
          return response;
      }
  }
