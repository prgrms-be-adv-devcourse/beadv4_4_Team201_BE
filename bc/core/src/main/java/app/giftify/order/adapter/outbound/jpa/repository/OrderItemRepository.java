package app.giftify.order.adapter.outbound.jpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import app.giftify.order.adapter.outbound.jpa.entity.OrderItemEntity;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {
    List<OrderItemEntity> findByOrderId(Long orderId);
}
