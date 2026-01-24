package payment.usecase.command;

import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

/**
 * 결제 시작 커맨드.
 * Order BC가 Payment BC에 결제를 요청할 때 사용합니다.
 */
public record PaymentInitiateCommand(
	Long userId,
	Long orderId,           // Order BC에서 생성한 주문 ID
	Money amount,
	PaymentType paymentType // Order BC가 결정 (FUNDING, PRODUCT 등)
) {
}
