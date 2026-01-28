package app.giftify.payment.application.inbound;

import java.time.LocalDateTime;

import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;

/**
 * 결제 승인 Command.
 * PG사 결제 완료 후 Payment 상태를 PAID로 변경할 때 사용합니다.
 *
 * @param paymentId   승인할 Payment ID
 * @param paymentKey  PG사에서 받은 결제 키 (암호화 저장됨)
 * @param approveCode PG사 승인 코드 (optional, 암호화 저장됨)
 * @param paidAt      결제 완료 시각
 */
public record ConfirmPaymentCommand(
	Long paymentId,
	String paymentKey,
	String approveCode,
	LocalDateTime paidAt
) {
	public ConfirmPaymentCommand {
		if (paymentId == null) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[ConfirmPaymentCommand] paymentId는 필수입니다.");
		}
		if (paymentKey == null || paymentKey.isBlank()) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[ConfirmPaymentCommand] paymentKey는 필수입니다.");
		}
		if (paidAt == null) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[ConfirmPaymentCommand] paidAt은 필수입니다.");
		}
		// approveCode는 optional (PG사에 따라 없을 수 있음)
	}
}
