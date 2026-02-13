package app.giftify.payment.domain;

import java.util.Objects;

public final class PaymentHistoryKeyGenerator {

	private PaymentHistoryKeyGenerator() {
	}

	public static String generate(
		String orderId,
		PaymentEventType eventType,
		String eventId
	) {
		Objects.requireNonNull(eventId, "eventId는 필수입니다");
		return orderId + "-" + eventType.name() + "-" + eventId;
	}
}
