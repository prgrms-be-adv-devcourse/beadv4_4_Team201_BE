package domain.payment;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import app.giftify.shared.domain.event.payment.PaymentType;
import app.giftify.shared.domain.vo.Money;

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
		assertThat(payment.getUncommittedHistory().getFirst().eventType()).isEqualTo(PaymentEventType.CREATED);
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
			.isInstanceOf(PaymentException.class)
			.hasMessageContaining("PENDING");
	}

	@Test
	@DisplayName("PENDING 상태에서는 취소가 가능하고 CANCELED 이력이 생성된다")
	void cancel_ShouldSucceed_WhenPending() {
		// Given
		Payment payment = Payment.create(1L, PaymentType.CHARGE, Money.of(10000L), PaymentMethod.GIFTIFY_CASH);

		// When
		PaymentHistory history = payment.cancel();

		// Then
		assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELED);
		assertThat(history.eventType()).isEqualTo(PaymentEventType.CANCELED);
		assertThat(payment.getUncommittedHistory()).hasSize(2); // CREATED + CANCELED
	}

	@Test
	@DisplayName("PAID 상태에서는 취소할 수 없고 예외가 발생한다")
	void cancel_ShouldFail_WhenAlreadyPaid() {
		// Given
		Payment payment = Payment.create(1L, PaymentType.CHARGE, Money.of(10000L), PaymentMethod.GIFTIFY_CASH);
		payment.markAsPaid("TX_123");

		// When & Then
		assertThatThrownBy(payment::cancel)
			.isInstanceOf(PaymentException.class)
			.hasMessageContaining("취소 불가능한 상태");
	}

	@Test
	@DisplayName("PAID 상태에서는 환불이 가능하고 REFUNDED 이력이 생성된다")
	void refund_ShouldSucceed_WhenPaid() {
		// Given
		Payment payment = Payment.create(1L, PaymentType.CHARGE, Money.of(10000L), PaymentMethod.GIFTIFY_CASH);
		payment.markAsPaid("TX_123");

		// When
		PaymentHistory history = payment.refund();

		// Then
		assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
		assertThat(history.eventType()).isEqualTo(PaymentEventType.REFUNDED);
	}

	@Test
	@DisplayName("PAID 상태에서만 정산(SETTLED)이 가능하고 그 후에는 환불이 불가하다")
	void settle_ShouldChangeStatusToSettled_AndPreventRefund() {
		// Given
		Payment payment = Payment.create(1L, PaymentType.CHARGE, Money.of(10000L), PaymentMethod.GIFTIFY_CASH);
		payment.markAsPaid("TX_123");

		// When
		payment.settle();

		// Then
		assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SETTLED);

		// 정산 후 환불 시도 시 예외 발생 확인
		assertThatThrownBy(payment::refund)
			.isInstanceOf(PaymentException.class)
			.hasMessageContaining("이미 정산(수령) 처리되어 환불할 수 없습니다");
	}

	@Test
	@DisplayName("PENDING 상태에서만 실패(FAILED) 처리가 가능하다")
	void markAsFailed_ShouldSucceed_WhenPending() {
		// Given
		Payment payment = Payment.create(1L, PaymentType.CHARGE, Money.of(10000L), PaymentMethod.GIFTIFY_CASH);

		// When
		payment.markAsFailed();

		// Then
		assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
	}

	@Test
	@DisplayName("clearUncommittedHistory 호출 시 누적된 이력이 모두 삭제된다")
	void clearUncommittedHistory_ShouldRemoveAllHistory() {
		// Given
		Payment payment = Payment.create(1L, PaymentType.CHARGE, Money.of(10000L), PaymentMethod.GIFTIFY_CASH);
		payment.markAsPaid("TX_123");
		assertThat(payment.getUncommittedHistory()).hasSize(2);

		// When
		payment.clearUncommittedHistory();

		// Then
		assertThat(payment.getUncommittedHistory()).isEmpty();
	}

	@Test
	@DisplayName("펀딩 결제 생성 시 walletUsedAmount가 저장되고 FUNDING 타입이어야 한다")
	void createForFunding_ShouldStoreWalletUsedAmount_AndBeTypeFunding() {
		// Given
		Long userId = 1L;
		Money pgAmount = Money.of(20000L);
		Money walletUsedAmount = Money.of(30000L);

		// When
		Payment payment = Payment.createForFunding(userId, pgAmount, walletUsedAmount);

		// Then
		assertThat(payment.getType()).isEqualTo(PaymentType.FUNDING);
		assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
		assertThat(payment.getAmount()).isEqualTo(pgAmount);
		assertThat(payment.getWalletUsedAmount()).isEqualTo(walletUsedAmount);
		assertThat(payment.getOrderId()).startsWith("GFTFY_FUNDING_");
		assertThat(payment.getUncommittedHistory()).hasSize(1);
		assertThat(payment.getUncommittedHistory().getFirst().eventType()).isEqualTo(PaymentEventType.CREATED);
	}

	@Test
	@DisplayName("펀딩 결제는 walletUsedAmount가 0원이어도 저장된다")
	void createForFunding_ShouldStoreZeroWalletUsedAmount() {
		// Given
		Long userId = 1L;
		Money pgAmount = Money.of(50000L);
		Money walletUsedAmount = Money.zero();

		// When
		Payment payment = Payment.createForFunding(userId, pgAmount, walletUsedAmount);

		// Then
		assertThat(payment.getWalletUsedAmount()).isEqualTo(Money.zero());
	}
}
