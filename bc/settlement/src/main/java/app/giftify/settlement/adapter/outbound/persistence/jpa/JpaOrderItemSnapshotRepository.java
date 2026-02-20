package app.giftify.settlement.adapter.outbound.persistence.jpa;

import app.giftify.settlement.domain.snapshot.OrderItemSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaOrderItemSnapshotRepository extends JpaRepository<OrderItemSnapshot, Long> {
    Optional<OrderItemSnapshot> findByTargetId(Long targetId);
}
