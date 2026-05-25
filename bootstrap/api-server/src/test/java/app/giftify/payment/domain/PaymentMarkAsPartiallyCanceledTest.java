package app.giftify.payment.domain;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import app.giftify.payment.domain.event.PaymentDomainEvent;
import app.giftify.payment.domain.type.CancelType;
import app.giftify.payment.domain.type.PaymentMethod;
import app.giftify.payment.domain.type.PaymentType;
import app.giftify.support.common.money.Money;

@DisplayName("Payment.partialCancel() 테스트")
class PaymentMarkAsPartiallyCanceledTest {

	private Payment createPaidPayment(Long paymentId, Money paidAmount) {
		return Payment.builder()
			.id(paymentId)
			.orderId(123L)
			.orderNumber("order-123")
			.memberId(100L)
			.type(PaymentType.DEPOSIT_CHARGE)
			.method(PaymentMethod.CARD)
			.originAmount(paidAmount)
			.paidAmount(paidAmount)
			.refundedAmount(Money.zero())
			.status(PaymentStatus.PAID)
			.paymentKey("payment-key-123")
			.lastTransactionKey("txn-001")
			.approveCode("approve-001")
			.paidAt(LocalDateTime.now())
			.createdAt(LocalDateTime.now())
			.build();
	}

	private Payment createPartiallyCanceledPayment(Long paymentId, Money paidAmount, Money refundedAmount) {
		return Payment.builder()
			.id(paymentId)
			.orderId(123L)
			.orderNumber("order-123")
			.memberId(100L)
			.type(PaymentType.DEPOSIT_CHARGE)
			.method(PaymentMethod.CARD)
			.originAmount(paidAmount)
			.paidAmount(paidAmount)
			.refundedAmount(refundedAmount)
			.status(PaymentStatus.PARTIALLY_CANCELED)
			.paymentKey("payment-key-123")
			.lastTransactionKey("txn-cancel-001")
			.approveCode("approve-001")
			.paidAt(LocalDateTime.now())
			.createdAt(LocalDateTime.now())
			.build();
	}

	private Payment createPendingPayment(Long paymentId) {
		return Payment.builder()
			.id(paymentId)
			.orderId(123L)
			.orderNumber("order-123")
			.memberId(100L)
			.type(PaymentType.DEPOSIT_CHARGE)
			.method(PaymentMethod.CARD)
			.originAmount(Money.of(10000))
			.paidAmount(Money.of(10000))
			.refundedAmount(Money.zero())
			.status(PaymentStatus.PENDING)
			.build();
	}

	@Nested
	@DisplayName("부분 취소 시나리오")
	class PartialCancelScenarios {

		@Test
		@DisplayName("PAID 상태에서 부분 취소 → PARTIALLY_CANCELED 상태로 변경")
		void firstPartialCancel_FromPaid_ToPartiallyCanceled() {
			// given
			Payment payment = createPaidPayment(1L, Money.of(10000));
			Money cancelAmount = Money.of(3000);
			String newTransactionKey = "txn-cancel-001";
			String reason = "부분 환불 요청";

			// when
			Payment result = payment.partialCancel(newTransactionKey, cancelAmount, CancelType.REFUND, reason);

			// then
			assertThat(result.getStatus()).isEqualTo(PaymentStatus.PARTIALLY_CANCELED);
			assertThat(result.getRefundedAmount()).isEqualTo(Money.of(3000));
			assertThat(result.getLastTransactionKey()).isEqualTo(newTransactionKey);

			List<Object> events = result.pullEvents();
			assertThat(events).hasSize(1);
			assertThat(events.get(0)).isInstanceOf(PaymentDomainEvent.PartialCanceled.class);

			PaymentDomainEvent.PartialCanceled event = (PaymentDomainEvent.PartialCanceled) events.get(0);
			assertThat(event.cancelAmount()).isEqualTo(cancelAmount);
			assertThat(event.lastTransactionKey()).isEqualTo(newTransactionKey);
			assertThat(event.reason()).isEqualTo(reason);
		}

		@Test
		@DisplayName("PAID 상태에서 전액 취소(partialCancel 사용) → CANCELED 상태로 변경")
		void fullCancelViaPartialCancel_FromPaid_ToCanceled() {
			// given
			Payment payment = createPaidPayment(1L, Money.of(10000));
			Money cancelAmount = Money.of(10000);
			String newTransactionKey = "txn-cancel-full";
			String reason = "전액 환불";

			// when
			Payment result = payment.partialCancel(newTransactionKey, cancelAmount, CancelType.REFUND, reason);

			// then
			assertThat(result.getStatus()).isEqualTo(PaymentStatus.CANCELED);
			assertThat(result.getRefundedAmount()).isEqualTo(Money.of(10000));
			assertThat(result.getLastTransactionKey()).isEqualTo(newTransactionKey);
		}

