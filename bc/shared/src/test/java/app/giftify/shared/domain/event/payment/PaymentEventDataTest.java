package app.giftify.shared.domain.event.payment;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import app.giftify.shared.domain.type.CancelType;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

@DisplayName("PaymentEventData")
class PaymentEventDataTest {

	@Test
	@DisplayName("결제 성공 데이터 생성 - 전용 필드 확인")
	void successData() {
		PaymentSuccessData data = new PaymentSuccessData(
			1L, 100L, 10L, "ORD-001", Money.of(10000),
			PaymentMethod.CARD, PaymentType.FUNDING,
			"pk_test", "txn_test"
		);

		assertThat(data.paymentId()).isEqualTo(1L);
		assertThat(data.orderId()).isEqualTo(100L);
		assertThat(data.memberId()).isEqualTo(10L);
		assertThat(data.orderNumber()).isEqualTo("ORD-001");
		assertThat(data.paidAmount()).isEqualTo(Money.of(10000));
		assertThat(data.paymentKey()).isEqualTo("pk_test");
		assertThat(data).isInstanceOf(PaymentEventData.class);
	}

	@Test
	@DisplayName("취소 데이터 생성 - cancelType, reason 포함")
	void cancelData() {
		PaymentCancelData data = new PaymentCancelData(
			1L, 100L, 10L, "ORD-001", Money.of(10000), Money.zero(),
			PaymentMethod.CARD, PaymentType.FUNDING,
			CancelType.REFUND, "고객 요청", "txn-key-001"
		);

		assertThat(data.cancelType()).isEqualTo(CancelType.REFUND);
		assertThat(data.reason()).isEqualTo("고객 요청");
		assertThat(data).isInstanceOf(PaymentEventData.class);
	}
}
