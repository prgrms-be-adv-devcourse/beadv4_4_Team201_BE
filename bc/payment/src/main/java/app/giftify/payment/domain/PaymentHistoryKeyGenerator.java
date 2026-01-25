package app.giftify.payment.domain;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

/**
 * PaymentHistory 이벤트 멱등성 키 생성기.
 *
 * <p>이벤트 레벨의 멱등성을 보장하기 위해, 각 이벤트마다 고유한 키를 생성합니다.
 * Payment.idempotencyKey(결제 생성 멱등성)와는 다른 목적으로 사용됩니다.</p>
 *
 * <p>생성되는 키 형식: {@code {paymentIdempotencyKey}-{eventType}-{uniqueId}}</p>
 * <ul>
 *   <li>paymentIdempotencyKey: 어느 결제의 이벤트인지 식별</li>
 *   <li>eventType: 어떤 이벤트인지 식별</li>
 *   <li>uniqueId: 동일 이벤트 타입이 여러 번 발생할 경우 구분 (CANCEL_FAILED 등)</li>
 * </ul>
 * <ul>
 *
 * <li> // 결제 생성 이벤트  order-12345-pay-CREATED-Ks8dF2xLQm-v7N3pRtYw_A   </li>
 *
 *
 *  <li> // 결제 완료 이벤트   order-12345-pay-PAID-mX9aB1cDEf-gH2iJ3kL4M        </li>
 *
 *  <li> // 환불 이벤트 order-12345-pay-REFUNDED-nO5pQ6rS7t-uV8wX9yZ0a </li>
 *
 *
 *   <li>// 취소 실패 이벤트 (여러 번 발생 가능)  order-12345-pay-CANCEL_FAILED-aB1cD2eF3g-hI4jK5lM6n        </li>
 *   <li>order-12345-pay-CANCEL_FAILED-zY9xW8vU7t-sR6qP5oN4m  ← 재시도 시 다른 uniqueId      </li>
 * </ul>
 */
public final class PaymentHistoryKeyGenerator {

	private PaymentHistoryKeyGenerator() {
	}

	/**
	 * 이벤트 멱등성 키를 생성합니다.
	 *
	 * @param paymentIdempotencyKey 결제의 멱등성 키
	 * @param eventType             이벤트 타입
	 * @return 이벤트 고유 멱등성 키
	 */
	public static String generate(String paymentIdempotencyKey, PaymentEventType eventType) {
		return paymentIdempotencyKey + "-" + eventType.name() + "-" + generateUniqueId();
	}

	/**
	 * UUID 기반 고유 ID 생성 (Base64 URL-safe 인코딩).
	 */
	private static String generateUniqueId() {
		UUID uuid = UUID.randomUUID();
		ByteBuffer buffer = ByteBuffer.allocate(16);
		buffer.putLong(uuid.getMostSignificantBits());
		buffer.putLong(uuid.getLeastSignificantBits());
		return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer.array());
	}
}
