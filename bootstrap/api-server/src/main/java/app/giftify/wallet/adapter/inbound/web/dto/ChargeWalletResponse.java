package app.giftify.wallet.adapter.inbound.web.dto;

import app.giftify.wallet.application.inbound.ChargeWalletResult;

import java.math.BigDecimal;

public record ChargeWalletResponse(
    Long walletId,
    BigDecimal balance,
    BigDecimal chargedAmount,
    String transactionId
) {
    public static ChargeWalletResponse from(ChargeWalletResult result) {
        return new ChargeWalletResponse(
            result.walletId(),
            result.balanceAfter().amount(),
            result.chargedAmount().amount(),
            result.transactionId()
        );
    }
}
