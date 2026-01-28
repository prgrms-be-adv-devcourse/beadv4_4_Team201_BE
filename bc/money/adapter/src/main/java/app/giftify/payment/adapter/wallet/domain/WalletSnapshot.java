package app.giftify.payment.adapter.wallet.domain;

import app.giftify.shared.domain.vo.Money;

public record WalletSnapshot(
        Long id,
        Long memberId,
        Money balance
) {
}
