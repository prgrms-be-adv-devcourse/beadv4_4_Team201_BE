package app.giftify.payment.adapter.outbound.pg;

public record TossCancelResult(
	boolean success,
	String paymentKey,
	String lastTransactionKey,
	String errorCode,
	String errorMessage
) {
	public static TossCancelResult success(String paymentKey, String lastTransactionKey) {
		return new TossCancelResult(true, paymentKey, lastTransactionKey, null, null);
	}

	public static TossCancelResult failure(String errorCode, String errorMessage) {
		return new TossCancelResult(false, null, null, errorCode, errorMessage);
	}
}
