package app.giftify.order.adapter.outbound.persistence.jpa;

import app.giftify.order.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOrderItemRepository extends JpaRepository<OrderItem, Long> {
}
