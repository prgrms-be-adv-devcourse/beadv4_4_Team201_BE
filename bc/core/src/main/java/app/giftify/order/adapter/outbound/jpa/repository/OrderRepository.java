package app.giftify.order.adapter.outbound.jpa.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import app.giftify.order.adapter.outbound.jpa.entity.OrderEntity;
import app.giftify.order.domain.OrderStatus;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime threshold);

    Optional<OrderEntity> findByIdAndBuyerId(Long orderId, Long memberId);
}
