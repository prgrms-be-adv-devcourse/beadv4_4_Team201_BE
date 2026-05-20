package app.giftify.shared.domain.event.payment;

import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;

public sealed interface PaymentEventData
        permits PaymentSuccessData, PaymentFailureData,
        PaymentCancelData, PaymentCancelFailedData {

    Long paymentId();
    Long orderId();
    Long memberId();
    String orderNumber();
    PaymentMethod paymentMethod();
    PaymentType paymentType();
}
