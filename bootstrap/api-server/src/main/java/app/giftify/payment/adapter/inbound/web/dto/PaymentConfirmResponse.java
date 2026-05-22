package app.giftify.payment.adapter.inbound.web.dto;

public record PaymentConfirmResponse(
	Long paymentId,
	boolean success,
	String errorCode,
	String errorMessage
) {
	public static PaymentConfirmResponse success(Long paymentId) {
		return new PaymentConfirmResponse(paymentId, true, null, null);
	}

	public static PaymentConfirmResponse failure(String errorCode, String errorMessage) {
		return new PaymentConfirmResponse(null, false, errorCode, errorMessage);
	}
}
