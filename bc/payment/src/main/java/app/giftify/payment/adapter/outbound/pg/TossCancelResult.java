package app.giftify.payment.adapter.outbound.pg;

public record TossCancelResult(
	boolean success,
	String paymentKey,
	String errorCode,
	String errorMessage
) {
	public static TossCancelResult success(String paymentKey) {
		return new TossCancelResult(true, paymentKey, null, null);
	}

	public static TossCancelResult failure(String errorCode, String errorMessage) {
		return new TossCancelResult(false, null, errorCode, errorMessage);
	}
}
