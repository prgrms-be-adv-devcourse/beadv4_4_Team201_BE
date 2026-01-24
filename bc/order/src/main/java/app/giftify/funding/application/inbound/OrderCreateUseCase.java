package app.giftify.funding.application.inbound;

import app.giftify.funding.adapter.inbound.web.dto.OrderCreateRequest;
import app.giftify.funding.adapter.inbound.web.dto.OrderResponse;

public interface OrderCreateUseCase {
    OrderResponse createOrder(OrderCreateRequest request);
}
