package app.giftify.shared.domain.event.payment;

import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

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
