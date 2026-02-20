package app.giftify.payment.adapter.inbound.event;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.payment.adapter.outbound.jpa.JpaPaymentHistoryRepository;
import app.giftify.payment.adapter.outbound.jpa.entity.JpaPaymentHistory;
import app.giftify.payment.domain.PaymentEventType;
import app.giftify.payment.domain.event.PaymentCancelFailedEvent;
import app.giftify.payment.domain.event.PaymentCanceledEvent;
import app.giftify.payment.domain.event.PaymentConfirmedEvent;
import app.giftify.payment.domain.event.PaymentFailedEvent;
import app.giftify.payment.domain.event.PaymentReceivedEvent;
import app.giftify.payment.domain.event.PaymentRefundedEvent;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentHistoryEventListener 테스트")
class PaymentHistoryEventListenerTest {

	@Mock
	private JpaPaymentHistoryRepository historyRepository;

	@InjectMocks
	private PaymentHistoryEventListener listener;

	private static final Long PAYMENT_ID = 1L;
	private static final String ORDER_NUMBER = "ORD-001";
	private static final LocalDateTime NOW = LocalDateTime.of(2026, 2, 18, 14, 0);

	@Nested
	@DisplayName("onPaymentConfirmed")
	class OnPaymentConfirmedTests {

		@Test
		@DisplayName("PaymentConfirmedEvent를 받으면 PAID 히스토리를 저장한다")
		void onPaymentConfirmed_SavesPaidHistory() {
			// given
			PaymentConfirmedEvent event = new PaymentConfirmedEvent(
				PAYMENT_ID, 100L, ORDER_NUMBER, PaymentType.FUNDING, Money.of(10000), NOW);

			// when
			listener.onPaymentConfirmed(event);

			// then
			ArgumentCaptor<JpaPaymentHistory> captor = ArgumentCaptor.forClass(JpaPaymentHistory.class);
			verify(historyRepository).save(captor.capture());

			JpaPaymentHistory saved = captor.getValue();
			assertThat(saved.getPaymentId()).isEqualTo(PAYMENT_ID);
			assertThat(saved.getEventType()).isEqualTo(PaymentEventType.PAID);
			assertThat(saved.getOccurredAt()).isNotNull();
			assertThat(saved.getHistoryKey()).contains(ORDER_NUMBER)
				.contains(PaymentEventType.PAID.name())
				.contains(event.getEventId());
			assertThat(saved.getMetadata()).isNull();
		}
	}

	@Nested
	@DisplayName("onPaymentCanceled")
	class OnPaymentCanceledTests {

		@Test
		@DisplayName("PaymentCanceledEvent를 받으면 CANCELED 히스토리를 저장한다")
		void onPaymentCanceled_SavesCanceledHistory() {
			// given
			PaymentCanceledEvent event = new PaymentCanceledEvent(
				PAYMENT_ID, 100L, ORDER_NUMBER, PaymentType.FUNDING, Money.of(10000), "테스트 취소", NOW);

			// when
			listener.onPaymentCanceled(event);

			// then
			ArgumentCaptor<JpaPaymentHistory> captor = ArgumentCaptor.forClass(JpaPaymentHistory.class);
			verify(historyRepository).save(captor.capture());

			JpaPaymentHistory saved = captor.getValue();
			assertThat(saved.getPaymentId()).isEqualTo(PAYMENT_ID);
			assertThat(saved.getEventType()).isEqualTo(PaymentEventType.CANCELED);
			assertThat(saved.getHistoryKey()).contains(PaymentEventType.CANCELED.name());
		}
	}

	@Nested
	@DisplayName("onPaymentRefunded")
	class OnPaymentRefundedTests {

