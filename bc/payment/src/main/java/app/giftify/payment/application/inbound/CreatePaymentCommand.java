package app.giftify.payment.application.inbound;

import java.util.List;

import app.giftify.payment.domain.OrderItemSnapshot;
import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.payment.domain.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

public record CreatePaymentCommand(
	String idempotencyKey,
	Long memberId,
	String orderId,
	PaymentType type,
	PaymentMethod method,
	Money amount,
	List<OrderItemSnapshot> orderItems
) {
	public CreatePaymentCommand {
		if (idempotencyKey == null || idempotencyKey.isBlank()) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[CreatePaymentCommand] idempotencyKey는 필수입니다.");
		}

		if (orderItems == null || orderItems.isEmpty()) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[CreatePaymentCommand] orderItems는 필수입니다.");
		}

		Money itemsTotal = orderItems.stream()
			.map(OrderItemSnapshot::subtotal)
			.reduce(Money.zero(), Money::plus);

		if (!itemsTotal.equals(amount)) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				String.format("[CreatePaymentCommand] orderItems 합계(%s)와 amount(%s)가 일치하지 않습니다.",
					itemsTotal, amount));
		}
	}
}
