package app.giftify.funding.adapter.outbound.api;

import app.giftify.funding.application.outbound.OrderPaymentPort;
import app.giftify.funding.domain.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Component
public class OrderPaymentAdapter implements OrderPaymentPort {
    
    private final RestClient restClient;

    public OrderPaymentAdapter(
            @Value("${custom.global.internalBackUrl}") String internalBackUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(internalBackUrl + "/api")
                .build();
    }

    @Override
    public void initiatePayment(Order order) {
        // PaymentInitiateRequest 형식에 맞게 데이터 생성
        Map<String, Object> request = Map.of(
                "orderId", order.getId(),
                "amount", order.getTotalAmount().amount()
        );

        try {
            log.info("결제 요청을 보냅니다. [주문 ID: {}, 금액: {}]", order.getId(), order.getTotalAmount().amount());
            
            restClient.post()
                    .uri("/payments/initiate")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            
            log.info("결제 요청이 성공적으로 전송되었습니다.");
        } catch (Exception e) {
            log.error("결제 요청 중 오류가 발생했습니다. [주문 ID: {}]", order.getId(), e);
        }
    }
}
