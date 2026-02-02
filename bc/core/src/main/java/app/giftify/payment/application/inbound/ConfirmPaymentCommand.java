package app.giftify.payment.application.inbound;

import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.shared.domain.vo.Money;

/**
 * 결제 승인 Command.
 * PG사 결제 완료 후 Payment 상태를 PAID로 변경할 때 사용합니다.
 *
 * @param paymentId       승인할 Payment ID
 * @param requesterId     요청자 ID (소유자 검증용)
 * @param paymentKey      PG사에서 받은 결제 키 (암호화 저장됨)
 * @param orderId         주문 ID (PG 승인 요청용)
 * @param requestedAmount 요청 금액 (조작 방지 검증용)
 */
public record ConfirmPaymentCommand(
	Long paymentId,
	Long requesterId,
	String paymentKey,
	String orderId,
	Money requestedAmount
) {
	public ConfirmPaymentCommand {
		if (paymentId == null) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[ConfirmPaymentCommand] paymentId는 필수입니다.");
		}
		if (requesterId == null) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[ConfirmPaymentCommand] requesterId는 필수입니다.");
		}
		if (paymentKey == null || paymentKey.isBlank()) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[ConfirmPaymentCommand] paymentKey는 필수입니다.");
		}
		if (orderId == null || orderId.isBlank()) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[ConfirmPaymentCommand] orderId는 필수입니다.");
		}
		if (requestedAmount == null) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[ConfirmPaymentCommand] requestedAmount는 필수입니다.");
		}
	}
}
