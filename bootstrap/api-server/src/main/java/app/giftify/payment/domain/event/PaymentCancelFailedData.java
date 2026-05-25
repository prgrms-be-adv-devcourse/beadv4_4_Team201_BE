package app.giftify.payment.domain.event;

import app.giftify.payment.domain.type.PaymentMethod;
import app.giftify.payment.domain.type.PaymentType;

public record PaymentCancelFailedData(
        Long paymentId,
        Long orderId,
        Long memberId,
        String orderNumber,
        PaymentMethod paymentMethod,
        PaymentType paymentType,
        String errorMetadata
) implements PaymentEventData {
}
