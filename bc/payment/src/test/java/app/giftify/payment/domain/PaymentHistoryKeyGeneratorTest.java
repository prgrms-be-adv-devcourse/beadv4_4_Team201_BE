package app.giftify.payment.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * PaymentHistoryKeyGenerator 단위 테스트.
 * 결정적 멱등성 키 생성을 검증합니다.
 */
class PaymentHistoryKeyGeneratorTest {

	private static final String PAYMENT_IDEMPOTENCY_KEY = "order-12345-pay";
	private static final String REQUEST_ID = "toss-evt-abc123";

	// ========== 키 생성 형식 검증 ========== //

	@Nested
	@DisplayName("Given generate 메서드 호출 시")
	class Given_generate_메서드_호출_시 {

		@Test
		@DisplayName("Then {paymentIdempotencyKey}-{eventType}-{requestId} 형식으로 키가 생성된다")
		void Then_올바른_형식으로_키_생성() {
			String key = PaymentHistoryKeyGenerator.generate(
				PAYMENT_IDEMPOTENCY_KEY,
				PaymentEventType.PAID,
				REQUEST_ID
			);

			assertThat(key).isEqualTo("order-12345-pay-PAID-toss-evt-abc123");
		}

		@Test
		@DisplayName("Then 모든 이벤트 타입에 대해 올바른 형식의 키가 생성된다")
		void Then_모든_이벤트_타입_키_생성() {
			assertThat(PaymentHistoryKeyGenerator.generate(PAYMENT_IDEMPOTENCY_KEY, PaymentEventType.CREATED, REQUEST_ID))
				.isEqualTo("order-12345-pay-CREATED-toss-evt-abc123");

			assertThat(PaymentHistoryKeyGenerator.generate(PAYMENT_IDEMPOTENCY_KEY, PaymentEventType.PAID, REQUEST_ID))
				.isEqualTo("order-12345-pay-PAID-toss-evt-abc123");

			assertThat(PaymentHistoryKeyGenerator.generate(PAYMENT_IDEMPOTENCY_KEY, PaymentEventType.FAILED, REQUEST_ID))
				.isEqualTo("order-12345-pay-FAILED-toss-evt-abc123");

			assertThat(PaymentHistoryKeyGenerator.generate(PAYMENT_IDEMPOTENCY_KEY, PaymentEventType.CANCELED, REQUEST_ID))
				.isEqualTo("order-12345-pay-CANCELED-toss-evt-abc123");

			assertThat(PaymentHistoryKeyGenerator.generate(PAYMENT_IDEMPOTENCY_KEY, PaymentEventType.REFUNDED, REQUEST_ID))
				.isEqualTo("order-12345-pay-REFUNDED-toss-evt-abc123");

			assertThat(PaymentHistoryKeyGenerator.generate(PAYMENT_IDEMPOTENCY_KEY, PaymentEventType.RECEIVED, REQUEST_ID))
				.isEqualTo("order-12345-pay-RECEIVED-toss-evt-abc123");

			assertThat(PaymentHistoryKeyGenerator.generate(PAYMENT_IDEMPOTENCY_KEY, PaymentEventType.CANCEL_FAILED, REQUEST_ID))
				.isEqualTo("order-12345-pay-CANCEL_FAILED-toss-evt-abc123");
		}
	}

	// ========== 결정적 키 생성 검증 ========== //

	@Nested
	@DisplayName("Given 동일한 입력값으로 여러 번 호출 시")
	class Given_동일한_입력값으로_여러_번_호출_시 {

		@Test
		@DisplayName("Then 항상 동일한 키가 생성된다 (멱등성 보장)")
		void Then_동일한_키_생성_멱등성_보장() {
			String key1 = PaymentHistoryKeyGenerator.generate(
				PAYMENT_IDEMPOTENCY_KEY, PaymentEventType.PAID, REQUEST_ID
			);
			String key2 = PaymentHistoryKeyGenerator.generate(
				PAYMENT_IDEMPOTENCY_KEY, PaymentEventType.PAID, REQUEST_ID
			);
			String key3 = PaymentHistoryKeyGenerator.generate(
				PAYMENT_IDEMPOTENCY_KEY, PaymentEventType.PAID, REQUEST_ID
			);

			assertThat(key1).isEqualTo(key2).isEqualTo(key3);
		}
	}

