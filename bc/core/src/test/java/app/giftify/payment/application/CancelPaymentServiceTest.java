package app.giftify.payment.application;

import static app.giftify.payment.domain.SystemConstants.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.payment.adapter.outbound.pg.TossCancelResult;
import app.giftify.payment.application.inbound.CancelPaymentCommand;
import app.giftify.payment.application.outbound.CancelRepository;
import app.giftify.payment.application.outbound.PaymentFieldEncryptor;
import app.giftify.payment.application.outbound.PaymentGateway;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.Cancel;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.payment.PaymentCancelFailedEvent;
import app.giftify.shared.domain.event.payment.PaymentCanceledEvent;
import app.giftify.shared.domain.type.CancelType;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

@ExtendWith(MockitoExtension.class)
@DisplayName("CancelPaymentService 테스트")
class CancelPaymentServiceTest {

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private CancelRepository cancelRepository;

	@Mock
	private EventPublisher eventPublisher;

	@Mock
	private PaymentGateway paymentGateway;

	@Mock
	private PaymentFieldEncryptor encryptor;

	@InjectMocks
	private CancelPaymentService cancelPaymentService;

	// DEPOSIT_CHARGE(예치금 충전)는 orderItems가 불필요하므로 테스트에 적합
	private Payment createPendingPayment(Long paymentId, Long memberId, String orderNumber) {
		return Payment.builder()
			.id(paymentId)
			.orderId(123L)
			.orderNumber(orderNumber)
			.memberId(memberId)
			.paymentKey("encrypted-payment-key")
			.type(PaymentType.DEPOSIT_CHARGE)
			.method(PaymentMethod.CARD)
			.originAmount(Money.of(10000))
			.paidAmount(Money.of(10000))
			.orderItems(List.of())
			.status(PaymentStatus.PENDING)
			.build();
	}

	private Payment createPaidPayment(Long paymentId, Long memberId, String orderNumber) {
		return Payment.builder()
			.id(paymentId)
			.orderId(123L)
			.orderNumber(orderNumber)
			.memberId(memberId)
			.paymentKey("encrypted-payment-key")
			.type(PaymentType.DEPOSIT_CHARGE)
			.method(PaymentMethod.CARD)
			.originAmount(Money.of(10000))
			.paidAmount(Money.of(10000))
			.orderItems(List.of())
			.status(PaymentStatus.PAID)
			.paidAt(LocalDateTime.now())
			.build();
	}

	private Payment createCanceledPayment(Long paymentId, Long memberId, String orderNumber) {
		Payment payment = createPendingPayment(paymentId, memberId, orderNumber);
		Payment canceled = payment.cancel(CancelType.CANCEL, "이전 취소");
		canceled.pullEvents();
		return canceled;
	}

	@Nested
	@DisplayName("cancel 메서드")
	class CancelTests {

		@Test
		@DisplayName("결제 소유자가 취소를 요청하면 성공한다")
		void cancel_ByOwner_Success() {
			// given
			Long paymentId = 1L;
			Long memberId = 100L;
			String orderId = "order-123";
			CancelPaymentCommand command = CancelPaymentCommand.full(paymentId, memberId, "고객 요청");

			Payment payment = createPendingPayment(paymentId, memberId, orderId);

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(paymentRepository.save(any(Payment.class))).willAnswer(inv -> inv.getArgument(0));

			// when
			cancelPaymentService.cancel(command);

			// then
			verify(paymentRepository).findById(paymentId);
			verify(paymentRepository).save(any(Payment.class));
			verify(eventPublisher).publish(any());
		}

		@Test
		@DisplayName("시스템 사용자가 취소를 요청하면 권한 검증을 스킵하고 성공한다")
		void cancel_BySystemUser_Success() {
			// given
			Long paymentId = 1L;
			Long actualOwnerId = 100L;
			String orderId = "order-123";
			CancelPaymentCommand command = CancelPaymentCommand.full(paymentId, SYSTEM_REQUESTER_ID, "시스템 자동 취소");

			Payment payment = createPendingPayment(paymentId, actualOwnerId, orderId);

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(paymentRepository.save(any(Payment.class))).willAnswer(inv -> inv.getArgument(0));

			// when
			cancelPaymentService.cancel(command);

			// then
			verify(paymentRepository).findById(paymentId);
			verify(paymentRepository).save(any(Payment.class));
			verify(eventPublisher).publish(any());
		}

