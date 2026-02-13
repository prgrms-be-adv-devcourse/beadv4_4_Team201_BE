package app.giftify.payment.adapter.outbound.pg;

public record TossConfirmResult(
	boolean success,
	String paymentKey,
	String lastTransactionKey,
	String approveNo,
	String errorCode,
	String errorMessage
) {
	public static TossConfirmResult success(String paymentKey, String lastTransactionKey, String approveNo) {
		return new TossConfirmResult(true, paymentKey, lastTransactionKey, approveNo, null, null);
	}

	public static TossConfirmResult failure(String errorCode, String errorMessage) {
		return new TossConfirmResult(false, null, null, null, errorCode, errorMessage);
	}
}
