package app.giftify.order.adapter.in.web.dto.response;

import app.giftify.order.domain.domain.Order;
import lombok.Builder;
import lombok.Getter;

// 주문 응답 DTO
@Getter
@Builder
public class OrderResponse {
    private Long orderId;
    private String orderNumber;
    private String status;
    private Long totalAmount;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount().amount().longValue())
                .build();
    }
}