		@Test
		@DisplayName("PAID 상태 결제 전액 취소 시 PG 취소 + Cancel 이력을 생성한다")
		void cancel_WhenPaid_CallsPgCancelAndCreatesCancelRecord() {
			// given
			Long paymentId = 1L;
			Long memberId = 100L;
			Payment payment = createPaidPayment(paymentId, memberId, "order-123");
			CancelPaymentCommand command = CancelPaymentCommand.full(paymentId, memberId, "고객 변심");

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(encryptor.decrypt("encrypted-payment-key")).willReturn("raw-payment-key");
			given(paymentGateway.cancel(eq("raw-payment-key"), eq("고객 변심"), isNull()))
				.willReturn(TossCancelResult.success("raw-payment-key", "txn-cancel-123", List.of()));
			given(paymentRepository.save(any(Payment.class))).willAnswer(inv -> inv.getArgument(0));

			// when
			cancelPaymentService.cancel(command);

			// then
			verify(paymentGateway).cancel("raw-payment-key", "고객 변심", null);
			verify(paymentRepository).save(any(Payment.class));
			verify(eventPublisher).publish(any(PaymentCanceledEvent.class));

			ArgumentCaptor<Cancel> cancelCaptor = ArgumentCaptor.forClass(Cancel.class);
			verify(cancelRepository).save(cancelCaptor.capture());
			Cancel savedCancel = cancelCaptor.getValue();
			assertThat(savedCancel.getPaymentId()).isEqualTo(paymentId);
			assertThat(savedCancel.getTransactionKey()).isEqualTo("txn-cancel-123");
			assertThat(savedCancel.getCancelAmount()).isEqualTo(Money.of(10000));
			assertThat(savedCancel.getCancelReason()).isEqualTo("고객 변심");
		}

		@Test
		@DisplayName("결제가 존재하지 않으면 예외가 발생한다")
		void cancel_PaymentNotFound_ThrowsException() {
			// given
			Long paymentId = 999L;
			Long requesterId = 100L;
			CancelPaymentCommand command = CancelPaymentCommand.full(paymentId, requesterId, "고객 요청");

			given(paymentRepository.findById(paymentId)).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> cancelPaymentService.cancel(command))
				.isInstanceOf(PaymentException.class)
				.extracting("errorCode")
				.isEqualTo(PaymentErrorCode.PAYMENT_NOT_FOUND);

			verify(paymentRepository).findById(paymentId);
		}

		@Test
		@DisplayName("권한이 없는 사용자가 취소를 요청하면 예외가 발생한다")
		void cancel_UnauthorizedRequester_ThrowsException() {
			// given
			Long paymentId = 1L;
			Long actualOwnerId = 100L;
			Long unauthorizedUserId = 200L;
			CancelPaymentCommand command = CancelPaymentCommand.full(paymentId, unauthorizedUserId, "고객 요청");

			Payment payment = createPendingPayment(paymentId, actualOwnerId, "order-123");

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

			// when & then
			assertThatThrownBy(() -> cancelPaymentService.cancel(command))
				.isInstanceOf(PaymentException.class)
				.extracting("errorCode")
				.isEqualTo(PaymentErrorCode.UNAUTHORIZED_ACCESS);

			verify(paymentRepository).findById(paymentId);
		}

		@Test
		@DisplayName("이미 취소된 결제를 다시 취소하면 예외가 발생한다")
		void cancel_NotCancelableStatus_ThrowsException() {
			// given
			Long paymentId = 1L;
			Long memberId = 100L;
			CancelPaymentCommand command = CancelPaymentCommand.full(paymentId, memberId, "고객 요청");

			Payment payment = createCanceledPayment(paymentId, memberId, "order-123");

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

			// when & then
			assertThatThrownBy(() -> cancelPaymentService.cancel(command))
				.isInstanceOf(PaymentException.class)
				.extracting("errorCode")
				.isEqualTo(PaymentErrorCode.NOT_CANCELABLE);

			verify(paymentRepository).findById(paymentId);
		}

