package app.giftify.wallet.adapter.inbound.web.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ChargeWalletRequest(
    @NotBlank String chargeOrderId,
    @NotNull @Positive BigDecimal amount
) {}
