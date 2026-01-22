package app.giftify.order.domain.exception;

public class PaymentAlreadyCompletedException extends OrderDomainException {

    public PaymentAlreadyCompletedException() {
        super(OrderErrorCode.PAYMENT_KEY_ALREADY_EXISTS);
    }
}