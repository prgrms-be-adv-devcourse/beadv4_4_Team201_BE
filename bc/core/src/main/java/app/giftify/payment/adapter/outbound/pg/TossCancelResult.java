package app.giftify.payment.adapter.outbound.pg;

import java.util.List;

public record TossCancelResult(
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

	public static TossCancelResult success(String paymentKey, String lastTransactionKey, List<CancelDetail> cancels) {
		return new TossCancelResult(true, paymentKey, lastTransactionKey, cancels, null, null);
	}

	public static TossCancelResult failure(String errorCode, String errorMessage) {
		return new TossCancelResult(false, null, null, List.of(), errorCode, errorMessage);
	}
}
