package app.giftify.payment.application.inbound;

import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.shared.domain.vo.Money;

/**
 * 예치금 충전 커맨드.
 *
 * <p>예치금 충전은 orderItems가 필요 없으며, PG 승인 후 지갑에 예치금이 충전됩니다.</p>
 * <p>{@code orderId}는 멱등성 키 역할도 겸합니다.</p>
 */
public record ChargeDepositCommand(
	Long memberId,
	String orderNumber,
	Money amount
) {
	public ChargeDepositCommand {
		if (memberId == null) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[ChargeDepositCommand] memberId는 필수입니다.");
		}
		if (orderNumber == null || orderNumber.isBlank()) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[ChargeDepositCommand] orderNumber는 필수입니다.");
		}
		if (amount == null || amount.isLessThanOrEqual(Money.zero())) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[ChargeDepositCommand] 충전 금액은 0보다 커야 합니다.");
		}
	}
}
