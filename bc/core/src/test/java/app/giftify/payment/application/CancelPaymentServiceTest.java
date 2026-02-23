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

import app.giftify.payment.application.inbound.CancelPaymentCommand;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.payment.PaymentCanceledEvent;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

@ExtendWith(MockitoExtension.class)
@DisplayName("CancelPaymentService 테스트")
class CancelPaymentServiceTest {

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private EventPublisher eventPublisher;

	@InjectMocks
	private CancelPaymentService cancelPaymentService;

	// DEPOSIT_CHARGE(예치금 충전)는 orderItems가 불필요하므로 테스트에 적합
	private Payment createPendingPayment(Long paymentId, Long memberId, String orderNumber) {
		return Payment.builder()
			.id(paymentId)
			.orderId(123L)
			.orderNumber(orderNumber)
			.memberId(memberId)
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
			.type(PaymentType.DEPOSIT_CHARGE)
			.method(PaymentMethod.CARD)
			.originAmount(Money.of(10000))
			.paidAmount(Money.of(10000))
			.orderItems(List.of())
			.status(PaymentStatus.PAID)
			.paidAt(LocalDateTime.now())
			.build();
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
			CancelPaymentCommand command = new CancelPaymentCommand(paymentId, memberId, "고객 요청");

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
			CancelPaymentCommand command = new CancelPaymentCommand(paymentId, SYSTEM_REQUESTER_ID, "시스템 자동 취소");

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
		@DisplayName("결제가 존재하지 않으면 예외가 발생한다")
		void cancel_PaymentNotFound_ThrowsException() {
			// given
			Long paymentId = 999L;
			Long requesterId = 100L;
			CancelPaymentCommand command = new CancelPaymentCommand(paymentId, requesterId, "고객 요청");

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
			CancelPaymentCommand command = new CancelPaymentCommand(paymentId, unauthorizedUserId, "고객 요청");

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
		@DisplayName("취소 불가능한 상태(PAID)이면 예외가 발생한다")
		void cancel_NotCancelableStatus_ThrowsException() {
			// given
			Long paymentId = 1L;
			Long memberId = 100L;
			CancelPaymentCommand command = new CancelPaymentCommand(paymentId, memberId, "고객 요청");

			Payment payment = createPaidPayment(paymentId, memberId, "order-123");

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

			// when & then
			assertThatThrownBy(() -> cancelPaymentService.cancel(command))
				.isInstanceOf(PaymentException.class)
				.extracting("errorCode")
				.isEqualTo(PaymentErrorCode.NOT_CANCELABLE);

			verify(paymentRepository).findById(paymentId);
		}

		@Test
		@DisplayName("취소 사유가 없어도 정상 처리된다")
		void cancel_WithoutReason_Success() {
			// given
			Long paymentId = 1L;
			Long memberId = 100L;
			CancelPaymentCommand command = new CancelPaymentCommand(paymentId, memberId, null);

			Payment payment = createPendingPayment(paymentId, memberId, "order-123");

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(paymentRepository.save(any(Payment.class))).willAnswer(inv -> inv.getArgument(0));

			// when
			cancelPaymentService.cancel(command);

			// then
			verify(paymentRepository).save(any(Payment.class));

			ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
			verify(eventPublisher).publish(eventCaptor.capture());
			PaymentCanceledEvent event = (PaymentCanceledEvent) eventCaptor.getValue();

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
			CancelPaymentCommand command = new CancelPaymentCommand(paymentId, memberId, reason);

			Payment payment = createPendingPayment(paymentId, memberId, orderNumber);

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(paymentRepository.save(any(Payment.class))).willAnswer(inv -> inv.getArgument(0));

			// when
			cancelPaymentService.cancel(command);

			// then
			ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
			verify(eventPublisher).publish(eventCaptor.capture());
			PaymentCanceledEvent event = (PaymentCanceledEvent) eventCaptor.getValue();

			assertThat(event.data().paymentId()).isEqualTo(paymentId);
			assertThat(event.data().memberId()).isEqualTo(memberId);
			assertThat(event.data().orderNumber()).isEqualTo(orderNumber);
			assertThat(event.data().paymentType()).isEqualTo(PaymentType.DEPOSIT_CHARGE);
			assertThat(event.data().amount()).isEqualTo(Money.of(10000));
			assertThat(event.data().reason()).isEqualTo(reason);
			assertThat(event.time()).isNotNull();
		}

	}
}
