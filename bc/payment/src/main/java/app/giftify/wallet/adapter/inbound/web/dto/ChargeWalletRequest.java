package app.giftify.wallet.adapter.inbound.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ChargeWalletRequest(
    @NotBlank String paymentKey,
    @NotBlank String chargeOrderId,
    @NotNull @Positive BigDecimal amount
) {}
