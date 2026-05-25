package app.giftify.order.adapter.inbound.web.dto.request;

import app.giftify.payment.domain.type.PaymentMethod;
import app.giftify.support.common.money.Money;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PlaceOrderRequest(
        @NotNull
        @Size(min = 1)
        List<PlaceOrderItemRequest> items,
        @NotNull
        PaymentMethod method,
        Money walletDeductAmount
) {
    public Money walletDeductAmount() {
        return walletDeductAmount != null ? walletDeductAmount : Money.zero();
    }
}
