package app.giftify.funding.application.outbound;
import app.giftify.funding.domain.Order;
import app.giftify.funding.domain.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepositoryPort {
    Order save(Order order);

    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime threshold);

    Optional<Order> findById(Long id);

    void delete(Order order);

    Optional<Order> findByIdAndBuyerId(Long orderId, Long memberId);
}
