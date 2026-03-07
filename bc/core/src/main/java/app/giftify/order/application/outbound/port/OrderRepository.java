package app.giftify.order.application.outbound.port;

import app.giftify.order.domain.Order;
import app.giftify.order.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository {
    Order save(Order order);

    Page<Order> getByBuyerId(Long buyerId, Pageable pageable);

    Order getById(Long orderId);

    Order getByOrderNumber(String orderNumber);

    Order getByIdWithItemsAndLock(Long id);

    List<Order> getAllByIdInWithItems(List<Long> ids);

    List<Long> getIdsByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime threshold);

    Order getByIdWithItems(Long id);
}
