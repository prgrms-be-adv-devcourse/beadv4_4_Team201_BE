package app.giftify.payment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

import app.giftify.payment.application.inbound.RefundPaymentCommand;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.OrderItemSnapshot;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.payment.domain.event.PaymentRefundedEvent;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.payment.PaymentRefundedForSettlement;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefundPaymentService 테스트")
class RefundPaymentServiceTest {

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private EventPublisher eventPublisher;

	@InjectMocks
	private RefundPaymentService refundPaymentService;

	@Nested
	@DisplayName("refund 메서드")
	class RefundTests {

		@Test
		@DisplayName("결제를 정상적으로 환불한다")
		void refund_Success() {
			// given
			Long paymentId = 1L;
			Long requesterId = 100L;
			String reason = "단순 변심";

			OrderItemSnapshot item1 = new OrderItemSnapshot(1L, Money.of(10000), 200L);
			OrderItemSnapshot item2 = new OrderItemSnapshot(2L, Money.of(10000), 201L);

			Payment payment = Payment.builder()
				.id(paymentId)
				.orderId("order-123")
				.memberId(requesterId)
				.type(PaymentType.FUNDING)
				.method(PaymentMethod.CARD)
				.originAmount(Money.of(20000))
				.paidAmount(Money.of(20000))
				.orderItems(List.of(item1, item2))
				.status(PaymentStatus.PAID)
				.paymentKey("payment-key")
				.approveCode("approve-code")
				.paidAt(LocalDateTime.now().minusDays(1))
				.build();

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> invocation.getArgument(0));

			RefundPaymentCommand command = new RefundPaymentCommand(paymentId, requesterId, reason, Money.of(20000));

			// when
			refundPaymentService.refund(command);

			// then
			verify(paymentRepository).findById(paymentId);
			verify(paymentRepository).save(payment);
			assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
		}

		@Test
		@DisplayName("PaymentRefundedEvent를 발행한다")
		void refund_PublishesPaymentRefundedEvent() {
			// given
			Long paymentId = 1L;
			Long requesterId = 100L;
			String reason = "단순 변심";

			OrderItemSnapshot item = new OrderItemSnapshot(1L, Money.of(20000), 200L);

			Payment payment = Payment.builder()
				.id(paymentId)
				.orderId("order-123")
				.memberId(requesterId)
				.type(PaymentType.FUNDING)
				.method(PaymentMethod.CARD)
				.originAmount(Money.of(20000))
				.paidAmount(Money.of(20000))
				.orderItems(List.of(item))
				.status(PaymentStatus.PAID)
				.paymentKey("payment-key")
				.approveCode("approve-code")
				.paidAt(LocalDateTime.now().minusDays(1))
				.build();

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> invocation.getArgument(0));

			RefundPaymentCommand command = new RefundPaymentCommand(paymentId, requesterId, reason, Money.of(20000));

			// when
			refundPaymentService.refund(command);

			// then
			ArgumentCaptor<PaymentRefundedEvent> eventCaptor = ArgumentCaptor.forClass(PaymentRefundedEvent.class);
			verify(eventPublisher).publish(eventCaptor.capture());

