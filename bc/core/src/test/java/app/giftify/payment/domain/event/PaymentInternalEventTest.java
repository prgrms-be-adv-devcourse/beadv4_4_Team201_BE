package app.giftify.payment.domain.event;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("PaymentInternalEvent 테스트")
class PaymentInternalEventTest {

	@Nested
	@DisplayName("PaymentFailedEvent")
	class PaymentFailedEventTests {

		@Test
		@DisplayName("생성 시 필드가 올바르게 설정된다")
		void create_SetsFieldsCorrectly() {
			// given
			Long paymentId = 1L;
			String orderNumber = "ORD-001";
			LocalDateTime failedAt = LocalDateTime.of(2026, 2, 18, 10, 0);

			// when
			PaymentFailedEvent event = new PaymentFailedEvent(paymentId, orderNumber, failedAt);

			// then
			assertThat(event.getPaymentId()).isEqualTo(paymentId);
			assertThat(event.getOrderNumber()).isEqualTo(orderNumber);
			assertThat(event.getFailedAt()).isEqualTo(failedAt);
			assertThat(event.getEventId()).isNotNull();
			assertThat(event.getOccurredAt()).isNotNull();
		}
	}

	@Nested
	@DisplayName("PaymentCancelFailedEvent")
	class PaymentCancelFailedEventTests {

		@Test
		@DisplayName("생성 시 필드가 올바르게 설정된다")
		void create_SetsFieldsCorrectly() {
			// given
			Long paymentId = 2L;
			String orderNumber = "ORD-002";
			String errorMetadata = "{\"code\":\"PG_ERROR\",\"message\":\"취소 실패\"}";
			LocalDateTime cancelFailedAt = LocalDateTime.of(2026, 2, 18, 11, 0);

			// when
			PaymentCancelFailedEvent event = new PaymentCancelFailedEvent(
				paymentId, orderNumber, errorMetadata, cancelFailedAt);

			// then
			assertThat(event.getPaymentId()).isEqualTo(paymentId);
			assertThat(event.getOrderNumber()).isEqualTo(orderNumber);
			assertThat(event.getErrorMetadata()).isEqualTo(errorMetadata);
			assertThat(event.getCancelFailedAt()).isEqualTo(cancelFailedAt);
			assertThat(event.getEventId()).isNotNull();
			assertThat(event.getOccurredAt()).isNotNull();
		}
	}

	@Nested
	@DisplayName("PaymentReceivedEvent")
	class PaymentReceivedEventTests {

		@Test
		@DisplayName("생성 시 필드가 올바르게 설정된다")
		void create_SetsFieldsCorrectly() {
			// given
			Long paymentId = 3L;
			Long memberId = 100L;
			String orderNumber = "ORD-003";
			LocalDateTime receivedAt = LocalDateTime.of(2026, 2, 18, 12, 0);

			// when
			PaymentReceivedEvent event = new PaymentReceivedEvent(
				paymentId, memberId, orderNumber, receivedAt);

			// then
			assertThat(event.getPaymentId()).isEqualTo(paymentId);
			assertThat(event.getMemberId()).isEqualTo(memberId);
			assertThat(event.getOrderNumber()).isEqualTo(orderNumber);
			assertThat(event.getReceivedAt()).isEqualTo(receivedAt);
			assertThat(event.getEventId()).isNotNull();
			assertThat(event.getOccurredAt()).isNotNull();
		}
	}
}
