package payment.usecase.command;

import domain.payment.CancelReason;

public record CancelPaymentCommand(
        Long paymentId,
        Long requesterId,
        CancelReason reason
) {
}