		@Test
		@DisplayName("PAID 상태에서 PG 취소가 실패하면 상태를 유지하고 실패 이벤트를 발행한다")
		void cancel_WhenPgFails_RecordsCancelFailed() {
			// given
			Long paymentId = 1L;
			Long memberId = 100L;
			Payment payment = createPaidPayment(paymentId, memberId, "order-123");
			CancelPaymentCommand command = CancelPaymentCommand.full(paymentId, memberId, "고객 변심");

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(encryptor.decrypt("encrypted-payment-key")).willReturn("raw-payment-key");
			given(paymentGateway.cancel(eq("raw-payment-key"), eq("고객 변심"), isNull()))
				.willReturn(TossCancelResult.failure("ALREADY_CANCELED", "이미 취소된 결제입니다"));
			given(paymentRepository.save(any(Payment.class))).willAnswer(inv -> inv.getArgument(0));

			// when
			cancelPaymentService.cancel(command);

			// then
			verify(paymentGateway).cancel("raw-payment-key", "고객 변심", null);
			verify(paymentRepository).save(any(Payment.class));

			ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
			verify(eventPublisher).publish(eventCaptor.capture());
			assertThat(eventCaptor.getValue()).isInstanceOf(PaymentCancelFailedEvent.class);

			assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
		}

		@Test
		@DisplayName("취소 사유가 없어도 정상 처리된다")
		void cancel_WithoutReason_Success() {
			// given
			Long paymentId = 1L;
			Long memberId = 100L;
			CancelPaymentCommand command = CancelPaymentCommand.full(paymentId, memberId, null);

			Payment payment = createPendingPayment(paymentId, memberId, "order-123");

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(paymentRepository.save(any(Payment.class))).willAnswer(inv -> inv.getArgument(0));

			// when
			cancelPaymentService.cancel(command);

			// then
			verify(paymentRepository).save(any(Payment.class));

			ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
			verify(eventPublisher).publish(eventCaptor.capture());
			PaymentCanceledEvent event = (PaymentCanceledEvent)eventCaptor.getValue();

			assertThat(event.data().reason()).isNull();
		}

		@Test
		@DisplayName("PaymentCanceledEvent가 올바른 정보로 발행된다")
		void cancel_PaymentCanceledEvent_PublishedCorrectly() {
			// given
			Long paymentId = 1L;
			Long memberId = 100L;
			String orderNumber = "order-123";
			String reason = "고객 변심";
			CancelPaymentCommand command = CancelPaymentCommand.full(paymentId, memberId, reason);

			Payment payment = createPendingPayment(paymentId, memberId, orderNumber);

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(paymentRepository.save(any(Payment.class))).willAnswer(inv -> inv.getArgument(0));

			// when
			cancelPaymentService.cancel(command);

			// then
			ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
			verify(eventPublisher).publish(eventCaptor.capture());
			PaymentCanceledEvent event = (PaymentCanceledEvent)eventCaptor.getValue();

			assertThat(event.data().paymentId()).isEqualTo(paymentId);
			assertThat(event.data().memberId()).isEqualTo(memberId);
			assertThat(event.data().orderNumber()).isEqualTo(orderNumber);
			assertThat(event.data().paymentType()).isEqualTo(PaymentType.DEPOSIT_CHARGE);
			assertThat(event.data().cancelAmount()).isEqualTo(Money.of(10000));
			assertThat(event.data().reason()).isEqualTo(reason);
			assertThat(event.time()).isNotNull();
		}

