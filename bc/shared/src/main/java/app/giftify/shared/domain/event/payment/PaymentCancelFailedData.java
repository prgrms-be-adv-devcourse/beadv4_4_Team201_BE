package app.giftify.shared.domain.event.payment;

import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;

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
