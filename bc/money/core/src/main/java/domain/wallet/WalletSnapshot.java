package domain.wallet;

import app.giftify.shared.domain.vo.Money;

import java.time.LocalDateTime;

public record WalletSnapshot(
        Long id,
        Long memberId,
        Money balance,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
