package domain.payment;

import app.giftify.shared.domain.payment.PaymentType;

public interface PaymentPolicy {
	boolean support(PaymentType type);

	void validate(PaymentCreateContext context); // 실패시 예외 던지도록
}
