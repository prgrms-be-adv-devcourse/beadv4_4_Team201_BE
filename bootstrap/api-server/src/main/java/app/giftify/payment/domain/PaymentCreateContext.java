package app.giftify.payment.domain;

import app.giftify.payment.domain.type.PaymentMethod;
import app.giftify.payment.domain.type.PaymentType;

/**
 * 결제 생성에 필요한 컨텍스트 정보.
 * Money(금액)는 orderId를 통해 Order에서 조회하여 설정합니다.
 */
public record PaymentCreateContext(
	Long memberId,
	Long orderId,
	String orderNumber,
	PaymentType type,
	PaymentMethod method
) {
	/**
	 * Compact Constructor - 필수 필드 검증
	 */
	public PaymentCreateContext {
		if (memberId == null) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[PaymentCreateContext] memberId는 필수입니다.");
		}
		if (orderNumber == null || orderNumber.isBlank()) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[PaymentCreateContext] orderNumber는 필수입니다.");
		}
		if (type == null) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[PaymentCreateContext] type은 필수입니다.");
		}
		if (method == null) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[PaymentCreateContext] method는 필수입니다.");
		}
	}
}
