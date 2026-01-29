package app.giftify.order.application.inbound;

public interface PaymentResultUseCase {
    void completePayment(Long orderId, String paymentKey);

    void refundPayment(Long orderId);

    void failPayment(Long orderId);

    void cancelPayment(Long orderId);
}
