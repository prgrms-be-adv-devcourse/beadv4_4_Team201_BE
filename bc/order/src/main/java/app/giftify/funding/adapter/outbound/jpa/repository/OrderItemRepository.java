package app.giftify.funding.adapter.outbound.jpa.repository;

import app.giftify.funding.adapter.outbound.jpa.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {
    List<OrderItemEntity> findByOrderId(Long orderId);
}
