package app.giftify.payment.domain;

import java.util.Objects;

public final class PaymentHistoryKeyGenerator {

	private PaymentHistoryKeyGenerator() {
	}

	public static String generate(
		String orderId,
		PaymentEventType eventType,
		String requestId
	) {
		Objects.requireNonNull(requestId, "requestId는 필수입니다");
		return orderId + "-" + eventType.name() + "-" + requestId;
	}
}
