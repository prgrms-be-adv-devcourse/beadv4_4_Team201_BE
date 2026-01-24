package payment.usecase.command;

/**
 * 결제 환불 요청 커맨드.
 * 펀딩 만료/취소 시 자동 환불 처리에 사용됩니다.
 */
public record RefundPaymentCommand(
	Long paymentId,
	String reason
) {
}