			PaymentRefundedEvent capturedEvent = eventCaptor.getValue();
			assertThat(capturedEvent.getPaymentId()).isEqualTo(paymentId);
			assertThat(capturedEvent.getMemberId()).isEqualTo(requesterId);
			assertThat(capturedEvent.getOrderId()).isEqualTo("order-123");
			assertThat(capturedEvent.getPaymentType()).isEqualTo(PaymentType.FUNDING);
			assertThat(capturedEvent.getRefundAmount()).isEqualTo(Money.of(20000));
			assertThat(capturedEvent.getReason()).isEqualTo(reason);
			assertThat(capturedEvent.getRefundedAt()).isNotNull();
		}

		@Test
		@DisplayName("PaymentRefundedForSettlement 이벤트를 올바른 판매자 ID와 함께 발행한다")
		void refund_PublishesPaymentRefundedForSettlementEvent() {
			// given
			Long paymentId = 1L;
			Long requesterId = 100L;
			String reason = "단순 변심";

			OrderItemSnapshot item1 = new OrderItemSnapshot(1L, Money.of(10000), 200L);
			OrderItemSnapshot item2 = new OrderItemSnapshot(2L, Money.of(10000), 201L);

			Payment payment = Payment.builder()
				.id(paymentId)
				.orderId("order-123")
				.memberId(requesterId)
				.type(PaymentType.FUNDING)
				.method(PaymentMethod.CARD)
				.originAmount(Money.of(20000))
				.paidAmount(Money.of(20000))
				.orderItems(List.of(item1, item2))
				.status(PaymentStatus.PAID)
				.paymentKey("payment-key")
				.approveCode("approve-code")
				.paidAt(LocalDateTime.now().minusDays(1))
				.build();

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> invocation.getArgument(0));

			RefundPaymentCommand command = new RefundPaymentCommand(paymentId, requesterId, reason, Money.of(20000));

			// when
			refundPaymentService.refund(command);

			// then
			ArgumentCaptor<PaymentRefundedForSettlement> eventCaptor = ArgumentCaptor.forClass(
				PaymentRefundedForSettlement.class);
			verify(eventPublisher).publish(eventCaptor.capture());

			PaymentRefundedForSettlement capturedEvent = eventCaptor.getValue();
			assertThat(capturedEvent.paymentId()).isEqualTo(paymentId);
			assertThat(capturedEvent.refundAmount()).isEqualTo(Money.of(20000));
			assertThat(capturedEvent.sellerIds()).containsExactlyInAnyOrder(200L, 201L);
			assertThat(capturedEvent.occurredAt()).isNotNull();
			assertThat(capturedEvent.eventId()).isNotNull();
		}

		@Test
		@DisplayName("여러 판매자의 상품이 있을 때 중복된 판매자 ID를 제거하고 이벤트를 발행한다")
		void refund_WithMultipleSellers_PublishesDistinctSellerIds() {
			// given
			Long paymentId = 1L;
			Long requesterId = 100L;
			String reason = "단순 변심";

			// 동일한 판매자(200L)의 상품이 여러 개 포함
			OrderItemSnapshot item1 = new OrderItemSnapshot(1L, Money.of(10000), 200L);
			OrderItemSnapshot item2 = new OrderItemSnapshot(2L, Money.of(5000), 200L);
			OrderItemSnapshot item3 = new OrderItemSnapshot(3L, Money.of(5000), 201L);

			Payment payment = Payment.builder()
				.id(paymentId)
				.orderId("order-123")
				.memberId(requesterId)
				.type(PaymentType.FUNDING)
				.method(PaymentMethod.CARD)
				.originAmount(Money.of(20000))
				.paidAmount(Money.of(20000))
				.orderItems(List.of(item1, item2, item3))
				.status(PaymentStatus.PAID)
				.paymentKey("payment-key")
				.approveCode("approve-code")
				.paidAt(LocalDateTime.now().minusDays(1))
				.build();

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> invocation.getArgument(0));

			RefundPaymentCommand command = new RefundPaymentCommand(paymentId, requesterId, reason, Money.of(20000));

			// when
			refundPaymentService.refund(command);

			// then
			ArgumentCaptor<PaymentRefundedForSettlement> eventCaptor = ArgumentCaptor.forClass(
				PaymentRefundedForSettlement.class);
			verify(eventPublisher).publish(eventCaptor.capture());

			PaymentRefundedForSettlement capturedEvent = eventCaptor.getValue();
			// 200L이 중복되지 않고 한 번만 포함되는지 확인
			assertThat(capturedEvent.sellerIds()).hasSize(2);
			assertThat(capturedEvent.sellerIds()).containsExactlyInAnyOrder(200L, 201L);
		}

		@Test
		@DisplayName("결제를 찾을 수 없으면 예외가 발생한다")
		void refund_PaymentNotFound_ThrowsException() {
			// given
			Long paymentId = 999L;
			Long requesterId = 100L;
			String reason = "단순 변심";

			given(paymentRepository.findById(paymentId)).willReturn(Optional.empty());

			RefundPaymentCommand command = new RefundPaymentCommand(paymentId, requesterId, reason, Money.of(20000));

			// when & then
			assertThatThrownBy(() -> refundPaymentService.refund(command))
				.isInstanceOf(PaymentException.class)
				.extracting("errorCode")
				.isEqualTo(PaymentErrorCode.PAYMENT_NOT_FOUND);

			verify(paymentRepository).findById(paymentId);
			verify(paymentRepository, never()).save(any(Payment.class));
			verify(eventPublisher, never()).publish(any());
		}

		@Test
		@DisplayName("요청자가 결제 소유자가 아니면 예외가 발생한다")
		void refund_UnauthorizedRequester_ThrowsException() {
			// given
			Long paymentId = 1L;
			Long ownerId = 100L;
			Long unauthorizedRequesterId = 999L;
			String reason = "단순 변심";

			OrderItemSnapshot item = new OrderItemSnapshot(1L, Money.of(20000), 200L);

			Payment payment = Payment.builder()
				.id(paymentId)
				.orderId("order-123")
				.memberId(ownerId)
				.type(PaymentType.FUNDING)
				.method(PaymentMethod.CARD)
				.originAmount(Money.of(20000))
				.paidAmount(Money.of(20000))
				.orderItems(List.of(item))
				.status(PaymentStatus.PAID)
				.paymentKey("payment-key")
				.approveCode("approve-code")
				.paidAt(LocalDateTime.now().minusDays(1))
				.build();

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

			RefundPaymentCommand command = new RefundPaymentCommand(paymentId, unauthorizedRequesterId, reason, Money.of(20000));

			// when & then
			assertThatThrownBy(() -> refundPaymentService.refund(command))
				.isInstanceOf(PaymentException.class)
				.extracting("errorCode")
				.isEqualTo(PaymentErrorCode.UNAUTHORIZED_ACCESS);

			verify(paymentRepository).findById(paymentId);
			verify(paymentRepository, never()).save(any(Payment.class));
			verify(eventPublisher, never()).publish(any());
		}

		@Test
		@DisplayName("환불 불가능한 상태(PENDING)일 때 예외가 발생한다")
		void refund_NotRefundableStatus_ThrowsException() {
			// given
			Long paymentId = 1L;
			Long requesterId = 100L;
			String reason = "단순 변심";

			OrderItemSnapshot item = new OrderItemSnapshot(1L, Money.of(20000), 200L);

			Payment payment = Payment.builder()
				.id(paymentId)
				.orderId("order-123")
				.memberId(requesterId)
				.type(PaymentType.FUNDING)
				.method(PaymentMethod.CARD)
				.originAmount(Money.of(20000))
				.paidAmount(Money.of(20000))
				.orderItems(List.of(item))
				.status(PaymentStatus.PENDING) // 환불 불가능한 상태
				.build();

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

			RefundPaymentCommand command = new RefundPaymentCommand(paymentId, requesterId, reason, Money.of(20000));

			// when & then
			assertThatThrownBy(() -> refundPaymentService.refund(command))
				.isInstanceOf(PaymentException.class)
				.extracting("errorCode")
				.isEqualTo(PaymentErrorCode.NOT_REFUNDABLE);

			verify(paymentRepository).findById(paymentId);
			verify(paymentRepository, never()).save(any(Payment.class));
			verify(eventPublisher, never()).publish(any());
		}

		@Test
		@DisplayName("환불 불가능한 상태(RECEIVED)일 때 예외가 발생한다")
		void refund_ReceivedStatus_ThrowsException() {
			// given
			Long paymentId = 1L;
			Long requesterId = 100L;
			String reason = "단순 변심";

			OrderItemSnapshot item = new OrderItemSnapshot(1L, Money.of(20000), 200L);

			Payment payment = Payment.builder()
				.id(paymentId)
				.orderId("order-123")
				.memberId(requesterId)
				.type(PaymentType.FUNDING)
				.method(PaymentMethod.CARD)
				.originAmount(Money.of(20000))
				.paidAmount(Money.of(20000))
				.orderItems(List.of(item))
				.status(PaymentStatus.RECEIVED) // 수령 확정 상태는 환불 불가
				.paymentKey("payment-key")
				.approveCode("approve-code")
				.paidAt(LocalDateTime.now().minusDays(1))
				.build();

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

			RefundPaymentCommand command = new RefundPaymentCommand(paymentId, requesterId, reason, Money.of(20000));

			// when & then
			assertThatThrownBy(() -> refundPaymentService.refund(command))
				.isInstanceOf(PaymentException.class)
				.extracting("errorCode")
				.isEqualTo(PaymentErrorCode.NOT_REFUNDABLE);

			verify(paymentRepository).findById(paymentId);
			verify(paymentRepository, never()).save(any(Payment.class));
			verify(eventPublisher, never()).publish(any());
		}

		@Test
		@DisplayName("이미 환불된 결제는 예외가 발생한다")
		void refund_AlreadyRefunded_ThrowsException() {
			// given
			Long paymentId = 1L;
			Long requesterId = 100L;
			String reason = "단순 변심";

			OrderItemSnapshot item = new OrderItemSnapshot(1L, Money.of(20000), 200L);

			Payment payment = Payment.builder()
				.id(paymentId)
				.orderId("order-123")
				.memberId(requesterId)
				.type(PaymentType.FUNDING)
				.method(PaymentMethod.CARD)
				.originAmount(Money.of(20000))
				.paidAmount(Money.of(20000))
				.orderItems(List.of(item))
				.status(PaymentStatus.REFUNDED) // 이미 환불됨
				.paymentKey("payment-key")
				.approveCode("approve-code")
				.paidAt(LocalDateTime.now().minusDays(1))
				.build();

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

			RefundPaymentCommand command = new RefundPaymentCommand(paymentId, requesterId, reason, Money.of(20000));

			// when & then
			assertThatThrownBy(() -> refundPaymentService.refund(command))
				.isInstanceOf(PaymentException.class)
				.extracting("errorCode")
				.isEqualTo(PaymentErrorCode.NOT_REFUNDABLE);

			verify(paymentRepository).findById(paymentId);
			verify(paymentRepository, never()).save(any(Payment.class));
			verify(eventPublisher, never()).publish(any());
		}

		@Test
		@DisplayName("두 개의 이벤트가 모두 발행된다")
		void refund_PublishesBothEvents() {
			// given
			Long paymentId = 1L;
			Long requesterId = 100L;
			String reason = "단순 변심";

			OrderItemSnapshot item = new OrderItemSnapshot(1L, Money.of(20000), 200L);

			Payment payment = Payment.builder()
				.id(paymentId)
				.orderId("order-123")
				.memberId(requesterId)
				.type(PaymentType.FUNDING)
				.method(PaymentMethod.CARD)
				.originAmount(Money.of(20000))
				.paidAmount(Money.of(20000))
				.orderItems(List.of(item))
				.status(PaymentStatus.PAID)
				.paymentKey("payment-key")
				.approveCode("approve-code")
				.paidAt(LocalDateTime.now().minusDays(1))
				.build();

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> invocation.getArgument(0));

			RefundPaymentCommand command = new RefundPaymentCommand(paymentId, requesterId, reason, Money.of(20000));

			// when
			refundPaymentService.refund(command);

			// then
			ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
			verify(eventPublisher, times(2)).publish(eventCaptor.capture());

			List<Object> capturedEvents = eventCaptor.getAllValues();
			assertThat(capturedEvents).hasAtLeastOneElementOfType(PaymentRefundedEvent.class);
			assertThat(capturedEvents).hasAtLeastOneElementOfType(PaymentRefundedForSettlement.class);
		}
	}
}
