package app.giftify.funding.application.inbound;

public interface PaymentResultUseCase {
    void completePayment(Long orderId, String paymentKey);

    void refundPayment(Long orderId, String reason);

    void failPayment(Long orderId);

    void cancelPayment(Long orderId);
}
