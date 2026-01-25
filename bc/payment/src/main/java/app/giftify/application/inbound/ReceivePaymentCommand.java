package app.giftify.application.inbound;

public record ReceivePaymentCommand(
	Long paymentId
) {
}
