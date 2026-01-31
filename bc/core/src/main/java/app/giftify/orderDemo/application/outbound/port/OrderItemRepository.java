package app.giftify.orderDemo.application.outbound.port;

import app.giftify.orderDemo.domain.OrderItem;

public interface OrderItemRepository {
    OrderItem save(OrderItem orderItem);
}
