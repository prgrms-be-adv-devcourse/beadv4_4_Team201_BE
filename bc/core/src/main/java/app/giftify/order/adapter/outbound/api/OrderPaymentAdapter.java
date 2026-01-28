package app.giftify.order.adapter.outbound.api;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import app.giftify.order.application.outbound.OrderPaymentPort;
import app.giftify.order.domain.Order;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderPaymentAdapter implements OrderPaymentPort {
    
    private final RestClient restClient;

    public OrderPaymentAdapter(
            @Value("${custom.global.internalBackUrl:http://localhost:8080}") String internalBackUrl) {
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

    @Override
    public void cancelPayment(String orderNumber) {
        Map<String, Object> request = Map.of(
                "orderNumber", orderNumber
        );

        try {
            log.info("결제 취소 및 환불 요청을 보냅니다. [주문번호: {}]", orderNumber);

            restClient.post()
                    .uri("/payments/cancel")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

            log.info("결제 취소 요청이 성공적으로 전송되었습니다.");
        } catch (Exception e) {
            log.error("결제 취소 요청 중 오류가 발생했습니다. [주문번호: {}]", orderNumber, e);
        }
    }
}
