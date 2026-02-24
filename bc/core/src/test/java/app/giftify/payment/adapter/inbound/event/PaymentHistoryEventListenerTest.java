package app.giftify.payment.adapter.inbound.event;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

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
import app.giftify.shared.domain.event.payment.PaymentCancelFailedEvent;
import app.giftify.shared.domain.event.payment.PaymentCanceledEvent;
import app.giftify.shared.domain.event.payment.PaymentEventData;
import app.giftify.shared.domain.event.payment.PaymentFailedEvent;
import app.giftify.shared.domain.event.payment.PaymentSucceededEvent;
import app.giftify.shared.domain.type.CancelType;
import app.giftify.shared.domain.type.PaymentMethod;
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

	@Nested
	@DisplayName("onPaymentSucceeded")
	class OnPaymentSucceededTests {

		@Test
		@DisplayName("PaymentSucceededEvent를 받으면 PAID 히스토리를 저장한다")
		void savesHistory() {
			PaymentSucceededEvent event = PaymentSucceededEvent.create(
				PaymentEventData.forSuccess(PAYMENT_ID, 100L, 10L, ORDER_NUMBER,
					Money.of(10000), PaymentMethod.CARD, PaymentType.FUNDING, "pk", "txn"));

			listener.onPaymentSucceeded(event);

			ArgumentCaptor<JpaPaymentHistory> captor = ArgumentCaptor.forClass(JpaPaymentHistory.class);
			verify(historyRepository).save(captor.capture());

			JpaPaymentHistory saved = captor.getValue();
			assertThat(saved.getPaymentId()).isEqualTo(PAYMENT_ID);
			assertThat(saved.getEventType()).isEqualTo(PaymentEventType.PAID);
			assertThat(saved.getOccurredAt()).isNotNull();
			assertThat(saved.getHistoryKey()).contains(ORDER_NUMBER)
				.contains(PaymentEventType.PAID.name());
			assertThat(saved.getMetadata()).isNull();
		}
	}

	@Nested
	@DisplayName("onPaymentFailed")
	class OnPaymentFailedTests {

		@Test
		@DisplayName("PaymentFailedEvent를 받으면 FAILED 히스토리를 저장한다")
		void savesHistory() {
			PaymentFailedEvent event = PaymentFailedEvent.create(
				PaymentEventData.forFailure(PAYMENT_ID, 100L, 10L, ORDER_NUMBER,
					Money.of(10000), PaymentMethod.CARD, PaymentType.FUNDING));

			listener.onPaymentFailed(event);

			ArgumentCaptor<JpaPaymentHistory> captor = ArgumentCaptor.forClass(JpaPaymentHistory.class);
			verify(historyRepository).save(captor.capture());

			JpaPaymentHistory saved = captor.getValue();
			assertThat(saved.getPaymentId()).isEqualTo(PAYMENT_ID);
			assertThat(saved.getEventType()).isEqualTo(PaymentEventType.FAILED);
		}
	}

	@Nested
	@DisplayName("onPaymentCancelSucceeded")
	class OnPaymentCancelSucceededTests {

		@Test
		@DisplayName("PaymentCanceledEvent를 받으면 CANCELED 히스토리를 저장한다")
		void savesHistory() {
			PaymentCanceledEvent event = PaymentCanceledEvent.create(
				PaymentEventData.forCancel(PAYMENT_ID, 100L, 10L, ORDER_NUMBER,
					Money.of(10000), PaymentMethod.CARD, PaymentType.FUNDING,
					CancelType.CANCEL, "사용자 요청", "txn-cancel-001"));

			listener.onPaymentCancelSucceeded(event);

			ArgumentCaptor<JpaPaymentHistory> captor = ArgumentCaptor.forClass(JpaPaymentHistory.class);
			verify(historyRepository).save(captor.capture());

			JpaPaymentHistory saved = captor.getValue();
			assertThat(saved.getPaymentId()).isEqualTo(PAYMENT_ID);
			assertThat(saved.getEventType()).isEqualTo(PaymentEventType.CANCELED);
			assertThat(saved.getHistoryKey()).contains(PaymentEventType.CANCELED.name());
		}
	}

	@Nested
	@DisplayName("onPaymentCancelFailed")
	class OnPaymentCancelFailedTests {

		@Test
		@DisplayName("PaymentCancelFailedEvent를 받으면 CANCEL_FAILED 히스토리와 metadata를 저장한다")
		void savesHistoryWithMetadata() {
			String errorMetadata = "{\"code\":\"PG_ERROR\"}";
			PaymentCancelFailedEvent event = PaymentCancelFailedEvent.create(
				PaymentEventData.forCancelFailed(PAYMENT_ID, 100L, 10L, ORDER_NUMBER,
					PaymentMethod.CARD, PaymentType.FUNDING, errorMetadata));

			listener.onPaymentCancelFailed(event);

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
