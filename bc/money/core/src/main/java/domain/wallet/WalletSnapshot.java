package domain.wallet;

import app.giftify.shared.domain.vo.Money;

public record WalletSnapshot(
        Long id,
        Long memberId,
        Money balance
) {
}
