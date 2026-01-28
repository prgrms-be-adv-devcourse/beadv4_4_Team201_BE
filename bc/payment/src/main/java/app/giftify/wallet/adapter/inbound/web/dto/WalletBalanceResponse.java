package app.giftify.wallet.adapter.inbound.web.dto;

import app.giftify.wallet.application.inbound.WalletBalanceResult;

import java.math.BigDecimal;

public record WalletBalanceResponse(
    Long walletId,
    BigDecimal balance
) {
    public static WalletBalanceResponse from(WalletBalanceResult result) {
        return new WalletBalanceResponse(
            result.walletId(),
            result.balance().amount()
        );
    }
}
