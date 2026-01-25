package app.giftify.application.inbound;

public record RefundPaymentCommand(
	Long paymentId,
	String reason
) {
}