	// ========== 다른 입력값은 다른 키 생성 검증 ========== //

	@Nested
	@DisplayName("Given 다른 입력값으로 호출 시")
	class Given_다른_입력값으로_호출_시 {

		@Test
		@DisplayName("Then paymentIdempotencyKey가 다르면 다른 키가 생성된다")
		void Then_다른_paymentIdempotencyKey_다른_키() {
			String key1 = PaymentHistoryKeyGenerator.generate(
				"order-111-pay", PaymentEventType.PAID, REQUEST_ID
			);
			String key2 = PaymentHistoryKeyGenerator.generate(
				"order-222-pay", PaymentEventType.PAID, REQUEST_ID
			);

			assertThat(key1).isNotEqualTo(key2);
		}

		@Test
		@DisplayName("Then eventType이 다르면 다른 키가 생성된다")
		void Then_다른_eventType_다른_키() {
			String key1 = PaymentHistoryKeyGenerator.generate(
				PAYMENT_IDEMPOTENCY_KEY, PaymentEventType.PAID, REQUEST_ID
			);
			String key2 = PaymentHistoryKeyGenerator.generate(
				PAYMENT_IDEMPOTENCY_KEY, PaymentEventType.REFUNDED, REQUEST_ID
			);

			assertThat(key1).isNotEqualTo(key2);
		}

		@Test
		@DisplayName("Then requestId가 다르면 다른 키가 생성된다")
		void Then_다른_requestId_다른_키() {
			String key1 = PaymentHistoryKeyGenerator.generate(
				PAYMENT_IDEMPOTENCY_KEY, PaymentEventType.PAID, "request-001"
			);
			String key2 = PaymentHistoryKeyGenerator.generate(
				PAYMENT_IDEMPOTENCY_KEY, PaymentEventType.PAID, "request-002"
			);

			assertThat(key1).isNotEqualTo(key2);
		}
	}

	// ========== null 검증 ========== //

	@Nested
	@DisplayName("Given requestId가 null인 경우")
	class Given_requestId가_null인_경우 {

		@Test
		@DisplayName("Then NullPointerException이 발생한다")
		void Then_NullPointerException_발생() {
			assertThatThrownBy(() ->
				PaymentHistoryKeyGenerator.generate(
					PAYMENT_IDEMPOTENCY_KEY,
					PaymentEventType.PAID,
					null
				))
				.isInstanceOf(NullPointerException.class)
				.hasMessageContaining("requestId는 필수입니다");
		}
	}

	// ========== 실제 시나리오 검증 ========== //

	@Nested
	@DisplayName("Given 실제 사용 시나리오")
	class Given_실제_사용_시나리오 {

		@Test
		@DisplayName("Then PG 웹훅 재시도 시 동일한 키가 생성되어 멱등성이 보장된다")
		void Then_PG_웹훅_재시도_멱등성_보장() {
			// PG사(Toss)가 웹훅을 재전송할 때 동일한 eventId를 사용
			String webhookEventId = "toss-webhook-evt-abc123";

			String firstAttemptKey = PaymentHistoryKeyGenerator.generate(
				"order-12345-pay", PaymentEventType.PAID, webhookEventId
			);
			String retryAttemptKey = PaymentHistoryKeyGenerator.generate(
				"order-12345-pay", PaymentEventType.PAID, webhookEventId
			);

			// 재시도 시에도 동일한 키 → DB UNIQUE 제약으로 중복 INSERT 방지
			assertThat(firstAttemptKey).isEqualTo(retryAttemptKey);
		}

		@Test
		@DisplayName("Then 취소 실패가 여러 번 발생해도 각각 다른 requestId로 구분된다")
		void Then_취소_실패_여러_번_구분() {
			// 첫 번째 취소 요청 실패
			String cancelRequest1Key = PaymentHistoryKeyGenerator.generate(
				"order-12345-pay", PaymentEventType.CANCEL_FAILED, "cancel-req-001"
			);

			// 두 번째 취소 요청 실패 (다른 요청)
			String cancelRequest2Key = PaymentHistoryKeyGenerator.generate(
				"order-12345-pay", PaymentEventType.CANCEL_FAILED, "cancel-req-002"
			);

			// 다른 요청이므로 다른 키 → 별도로 기록됨
			assertThat(cancelRequest1Key).isNotEqualTo(cancelRequest2Key);
		}
	}
}
