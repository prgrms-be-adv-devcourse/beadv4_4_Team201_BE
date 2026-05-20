package app.giftify.shared.domain.event.payment;

import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

public record PaymentFailureData(
        Long paymentId,
        Long orderId,
        Long memberId,
        String orderNumber,
        Money paidAmount,
        Money walletDeductedAmount,
        PaymentMethod paymentMethod,
        PaymentType paymentType
) implements PaymentEventData {}

