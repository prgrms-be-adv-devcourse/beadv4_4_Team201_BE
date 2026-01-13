package domain.payment;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import vo.Money;

class PaymentTest {

	@Test
	@DisplayName("펀딩 결제 생성 시 PAID 상태와 GIFTIFY_CASH 수단으로 생성되어야 한다")
	void createPaidForFunding_ShouldCreatePaymentWithPaidStatus() {
		// given
		Long userId = 1L;
		Long fundingId = 100L;
		Money amount = Money.of(15000);

		// when
		Payment payment = Payment.createPaidForFunding(userId, fundingId, amount);

		// then
		
		// 1. 환불 가능해야 함 (PAID 상태이고 아직 환불 안 됨)
		assertThat(payment.isRefundable()).isTrue();
		
		// 2. 취소 불가능해야 함 (PENDING이 아니므로)
		assertThat(payment.isCancelable()).isFalse();
	}

	@Test
	@DisplayName("환불이 성공적으로 수행되어야 한다")
	void refund_ShouldSucceed_WhenStatusIsPaid() {
		Payment payment = Payment.createPaidForFunding(1L, 100L, Money.of(10000));

		payment.refund();

		assertThat(payment.isRefundable()).isFalse(); // 이미 환불되었으므로 false
	}

	@Test
	@DisplayName("이미 환불된 결제를 다시 환불하려 하면 예외가 발생해야 한다")
	void refund_ShouldThrowException_WhenAlreadyRefunded() {
		Payment payment = Payment.createPaidForFunding(1L, 100L, Money.of(10000));
		payment.refund(); // 1차 환불

		assertThatThrownBy(payment::refund)
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("환불 불가능한 상태입니다");
	}

	@Test
	@DisplayName("완료된 펀딩 결제는 취소(Cancel)할 수 없다")
	void cancel_ShouldThrowException_WhenStatusIsPaid() {
		// given
		// createPaidForFunding은 PAID 상태로 생성됨
		Payment payment = Payment.createPaidForFunding(1L, 100L, Money.of(10000));

		// when & then
		// Cancel은 PENDING 상태에서만 가능하므로 예외 발생해야 함
		assertThatThrownBy(payment::cancel)
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("취소 불가능한 상태입니다");
	}

	@Test
	@DisplayName("펀딩 수령 확정(Settled) 이후에는 환불할 수 없다")
	void refund_ShouldFail_WhenPaymentIsSettled() {
		// given
		Payment payment = Payment.createPaidForFunding(1L, 100L, Money.of(10000));

		// when: 선물 수령 처리
		payment.settle();

		// then: 환불 시도 시 예외 발생
		assertThatThrownBy(payment::refund)
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("이미 수령 처리되어 환불할 수 없습니다");

		// 상태 확인
		assertThat(payment.isRefundable()).isFalse();
	}
}
