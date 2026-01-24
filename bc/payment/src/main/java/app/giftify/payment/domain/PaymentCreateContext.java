package app.giftify.payment.domain;

import app.giftify.shared.domain.type.PaymentType;

/**
 * 결제 생성에 필요한 컨텍스트 정보.
 * Money(금액)는 orderId를 통해 Order에서 조회하여 설정합니다.
 */
public record PaymentCreateContext(
	Long memberId,
	String orderId,
	PaymentType type,
	PaymentMethod method
) {
}
