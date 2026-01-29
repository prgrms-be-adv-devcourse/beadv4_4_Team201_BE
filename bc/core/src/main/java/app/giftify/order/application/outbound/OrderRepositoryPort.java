package app.giftify.order.application.outbound;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import app.giftify.order.domain.Order;
import app.giftify.order.domain.OrderStatus;

public interface OrderRepositoryPort {
    Order save(Order order);

    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime threshold);

    Optional<Order> findById(Long id);

    void delete(Order order);

    Optional<Order> findByIdAndBuyerId(Long orderId, Long memberId);
}
