package app.giftify.shared.domain.event.payment;

import app.giftify.shared.domain.type.CancelType;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

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
