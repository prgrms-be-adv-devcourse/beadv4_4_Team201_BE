package app.giftify.funding.adapter.outbound.jpa.repository;

import app.giftify.funding.adapter.outbound.jpa.entity.OrderEntity;
import app.giftify.funding.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime threshold);

    Optional<OrderEntity> findByIdAndBuyerId(Long orderId, Long memberId);
}
