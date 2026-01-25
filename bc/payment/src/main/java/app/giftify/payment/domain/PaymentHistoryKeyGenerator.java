package app.giftify.payment.domain;

import java.util.Objects;

public final class PaymentHistoryKeyGenerator {

	private PaymentHistoryKeyGenerator() {
	}

	/**
	 * 이벤트 멱등성 키를 생성합니다.
	 *
	 * @param paymentIdempotencyKey 결제의 멱등성 키 (Payment 식별)
	 * @param eventType             이벤트 타입
	 * @param requestId             요청 식별자 (호출자가 제공, 재시도 시 동일 값)
	 * @return 이벤트 고유 멱등성 키
	 */
	public static String generate(
		String paymentIdempotencyKey,
		PaymentEventType eventType,
		String requestId
	) {
		Objects.requireNonNull(requestId, "requestId는 필수입니다");
		return paymentIdempotencyKey + "-" + eventType.name() + "-" + requestId;
	}
}
