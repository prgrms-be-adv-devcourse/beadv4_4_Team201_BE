package app.giftify.application.inbound;

public record PaymentDetailQuery(
	Long paymentId,
	Long requesterId
) {
}
