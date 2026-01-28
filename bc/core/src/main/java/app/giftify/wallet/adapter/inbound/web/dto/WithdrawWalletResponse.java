package app.giftify.wallet.adapter.inbound.web.dto;

import app.giftify.wallet.application.inbound.WithdrawWalletResult;

import java.math.BigDecimal;

public record WithdrawWalletResponse(
    Long walletId,
    BigDecimal balance,
    BigDecimal withdrawnAmount,
    String transactionId,
    String status
) {
    public static WithdrawWalletResponse from(WithdrawWalletResult result) {
        return new WithdrawWalletResponse(
            result.walletId(),
            result.balanceAfter().amount(),
            result.withdrawnAmount().amount(),
            result.transactionId(),
            result.status().name()
        );
    }
}
