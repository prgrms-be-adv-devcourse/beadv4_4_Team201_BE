package app.giftify.settlement.adapter.outbound.persistence.jpa;

import app.giftify.settlement.domain.OrderSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOrderSnapshotRepository extends JpaRepository<OrderSnapshot, Long> {
}
