package app.giftify.payment.domain.event;

import app.giftify.payment.domain.type.PaymentMethod;
import app.giftify.payment.domain.type.PaymentType;

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
