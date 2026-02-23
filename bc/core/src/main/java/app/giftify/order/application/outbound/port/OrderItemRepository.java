package app.giftify.order.application.outbound.port;

import app.giftify.order.domain.OrderItem;

import java.util.List;

// todo: get -> find 접두사 수정
public interface OrderItemRepository {
    OrderItem save(OrderItem orderItem);

    OrderItem getOrderItemById(Long id);

    List<OrderItem> getOrderItemsWithOrderAndFindingId(Long findingId);
}