		@Test
		@DisplayName("PARTIALLY_CANCELED 상태에서 추가 부분 취소 → PARTIALLY_CANCELED 유지")
		void consecutivePartialCancel_FromPartiallyCanceled_ToPartiallyCanceled() {
			// given
			Payment payment = createPartiallyCanceledPayment(1L, Money.of(10000), Money.of(3000));
			Money additionalCancelAmount = Money.of(2000);
			String newTransactionKey = "txn-cancel-002";
			String reason = "추가 부분 환불";

			// when
			Payment result = payment.partialCancel(newTransactionKey, additionalCancelAmount, CancelType.REFUND, reason);

			// then
			assertThat(result.getStatus()).isEqualTo(PaymentStatus.PARTIALLY_CANCELED);
			assertThat(result.getRefundedAmount()).isEqualTo(Money.of(5000));
			assertThat(result.getLastTransactionKey()).isEqualTo(newTransactionKey);

			List<Object> events = result.pullEvents();
			assertThat(events).hasSize(1);
			PaymentDomainEvent.PartialCanceled event = (PaymentDomainEvent.PartialCanceled) events.get(0);
			assertThat(event.cancelAmount()).isEqualTo(additionalCancelAmount);
		}

		@Test
		@DisplayName("PARTIALLY_CANCELED 상태에서 최종 취소 → CANCELED 상태로 변경")
		void finalCancel_FromPartiallyCanceled_ToCanceled() {
			// given
			Payment payment = createPartiallyCanceledPayment(1L, Money.of(10000), Money.of(7000));
			Money finalCancelAmount = Money.of(3000);
			String newTransactionKey = "txn-cancel-final";
			String reason = "최종 취소";

			// when
			Payment result = payment.partialCancel(newTransactionKey, finalCancelAmount, CancelType.REFUND, reason);

			// then
			assertThat(result.getStatus()).isEqualTo(PaymentStatus.CANCELED);
			assertThat(result.getRefundedAmount()).isEqualTo(Money.of(10000));
			assertThat(result.getLastTransactionKey()).isEqualTo(newTransactionKey);
		}
	}

	@Nested
	@DisplayName("예외 시나리오")
	class ExceptionScenarios {

		@Test
		@DisplayName("취소 금액이 결제 금액을 초과하면 예외 발생")
		void cancelAmountExceedsPaidAmount_ThrowsException() {
			// given
			Payment payment = createPaidPayment(1L, Money.of(10000));
			Money excessiveCancelAmount = Money.of(15000);

			// when & then
			assertThatThrownBy(() ->
				payment.partialCancel("txn-excessive", excessiveCancelAmount, CancelType.REFUND, "초과 환불")
			)
				.isInstanceOf(PaymentException.class)
				.extracting("errorCode")
				.isEqualTo(PaymentErrorCode.CANCEL_AMOUNT_EXCEEDED);
		}

		@Test
		@DisplayName("PARTIALLY_CANCELED 상태에서 누적 취소 금액이 결제 금액을 초과하면 예외 발생")
		void cumulativeCancelAmountExceedsPaidAmount_ThrowsException() {
			// given
			Payment payment = createPartiallyCanceledPayment(1L, Money.of(10000), Money.of(7000));
			Money excessiveCancelAmount = Money.of(5000);

			// when & then
			assertThatThrownBy(() ->
				payment.partialCancel("txn-excessive", excessiveCancelAmount, CancelType.REFUND, "초과 환불")
			)
				.isInstanceOf(PaymentException.class)
				.extracting("errorCode")
				.isEqualTo(PaymentErrorCode.CANCEL_AMOUNT_EXCEEDED);
		}

		@Test
		@DisplayName("PENDING 상태에서 부분 취소 시도 시 예외 발생")
		void cancelFromPendingStatus_ThrowsException() {
			// given
			Payment payment = createPendingPayment(1L);
			Money cancelAmount = Money.of(3000);

			// when & then
			assertThatThrownBy(() ->
				payment.partialCancel("txn-invalid", cancelAmount, CancelType.REFUND, "대기 중 취소")
			)
				.isInstanceOf(PaymentException.class)
				.extracting("errorCode")
				.isEqualTo(PaymentErrorCode.NOT_CANCELABLE);
		}
	}

	@Nested
	@DisplayName("이벤트 검증")
	class EventValidation {

		@Test
		@DisplayName("이벤트에 취소 금액과 transactionKey가 올바르게 포함됨")
		void event_ContainsCorrectCancelAmountAndTransactionKey() {
			// given
			Payment payment = createPaidPayment(1L, Money.of(10000));
			Money cancelAmount = Money.of(4000);
			String transactionKey = "txn-test-123";
			String reason = "환불 테스트";

			// when
			Payment result = payment.partialCancel(transactionKey, cancelAmount, CancelType.REFUND, reason);

			// then
			List<Object> events = result.pullEvents();
			assertThat(events).hasSize(1);

			PaymentDomainEvent.PartialCanceled event = (PaymentDomainEvent.PartialCanceled) events.get(0);
			assertThat(event.paymentId()).isEqualTo(1L);
			assertThat(event.cancelAmount()).isEqualTo(cancelAmount);
			assertThat(event.lastTransactionKey()).isEqualTo(transactionKey);
			assertThat(event.reason()).isEqualTo(reason);
			assertThat(event.method()).isEqualTo(PaymentMethod.CARD);
			assertThat(event.type()).isEqualTo(PaymentType.DEPOSIT_CHARGE);
		}
	}
}