		@Test
		@DisplayName("PENDING 상태 전액 취소 시 Cancel 이력을 생성하지 않는다")
		void cancel_WhenPending_DoesNotCreateCancelRecord() {
			// given
			Long paymentId = 1L;
			Long memberId = 100L;
			CancelPaymentCommand command = CancelPaymentCommand.full(paymentId, memberId, "고객 요청");

			Payment payment = createPendingPayment(paymentId, memberId, "order-123");

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(paymentRepository.save(any(Payment.class))).willAnswer(inv -> inv.getArgument(0));

			// when
			cancelPaymentService.cancel(command);

			// then
			verify(cancelRepository, never()).save(any(Cancel.class));
			verify(paymentRepository).save(any(Payment.class));
			verify(eventPublisher).publish(any(PaymentCanceledEvent.class));
		}

	}

	@Nested
	@DisplayName("부분 취소")
	class PartialCancelTests {

		@Test
		@DisplayName("PAID 상태에서 부분 취소 시 PG 부분 환불 + Cancel 이력을 생성한다")
		void partialCancel_WhenPaid_Success() {
			// given
			Long paymentId = 1L;
			Long memberId = 100L;
			Money cancelAmount = Money.of(3000);
			Payment payment = createPaidPayment(paymentId, memberId, "order-123");
			CancelPaymentCommand command = CancelPaymentCommand.withAmount(paymentId, memberId, "부분 환불", cancelAmount);

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(encryptor.decrypt("encrypted-payment-key")).willReturn("raw-payment-key");
			given(paymentGateway.cancel(eq("raw-payment-key"), eq("부분 환불"), eq(cancelAmount)))
				.willReturn(TossCancelResult.success("raw-payment-key", "txn-partial-123", List.of()));
			given(paymentRepository.save(any(Payment.class))).willAnswer(inv -> inv.getArgument(0));

			// when
			cancelPaymentService.cancel(command);

			// then
			verify(paymentGateway).cancel("raw-payment-key", "부분 환불", cancelAmount);

			ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
			verify(paymentRepository).save(paymentCaptor.capture());
			assertThat(paymentCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.PARTIALLY_CANCELED);

			ArgumentCaptor<Cancel> cancelCaptor = ArgumentCaptor.forClass(Cancel.class);
			verify(cancelRepository).save(cancelCaptor.capture());
			Cancel savedCancel = cancelCaptor.getValue();
			assertThat(savedCancel.getCancelAmount()).isEqualTo(cancelAmount);
			assertThat(savedCancel.getTransactionKey()).isEqualTo("txn-partial-123");

			verify(eventPublisher).publish(any(PaymentCanceledEvent.class));
		}

		@Test
		@DisplayName("PENDING 상태에서 부분 취소를 요청하면 예외가 발생한다")
		void partialCancel_WhenPending_ThrowsException() {
			// given
			Long paymentId = 1L;
			Long memberId = 100L;
			Payment payment = createPendingPayment(paymentId, memberId, "order-123");
			CancelPaymentCommand command = CancelPaymentCommand.withAmount(paymentId, memberId, "부분 환불", Money.of(3000));

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

			// when & then
			assertThatThrownBy(() -> cancelPaymentService.cancel(command))
				.isInstanceOf(PaymentException.class)
				.extracting("errorCode")
				.isEqualTo(PaymentErrorCode.NOT_CANCELABLE);
		}

		@Test
		@DisplayName("부분 취소 PG 실패 시 상태를 유지하고 실패 이벤트를 발행한다")
		void partialCancel_WhenPgFails_RecordsCancelFailed() {
			// given
			Long paymentId = 1L;
			Long memberId = 100L;
			Money cancelAmount = Money.of(3000);
			Payment payment = createPaidPayment(paymentId, memberId, "order-123");
			CancelPaymentCommand command = CancelPaymentCommand.withAmount(paymentId, memberId, "부분 환불", cancelAmount);

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(encryptor.decrypt("encrypted-payment-key")).willReturn("raw-payment-key");
			given(paymentGateway.cancel(eq("raw-payment-key"), eq("부분 환불"), eq(cancelAmount)))
				.willReturn(TossCancelResult.failure("CANCEL_AMOUNT_EXCEEDED", "취소 금액 초과"));
			given(paymentRepository.save(any(Payment.class))).willAnswer(inv -> inv.getArgument(0));

			// when
			cancelPaymentService.cancel(command);

			// then
			assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
			verify(cancelRepository, never()).save(any(Cancel.class));

			ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
			verify(eventPublisher).publish(eventCaptor.capture());
			assertThat(eventCaptor.getValue()).isInstanceOf(PaymentCancelFailedEvent.class);
		}

	}
}
