package app.giftify.payment.application.outbound;

import java.util.List;

public record PgCancelResult(
	boolean success,
	String paymentKey,
	String lastTransactionKey,
	List<CancelDetail> cancels,
	String errorCode,
	String errorMessage
) {
	public record CancelDetail(
		String transactionKey,
		long cancelAmount,
		String canceledAt
	) {}

	public static PgCancelResult success(String paymentKey, String lastTransactionKey, List<CancelDetail> cancels) {
		return new PgCancelResult(true, paymentKey, lastTransactionKey, cancels, null, null);
	}

	public static PgCancelResult failure(String errorCode, String errorMessage) {
		return new PgCancelResult(false, null, null, List.of(), errorCode, errorMessage);
	}
}
