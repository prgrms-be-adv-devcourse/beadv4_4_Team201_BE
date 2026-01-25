package app.giftify.payment.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * PaymentHistory 도메인 모델 단위 테스트.
 */
class PaymentHistoryTest {

	private static final Long PAYMENT_ID = 100L;
	private static final String IDEMPOTENCY_KEY = "idem-key-123";
	private static final PaymentEventType EVENT_TYPE = PaymentEventType.PAID;
	private static final LocalDateTime OCCURRED_AT = LocalDateTime.of(2026, 1, 25, 12, 0);
	private static final String METADATA = "{\"pg\":\"toss\"}";

	// ========== 팩토리 메서드 테스트 ========== //

	@Nested
	@DisplayName("Given create 팩토리 메서드")
	class Given_create_팩토리_메서드 {

		@Test
		@DisplayName("id는 null, metadata는 null로 생성된다")
		void id와_metadata는_null() {
			PaymentHistory history = PaymentHistory.create(
				PAYMENT_ID, IDEMPOTENCY_KEY, EVENT_TYPE, OCCURRED_AT
			);

			assertThat(history.getId()).isNull();
			assertThat(history.getPaymentId()).isEqualTo(PAYMENT_ID);
			assertThat(history.getIdempotencyKey()).isEqualTo(IDEMPOTENCY_KEY);
			assertThat(history.getEventType()).isEqualTo(EVENT_TYPE);
			assertThat(history.getOccurredAt()).isEqualTo(OCCURRED_AT);
			assertThat(history.getMetadata()).isNull();
		}
	}

	@Nested
	@DisplayName("Given withMetadata 팩토리 메서드")
	class Given_withMetadata_팩토리_메서드 {

		@Test
		@DisplayName("id는 null, metadata는 설정된 값으로 생성된다")
		void id는_null_metadata는_설정값() {
			PaymentHistory history = PaymentHistory.withMetadata(
				PAYMENT_ID, IDEMPOTENCY_KEY, EVENT_TYPE, OCCURRED_AT, METADATA
			);

			assertThat(history.getId()).isNull();
			assertThat(history.getMetadata()).isEqualTo(METADATA);
		}
	}

	@Nested
	@DisplayName("Given restore 팩토리 메서드")
	class Given_restore_팩토리_메서드 {

		@Test
		@DisplayName("id 포함하여 모든 필드가 복원된다")
		void 모든_필드_복원() {
			Long historyId = 1L;

			PaymentHistory history = PaymentHistory.restore(
				historyId, PAYMENT_ID, IDEMPOTENCY_KEY, EVENT_TYPE, OCCURRED_AT, METADATA
			);

			assertThat(history.getId()).isEqualTo(historyId);
			assertThat(history.getPaymentId()).isEqualTo(PAYMENT_ID);
			assertThat(history.getIdempotencyKey()).isEqualTo(IDEMPOTENCY_KEY);
			assertThat(history.getEventType()).isEqualTo(EVENT_TYPE);
			assertThat(history.getOccurredAt()).isEqualTo(OCCURRED_AT);
			assertThat(history.getMetadata()).isEqualTo(METADATA);
		}
	}

	// ========== equals / hashCode (BaseDomainModel 상속) ========== //

	@Nested
	@DisplayName("Given equals/hashCode")
	class Given_equals_hashCode {

		@Test
		@DisplayName("동일한 id를 가진 PaymentHistory는 동등하다")
		void 동일_id_동등() {
			PaymentHistory history1 = PaymentHistory.restore(
				1L, 100L, "key-1", PaymentEventType.PAID, OCCURRED_AT, null
			);
			PaymentHistory history2 = PaymentHistory.restore(
				1L, 200L, "key-2", PaymentEventType.REFUNDED, OCCURRED_AT.plusDays(1), "meta"
			);

			assertThat(history1).isEqualTo(history2);
			assertThat(history1.hashCode()).isEqualTo(history2.hashCode());
		}

		@Test
		@DisplayName("다른 id를 가진 PaymentHistory는 다르다")
		void 다른_id_다름() {
			PaymentHistory history1 = PaymentHistory.restore(
				1L, PAYMENT_ID, IDEMPOTENCY_KEY, EVENT_TYPE, OCCURRED_AT, null
			);
			PaymentHistory history2 = PaymentHistory.restore(
				2L, PAYMENT_ID, IDEMPOTENCY_KEY, EVENT_TYPE, OCCURRED_AT, null
			);

			assertThat(history1).isNotEqualTo(history2);
		}

		@Test
		@DisplayName("id가 null인 경우 (비영속 상태) 동등성 비교")
		void id_null_비영속_상태() {
			PaymentHistory history1 = PaymentHistory.create(
				PAYMENT_ID, IDEMPOTENCY_KEY, EVENT_TYPE, OCCURRED_AT
			);
			PaymentHistory history2 = PaymentHistory.create(
				PAYMENT_ID, IDEMPOTENCY_KEY, EVENT_TYPE, OCCURRED_AT
			);

			// BaseDomainModel은 id가 null이면 null끼리 같다고 판단
			assertThat(history1).isEqualTo(history2);
		}
	}
}