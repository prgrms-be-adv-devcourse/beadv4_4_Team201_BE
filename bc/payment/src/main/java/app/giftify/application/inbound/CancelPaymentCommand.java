package app.giftify.application.inbound;

public record CancelPaymentCommand(
	Long paymentId,
	Long requesterId,
	String reason
) {
}
