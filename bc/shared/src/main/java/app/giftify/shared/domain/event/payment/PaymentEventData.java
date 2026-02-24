package app.giftify.shared.domain.event.payment;

import app.giftify.shared.domain.type.CancelType;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

public record PaymentEventData(
	Long paymentId,
	Long orderId,
	Long memberId,
	String orderNumber,
	Money amount,
	PaymentMethod paymentMethod,
	PaymentType paymentType,
	CancelType cancelType,
	String reason,
	String paymentKey,
	String transactionKey
) {
	public static PaymentEventData forSuccess(
		Long paymentId, Long orderId, Long memberId, String orderNumber, Money amount,
		PaymentMethod method, PaymentType type,
		String paymentKey, String transactionKey
	) {
		return new PaymentEventData(
			paymentId, orderId, memberId, orderNumber, amount,
			method, type, null, null, paymentKey, transactionKey
		);
	}

	public static PaymentEventData forFailure(
		Long paymentId, Long orderId, Long memberId, String orderNumber, Money amount,
		PaymentMethod method, PaymentType type
	) {
		return new PaymentEventData(
			paymentId, orderId, memberId, orderNumber, amount,
			method, type, null, null, null, null
		);
	}

	public static PaymentEventData forCancel(
		Long paymentId, Long orderId, Long memberId, String orderNumber, Money amount,
		PaymentMethod method, PaymentType type,
		CancelType cancelType, String reason,
		String transactionKey
	) {
		return new PaymentEventData(
			paymentId, orderId, memberId, orderNumber, amount,
			method, type, cancelType, reason, null, transactionKey
		);
	}

	public static PaymentEventData forCancelFailed(
		Long paymentId, Long orderId, Long memberId, String orderNumber,
		PaymentMethod method, PaymentType type,
		String errorMetadata
	) {
		return new PaymentEventData(
			paymentId, orderId, memberId, orderNumber, null,
			method, type, null, errorMetadata, null, null
		);
	}
}
