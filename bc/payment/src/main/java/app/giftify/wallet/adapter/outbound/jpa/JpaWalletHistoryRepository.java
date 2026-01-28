package app.giftify.wallet.adapter.outbound.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import app.giftify.wallet.adapter.outbound.jpa.entity.JpaWalletHistory;

public interface JpaWalletHistoryRepository extends JpaRepository<JpaWalletHistory, Long> {
    boolean existsByReferenceIdAndReferenceType(String referenceId, String referenceType);
}