		@Test
		@DisplayName("PaymentRefundedEvent를 받으면 REFUNDED 히스토리를 저장한다")
		void onPaymentRefunded_SavesRefundedHistory() {
			// given
			PaymentRefundedEvent event = new PaymentRefundedEvent(
				PAYMENT_ID, 100L, ORDER_NUMBER, PaymentType.FUNDING, Money.of(5000), "환불 사유", NOW);

			// when
			listener.onPaymentRefunded(event);

			// then
			ArgumentCaptor<JpaPaymentHistory> captor = ArgumentCaptor.forClass(JpaPaymentHistory.class);
			verify(historyRepository).save(captor.capture());

			JpaPaymentHistory saved = captor.getValue();
			assertThat(saved.getPaymentId()).isEqualTo(PAYMENT_ID);
			assertThat(saved.getEventType()).isEqualTo(PaymentEventType.REFUNDED);
			assertThat(saved.getHistoryKey()).contains(PaymentEventType.REFUNDED.name());
		}
	}

	@Nested
	@DisplayName("onPaymentReceived")
	class OnPaymentReceivedTests {

		@Test
		@DisplayName("PaymentReceivedEvent를 받으면 RECEIVED 히스토리를 저장한다")
		void onPaymentReceived_SavesReceivedHistory() {
			// given
			PaymentReceivedEvent event = new PaymentReceivedEvent(
				PAYMENT_ID, 100L, ORDER_NUMBER, NOW);

			// when
			listener.onPaymentReceived(event);

			// then
			ArgumentCaptor<JpaPaymentHistory> captor = ArgumentCaptor.forClass(JpaPaymentHistory.class);
			verify(historyRepository).save(captor.capture());

			JpaPaymentHistory saved = captor.getValue();
			assertThat(saved.getPaymentId()).isEqualTo(PAYMENT_ID);
			assertThat(saved.getEventType()).isEqualTo(PaymentEventType.RECEIVED);
		}
	}

	@Nested
	@DisplayName("onPaymentFailed")
	class OnPaymentFailedTests {

		@Test
		@DisplayName("PaymentFailedEvent를 받으면 FAILED 히스토리를 저장한다")
		void onPaymentFailed_SavesFailedHistory() {
			// given
			PaymentFailedEvent event = new PaymentFailedEvent(
				PAYMENT_ID, ORDER_NUMBER, NOW);

			// when
			listener.onPaymentFailed(event);

			// then
			ArgumentCaptor<JpaPaymentHistory> captor = ArgumentCaptor.forClass(JpaPaymentHistory.class);
			verify(historyRepository).save(captor.capture());

			JpaPaymentHistory saved = captor.getValue();
			assertThat(saved.getPaymentId()).isEqualTo(PAYMENT_ID);
			assertThat(saved.getEventType()).isEqualTo(PaymentEventType.FAILED);
		}
	}

	@Nested
	@DisplayName("onPaymentCancelFailed")
	class OnPaymentCancelFailedTests {

		@Test
		@DisplayName("PaymentCancelFailedEvent를 받으면 CANCEL_FAILED 히스토리와 metadata를 저장한다")
		void onPaymentCancelFailed_SavesCancelFailedHistoryWithMetadata() {
			// given
			String errorMetadata = "{\"code\":\"PG_ERROR\"}";
			PaymentCancelFailedEvent event = new PaymentCancelFailedEvent(
				PAYMENT_ID, ORDER_NUMBER, errorMetadata, NOW);

			// when
			listener.onPaymentCancelFailed(event);

			// then
			ArgumentCaptor<JpaPaymentHistory> captor = ArgumentCaptor.forClass(JpaPaymentHistory.class);
			verify(historyRepository).save(captor.capture());

			JpaPaymentHistory saved = captor.getValue();
			assertThat(saved.getPaymentId()).isEqualTo(PAYMENT_ID);
			assertThat(saved.getEventType()).isEqualTo(PaymentEventType.CANCEL_FAILED);
			assertThat(saved.getMetadata()).isEqualTo(errorMetadata);
			assertThat(saved.getHistoryKey()).contains(PaymentEventType.CANCEL_FAILED.name());
		}
	}
}
