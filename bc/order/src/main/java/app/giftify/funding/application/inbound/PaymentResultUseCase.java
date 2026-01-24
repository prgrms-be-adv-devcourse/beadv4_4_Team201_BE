package app.giftify.funding.application.inbound;

public interface PaymentResultUseCase {
    void completePayment(Long orderId, String paymentKey);
}
