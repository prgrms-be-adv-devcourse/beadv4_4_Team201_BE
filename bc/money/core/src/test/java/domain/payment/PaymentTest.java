package domain.payment;

import app.giftify.shared.domain.event.payment.PaymentType;
import app.giftify.shared.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentTest {

	@Test
	@DisplayName("결제 생성 시 초기 상태는 PENDING이고 CREATED 이력이 생성되어야 한다")
	void create_ShouldReturnPendingPayment_WithCreatedHistory() {
		// Given
		Long userId = 1L;
		Money amount = Money.of(10000L);

		// When
		Payment payment = Payment.create(
			userId,
			PaymentType.CHARGE,
			amount,
			PaymentMethod.GIFTIFY_CASH
		);

		// Then
		assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
		assertThat(payment.getUncommittedHistory()).hasSize(1);
		assertThat(payment.getUncommittedHistory().get(0).eventType()).isEqualTo(PaymentEventType.CREATED);
	}

	@Test
	@DisplayName("결제 완료(markAsPaid) 시 PAID 상태로 변경되고 PAID 이력이 생성되어야 한다")
	void markAsPaid_ShouldChangeStatusToPaid_AndAddHistory() {
		// Given
		Payment payment = Payment.create(1L, PaymentType.CHARGE, Money.of(10000L), PaymentMethod.GIFTIFY_CASH);
		String pgTxId = "TX_12345";

		// When
		PaymentHistory history = payment.markAsPaid(pgTxId);

		// Then
		assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
		assertThat(payment.getPgTransactionId()).isEqualTo(pgTxId);
		
		// History 검증
		assertThat(payment.getUncommittedHistory()).hasSize(2); // CREATED + PAID
		assertThat(history.eventType()).isEqualTo(PaymentEventType.PAID);
		assertThat(history.metadata()).contains(pgTxId);
		assertThat(history.occurredAt()).isNotNull();
	}

	@Test
	@DisplayName("PENDING 상태가 아니면 markAsPaid는 실패해야 한다")
	void markAsPaid_ShouldFail_WhenNotPending() {
		// Given
		Payment payment = Payment.create(1L, PaymentType.CHARGE, Money.of(10000L), PaymentMethod.GIFTIFY_CASH);
		payment.markAsPaid("TX_1"); // 이미 PAID 상태

		// When & Then
		assertThatThrownBy(() -> payment.markAsPaid("TX_2"))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("PENDING");
	}
}