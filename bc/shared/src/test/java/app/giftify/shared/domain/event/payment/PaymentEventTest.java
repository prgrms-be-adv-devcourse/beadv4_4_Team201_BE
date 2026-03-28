package app.giftify.shared.domain.event.payment;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import app.giftify.shared.domain.type.CancelType;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

@DisplayName("PaymentEvent 통합 이벤트")
class PaymentEventTest {

	@Test
	@DisplayName("PaymentSucceededEvent - CloudEvents 메타데이터 포함")
	void succeededEvent() {
		PaymentEventData data = PaymentEventData.forSuccess(
			1L, 100L, 10L, "ORD-001", Money.of(10000),
			PaymentMethod.CARD, PaymentType.FUNDING, "pk_test", "txn_test"
		);

		PaymentSucceededEvent event = PaymentSucceededEvent.create(data);

		assertThat(event.id()).isNotNull();
		assertThat(event.source()).isEqualTo("payment");
		assertThat(event.type()).isEqualTo("payment.succeeded");
		assertThat(event.time()).isNotNull();
		assertThat(event.data().paymentId()).isEqualTo(1L);
		assertThat(event.data().orderId()).isEqualTo(100L);
		assertThat(event).isInstanceOf(PaymentEvent.class);
	}

	@Test
	@DisplayName("PaymentFailedEvent")
	void failedEvent() {
		PaymentEventData data = PaymentEventData.forFailure(
			1L, 100L, 10L, "ORD-001", Money.of(10000), Money.zero(),
			PaymentMethod.CARD, PaymentType.FUNDING
		);

		PaymentFailedEvent event = PaymentFailedEvent.create(data);

		assertThat(event.type()).isEqualTo("payment.failed");
		assertThat(event).isInstanceOf(PaymentEvent.class);
	}

	@Test
	@DisplayName("PaymentCanceledEvent - cancelType 포함")
	void cancelSucceededEvent() {
		PaymentEventData data = PaymentEventData.forCancel(
			1L, 100L, 10L, "ORD-001", Money.of(10000), Money.zero(),
			PaymentMethod.CARD, PaymentType.FUNDING,
			CancelType.REFUND, "고객 요청", "txn-key-001"
		);

		PaymentCanceledEvent event = PaymentCanceledEvent.create(data);

		assertThat(event.type()).isEqualTo("payment.canceled");
		assertThat(event.data().cancelType()).isEqualTo(CancelType.REFUND);
		assertThat(event).isInstanceOf(PaymentEvent.class);
	}

	@Test
	@DisplayName("PaymentCancelFailedEvent")
	void cancelFailedEvent() {
		PaymentEventData data = PaymentEventData.forCancelFailed(
			1L, 100L, 10L, "ORD-001",
			PaymentMethod.CARD, PaymentType.FUNDING, "{\"code\":\"PG_ERROR\"}"
		);

		PaymentCancelFailedEvent event = PaymentCancelFailedEvent.create(data);

		assertThat(event.type()).isEqualTo("payment.cancel.failed");
		assertThat(event).isInstanceOf(PaymentEvent.class);
	}
}
