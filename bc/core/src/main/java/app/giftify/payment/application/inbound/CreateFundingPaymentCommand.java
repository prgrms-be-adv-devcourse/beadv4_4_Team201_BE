package app.giftify.payment.application.inbound;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import app.giftify.payment.domain.OrderItemSnapshot;
import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

public record CreateFundingPaymentCommand(
	Long memberId,
	Long orderId,
	String orderNumber,
	PaymentMethod method,
	Money expectedAmount,
	List<OrderItemSnapshot> orderItems
) {
	public CreateFundingPaymentCommand {
		if (memberId == null) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[CreateFundingPaymentCommand] memberId는 필수입니다.");
		}
		if (orderNumber == null || orderNumber.isBlank()) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[CreateFundingPaymentCommand] orderNumber는 필수입니다.");
		}
		if (method == null) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[CreateFundingPaymentCommand] method는 필수입니다.");
		}
		if (orderItems == null || orderItems.isEmpty()) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[CreateFundingPaymentCommand] orderItems는 필수입니다.");
		}
		if (expectedAmount == null) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[CreateFundingPaymentCommand] expectedAmount는 필수입니다.");
		}

		// 실제 금액 계산 및 검증
		Money itemsTotal = orderItems.stream()
			.map(OrderItemSnapshot::amount)
			.reduce(Money.zero(), Money::plus);

		if (!itemsTotal.equals(expectedAmount)) {
			throw new PaymentException(PaymentErrorCode.AMOUNT_MISMATCH,
				String.format("[CreateFundingPaymentCommand] 주문 금액이 변경되었습니다. " +
					"기대 금액: %s, 현재 금액: %s", expectedAmount, itemsTotal));
		}
	}

	/**
	 * 펀딩 결제의 PaymentType은 항상 FUNDING
	 */
	@JsonIgnore
	public PaymentType getType() {
		return PaymentType.FUNDING;
	}

}
