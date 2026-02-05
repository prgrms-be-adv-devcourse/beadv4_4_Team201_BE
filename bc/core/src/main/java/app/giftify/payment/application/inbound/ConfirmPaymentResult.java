package app.giftify.payment.application.inbound;

/**
 * 결제 승인 결과.
 *
 * @param success      승인 성공 여부
 * @param paymentId    결제 ID
 * @param errorCode    실패 시 에러 코드 (PG사 응답)
 * @param errorMessage 실패 시 에러 메시지 (PG사 응답)
 */
public record ConfirmPaymentResult(
	boolean success,
	Long paymentId,
	String errorCode,
	String errorMessage
) {
	public static ConfirmPaymentResult success(Long paymentId) {
		return new ConfirmPaymentResult(true, paymentId, null, null);
	}

	public static ConfirmPaymentResult failure(String errorCode, String errorMessage) {
		return new ConfirmPaymentResult(false, null, errorCode, errorMessage);
	}
}
