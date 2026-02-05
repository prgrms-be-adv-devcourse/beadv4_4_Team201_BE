package app.giftify.payment.application.inbound;

import java.util.List;

import app.giftify.payment.domain.OrderItemSnapshot;
import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

/**
 * 결제 생성 커맨드.
 *
 * <p>{@code orderId}는 멱등성 키 역할도 겸합니다.
 * 같은 orderId로 중복 결제 생성을 시도하면 기존 결제가 반환됩니다.</p>
 */
public record CreatePaymentCommand(
	Long memberId,
	String orderId,           // 멱등성 키 역할도 겸함
	PaymentType type,
	PaymentMethod method,
	Money expectedAmount,
	List<OrderItemSnapshot> orderItems
) {
	public CreatePaymentCommand {
		if (orderId == null || orderId.isBlank()) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[CreatePaymentCommand] orderId는 필수입니다.");
		}

		// POINT_CHARGE 유형이 아닌 경우에만 orderItems 검증
		if (type != PaymentType.POINT_CHARGE) {
			if (orderItems == null || orderItems.isEmpty()) {
				throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
					"[CreatePaymentCommand] orderItems는 필수입니다.");
			}

			// 실제 금액 계산
			Money itemsTotal = orderItems.stream()
				.map(OrderItemSnapshot::subtotal)
				.reduce(Money.zero(), Money::plus);

			//  "기대 금액"과 "실제 계산 금액" 비교
			if (!itemsTotal.equals(expectedAmount)) {
				throw new PaymentException(PaymentErrorCode.AMOUNT_MISMATCH,
					String.format("[CreatePaymentCommand] 주문 금액이 변경되었습니다. " +
						"기대 금액: %s, 현재 금액: %s", expectedAmount, itemsTotal));
			}
		}
	}
}
