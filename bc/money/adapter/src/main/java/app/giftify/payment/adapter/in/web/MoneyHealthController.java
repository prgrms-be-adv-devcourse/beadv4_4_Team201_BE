package app.giftify.payment.adapter.in.web;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

  @RestController
  @RequestMapping("/api/money")
  public class MoneyHealthController {

      @GetMapping("/health")
      public Map<String, Object> health() {
          Map<String, Object> response = new HashMap<>();
          response.put("module", "money");
          response.put("status", "UP");
          response.put("timestamp", LocalDateTime.now().toString());
          return response;
      }
  }
