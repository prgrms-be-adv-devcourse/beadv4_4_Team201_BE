package app.giftify.orderDemo.application.outbound.port;

import app.giftify.orderDemo.domain.OrderItem;
import app.giftify.orderDemo.domain.OrderItemStatus;

import java.util.List;

// todo: get -> find 접두사 수정
public interface OrderItemRepository {
    OrderItem save(OrderItem orderItem);

    OrderItem getOrderItemById(Long id);

    List<OrderItem> getCancelableItemsByOrderId(Long orderId);

    List<OrderItem> getPendingCancelItemsByOrderId(Long orderId);

    List<OrderItem> getAllByOrderIdAndIdIn(Long orderId, List<Long> itemIds);

    List<OrderItemStatus> getStatusesByOrderId(Long orderId);

    void flush();
}
