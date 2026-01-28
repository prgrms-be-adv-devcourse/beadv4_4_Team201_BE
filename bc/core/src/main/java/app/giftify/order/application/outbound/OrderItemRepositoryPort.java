package app.giftify.order.application.outbound;

import java.util.List;

import app.giftify.order.domain.OrderItem;

public interface OrderItemRepositoryPort {
    List<OrderItem> saveAll(List<OrderItem> orderItems);

    List<OrderItem> findByOrderId(Long orderId);

    void deleteAll(List<OrderItem> orderItems);
}
