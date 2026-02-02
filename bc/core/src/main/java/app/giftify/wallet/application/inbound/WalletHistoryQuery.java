package app.giftify.wallet.application.inbound;

import app.giftify.wallet.domain.TransactionType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record WalletHistoryQuery(
	Long memberId,
	TransactionType type,
	int page,
	int size
) {
	public WalletHistoryQuery {
		if (page < 0) {
			throw new IllegalArgumentException("page must be >= 0");
		}
		if (size <= 0 || size > 100) {
			throw new IllegalArgumentException("size must be between 1 and 100");
		}
	}

	public Pageable toPageable() {
		return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "occurredAt"));
	}
}
