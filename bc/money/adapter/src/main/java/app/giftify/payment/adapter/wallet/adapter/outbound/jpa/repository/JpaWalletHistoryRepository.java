package app.giftify.payment.adapter.wallet.adapter.outbound.jpa.repository;

import app.giftify.payment.adapter.out.jpa.entity.JpaWalletHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaWalletHistoryRepository extends JpaRepository<JpaWalletHistory, Long> {

    boolean existsByReferenceIdAndReferenceType(Long referenceId, String referenceType);
}
