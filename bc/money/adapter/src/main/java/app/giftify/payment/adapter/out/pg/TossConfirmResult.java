package app.giftify.payment.adapter.out.pg;

public record TossConfirmResult(
	boolean success,
	String paymentKey,
	String errorCode,
	String errorMessage
) {
	public static TossConfirmResult success(String paymentKey) {
		return new TossConfirmResult(true, paymentKey, null, null);
	}

	public static TossConfirmResult failure(String errorCode, String errorMessage) {
		return new TossConfirmResult(false, null, errorCode, errorMessage);
	}
}