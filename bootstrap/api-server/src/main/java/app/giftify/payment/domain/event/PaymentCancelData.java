package app.giftify.payment.domain.event;

import app.giftify.payment.domain.type.CancelType;
import app.giftify.payment.domain.type.PaymentMethod;
import app.giftify.payment.domain.type.PaymentType;
import app.giftify.support.common.money.Money;

public record PaymentCancelData(
        Long paymentId,
        Long orderId,
        Long memberId,
        String orderNumber,
        Money cancelAmount,
        Money walletDeductedAmount,
        PaymentMethod paymentMethod,
        PaymentType paymentType,
        CancelType cancelType,
        String reason,
        String transactionKey
) implements PaymentEventData {
}
