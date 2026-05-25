package app.giftify.wallet.application.inbound;

import app.giftify.support.common.money.Money;
import app.giftify.wallet.domain.ReferenceType;
import app.giftify.wallet.domain.TransactionType;

public record RestoreWalletCommand(
        Long memberId,
        Money amount,
        String referenceId,
        TransactionType transactionType,
        ReferenceType referenceType
) {
}
