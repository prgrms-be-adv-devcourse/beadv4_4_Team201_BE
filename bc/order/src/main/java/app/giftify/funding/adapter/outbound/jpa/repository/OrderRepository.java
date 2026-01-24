package app.giftify.funding.adapter.outbound.jpa.repository;

import app.giftify.funding.adapter.outbound.jpa.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
}
