package app.giftify.wallet.adapter.inbound.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record WithdrawWalletRequest(
    @NotNull @Positive BigDecimal amount,
    @NotBlank String bankCode,
    @NotBlank String accountNumber
) {}
