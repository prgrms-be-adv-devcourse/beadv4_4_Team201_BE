package app.giftify.settlement.adapter.outbound.jpa.repository;

import app.giftify.settlement.domain.OrderItemSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderItemSnapshotRepository extends JpaRepository<OrderItemSnapshot, Long> {
    Optional<OrderItemSnapshot> findByFundingId(Long fundingId);
}
