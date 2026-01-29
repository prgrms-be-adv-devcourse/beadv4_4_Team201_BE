package app.giftify.settlement.adapter.outbound.jpa.repository;

import app.giftify.settlement.domain.FundingSnapshot;
import app.giftify.settlement.domain.SettlementItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FundingSnapshotRepository extends JpaRepository<FundingSnapshot, Long> {
}
