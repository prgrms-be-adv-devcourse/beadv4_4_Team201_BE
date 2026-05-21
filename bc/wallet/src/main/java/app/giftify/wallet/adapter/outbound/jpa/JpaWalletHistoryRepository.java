package app.giftify.wallet.adapter.outbound.jpa;

import app.giftify.wallet.adapter.outbound.jpa.entity.JpaWalletHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaWalletHistoryRepository extends JpaRepository<JpaWalletHistory, Long> {
	boolean existsByReferenceIdAndReferenceType(String referenceId, String referenceType);

	Page<JpaWalletHistory> findByWalletId(Long walletId, Pageable pageable);

	Page<JpaWalletHistory> findByWalletIdAndTransactionType(Long walletId, String transactionType, Pageable pageable);
}
