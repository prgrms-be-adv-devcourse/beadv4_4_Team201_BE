package app.giftify.payment.application.outbound;

public record PgConfirmResult(
	boolean success,
	String paymentKey,
	String lastTransactionKey,
	String approveNo,
	String errorCode,
	String errorMessage
) {
	public static PgConfirmResult success(String paymentKey, String lastTransactionKey, String approveNo) {
		return new PgConfirmResult(true, paymentKey, lastTransactionKey, approveNo, null, null);
	}

	public static PgConfirmResult failure(String errorCode, String errorMessage) {
		return new PgConfirmResult(false, null, null, null, errorCode, errorMessage);
	}
}
