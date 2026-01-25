package app.giftify.application.inbound;

import java.util.List;

import app.giftify.payment.domain.OrderItemSnapshot;
import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.payment.domain.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

public record CreatePaymentCommand(
	Long memberId,
	String orderId,
	PaymentType type,
	PaymentMethod method,
	Money amount,
	List<OrderItemSnapshot> orderItems
) {
	public CreatePaymentCommand {
		if (orderItems == null || orderItems.isEmpty()) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE, "orderItems는 필수입니다.");
		}

		Money itemsTotal = orderItems.stream()
			.map(OrderItemSnapshot::subtotal)
			.reduce(Money.zero(), Money::plus);

		if (!itemsTotal.equals(amount)) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				String.format("orderItems 합계(%s)와 amount(%s)가 일치하지 않습니다.", itemsTotal, amount));
		}
	}
}
