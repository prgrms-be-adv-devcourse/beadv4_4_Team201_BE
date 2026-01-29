package app.giftify.wallet.adapter.outbound.jpa;

import app.giftify.wallet.adapter.outbound.jpa.entity.JpaWalletHistory;
import app.giftify.wallet.application.outbound.WalletHistoryRepository;
import app.giftify.wallet.domain.ReferenceType;
import app.giftify.wallet.domain.WalletHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WalletHistoryRepositoryAdapter implements WalletHistoryRepository {

	private final JpaWalletHistoryRepository jpaWalletHistoryRepository;

	@Override
	public void record(WalletHistory history) {
		JpaWalletHistory entity = JpaWalletHistory.from(history);
		jpaWalletHistoryRepository.save(entity);
	}

	@Override
	public boolean existsByReferenceIdAndReferenceType(String referenceId, ReferenceType type) {
		return jpaWalletHistoryRepository.existsByReferenceIdAndReferenceType(
			referenceId, type.name());
	}
}
