package app.giftify.payment.domain.event;

import app.giftify.payment.domain.type.PaymentMethod;
import app.giftify.payment.domain.type.PaymentType;
import app.giftify.support.common.money.Money;

public record PaymentSuccessData(
        Long paymentId,
        Long orderId,
        Long memberId,
        String orderNumber,
        Money paidAmount,
        PaymentMethod paymentMethod,
        PaymentType paymentType,
        String paymentKey,
        String transactionKey
) implements PaymentEventData {
}
