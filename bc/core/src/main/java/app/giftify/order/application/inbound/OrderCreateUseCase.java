package app.giftify.order.application.inbound;

import app.giftify.order.adapter.inbound.web.dto.request.OrderCreateRequest;
import app.giftify.order.adapter.inbound.web.dto.response.OrderResponse;

public interface OrderCreateUseCase {
    OrderResponse createOrder(OrderCreateRequest request);

    String confirmOrder(Long orderId, Long memberId);

    OrderResponse cancelOrder(Long orderId, Long memberId);
}
