package app.giftify.payment.application.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
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

import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.OrderItemSnapshot;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.payment.domain.event.PaymentConfirmedEvent;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;
import app.giftify.wallet.domain.event.WalletDeductedEvent;

@ExtendWith(MockitoExtension.class)
@DisplayName("WalletDeductedEventHandler 테스트")
class WalletDeductedEventHandlerTest {

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private EventPublisher eventPublisher;

	@InjectMocks
	private WalletDeductedEventHandler handler;

	@Nested
	@DisplayName("FUNDING 타입 결제 처리")
	class FundingPaymentTests {

		@Test
		@DisplayName("FUNDING 결제가 완료되면 PaymentPaidEvent를 발행한다")
		void handle_PublishesPaymentPaidEvent_WhenPaymentTypeIsFunding() {
			// given
			Long paymentId = 1L;
			Long walletId = 100L;
			Long memberId = 200L;
			String orderId = "ORDER-123";
			Money amount = Money.of(50000);
			LocalDateTime deductedAt = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

			WalletDeductedEvent event = new WalletDeductedEvent(
				walletId, memberId, paymentId, orderId, amount, deductedAt
			);

			List<OrderItemSnapshot> orderItems = List.of(
				new OrderItemSnapshot("item-1", "상품1", Money.of(50000), 1, Money.of(50000), 100L)
			);

			Payment payment = Payment.builder()
				.id(paymentId)
				.idempotencyKey("idem-key-123")
				.memberId(memberId)
				.orderId(orderId)
				.type(PaymentType.FUNDING)
				.method(PaymentMethod.DEPOSIT)
				.originAmount(amount)
				.paidAmount(amount)
				.orderItems(orderItems)
				.status(PaymentStatus.PENDING)
				.build();

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> invocation.getArgument(0));

			// when
			handler.handle(event);

			// then
			ArgumentCaptor<PaymentConfirmedEvent> eventCaptor = ArgumentCaptor.forClass(PaymentConfirmedEvent.class);
			verify(eventPublisher).publish(eventCaptor.capture());

			PaymentConfirmedEvent paidEvent = eventCaptor.getValue();
			assertThat(paidEvent.getPaymentId()).isEqualTo(paymentId);
			assertThat(paidEvent.getMemberId()).isEqualTo(memberId);
			assertThat(paidEvent.getOrderId()).isEqualTo(orderId);
			assertThat(paidEvent.getPaymentType()).isEqualTo(PaymentType.FUNDING);
			assertThat(paidEvent.getPaidAmount()).isEqualTo(amount);
			assertThat(paidEvent.getPaidAt()).isEqualTo(deductedAt);
		}

		@Test
		@DisplayName("FUNDING 결제 완료 시 Payment 상태가 PAID로 변경된다")
		void handle_UpdatesPaymentStatusToPaid_WhenFundingPayment() {
			// given
			Long paymentId = 1L;
			Long walletId = 100L;
			Long memberId = 200L;
			String orderId = "ORDER-123";
			Money amount = Money.of(50000);
			LocalDateTime deductedAt = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

			WalletDeductedEvent event = new WalletDeductedEvent(
				walletId, memberId, paymentId, orderId, amount, deductedAt
			);

			List<OrderItemSnapshot> orderItems = List.of(
				new OrderItemSnapshot("item-1", "상품1", Money.of(50000), 1, Money.of(50000), 100L)
			);

			Payment payment = Payment.builder()
				.id(paymentId)
				.idempotencyKey("idem-key-123")
				.memberId(memberId)
				.orderId(orderId)
				.type(PaymentType.FUNDING)
				.method(PaymentMethod.DEPOSIT)
				.originAmount(amount)
				.paidAmount(amount)
				.orderItems(orderItems)
				.status(PaymentStatus.PENDING)
				.build();

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> invocation.getArgument(0));

			// when
			handler.handle(event);

			// then
			ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
			verify(paymentRepository).save(paymentCaptor.capture());

			Payment savedPayment = paymentCaptor.getValue();
			assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.PAID);
			assertThat(savedPayment.getPaidAt()).isEqualTo(deductedAt);
		}
	}

	@Nested
	@DisplayName("DEPOSIT_CHARGE(예치금 충전) 타입 결제 처리")
	class PointChargePaymentTests {

		@Test
		@DisplayName("DEPOSIT_CHARGE(예치금 충전) 결제가 완료되면 PaymentPaidEvent를 발행한다")
		void handle_PublishesPaymentPaidEvent_WhenPaymentTypeIsPointCharge() {
			// given
			Long paymentId = 2L;
			Long walletId = 100L;
			Long memberId = 200L;
			String orderId = "ORDER-456";
			Money amount = Money.of(30000);
			LocalDateTime deductedAt = LocalDateTime.of(2024, 1, 15, 11, 0, 0);

			WalletDeductedEvent event = new WalletDeductedEvent(
				walletId, memberId, paymentId, orderId, amount, deductedAt
			);

			Payment payment = Payment.builder()
				.id(paymentId)
				.idempotencyKey("idem-key-456")
				.memberId(memberId)
				.orderId(orderId)
				.type(PaymentType.DEPOSIT_CHARGE)
				.method(PaymentMethod.DEPOSIT)
				.originAmount(amount)
				.paidAmount(amount)
				.orderItems(List.of())
				.status(PaymentStatus.PENDING)
				.build();

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> invocation.getArgument(0));

			// when
			handler.handle(event);

			// then
			ArgumentCaptor<PaymentConfirmedEvent> eventCaptor = ArgumentCaptor.forClass(PaymentConfirmedEvent.class);
			verify(eventPublisher).publish(eventCaptor.capture());

			PaymentConfirmedEvent paidEvent = eventCaptor.getValue();
			assertThat(paidEvent.getPaymentId()).isEqualTo(paymentId);
			assertThat(paidEvent.getMemberId()).isEqualTo(memberId);
			assertThat(paidEvent.getPaymentType()).isEqualTo(PaymentType.DEPOSIT_CHARGE);
		}

		@Test
		@DisplayName("DEPOSIT_CHARGE(예치금 충전) 결제 완료 시 Payment 상태가 PAID로 변경된다")
		void handle_UpdatesPaymentStatusToPaid_WhenPointChargePayment() {
			// given
			Long paymentId = 2L;
			Long walletId = 100L;
			Long memberId = 200L;
			String orderId = "ORDER-456";
			Money amount = Money.of(30000);
			LocalDateTime deductedAt = LocalDateTime.of(2024, 1, 15, 11, 0, 0);

			WalletDeductedEvent event = new WalletDeductedEvent(
				walletId, memberId, paymentId, orderId, amount, deductedAt
			);

			Payment payment = Payment.builder()
				.id(paymentId)
				.idempotencyKey("idem-key-456")
				.memberId(memberId)
				.orderId(orderId)
				.type(PaymentType.DEPOSIT_CHARGE)
				.method(PaymentMethod.DEPOSIT)
				.originAmount(amount)
				.paidAmount(amount)
				.orderItems(List.of())
				.status(PaymentStatus.PENDING)
				.build();

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> invocation.getArgument(0));

			// when
			handler.handle(event);

			// then
			ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
			verify(paymentRepository).save(paymentCaptor.capture());

			Payment savedPayment = paymentCaptor.getValue();
			assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.PAID);
			assertThat(savedPayment.getPaidAt()).isEqualTo(deductedAt);
		}
	}

	@Nested
	@DisplayName("requestId 포맷 검증")
	class RequestIdFormatTests {

		@Test
		@DisplayName("requestId가 'WALLET-{walletId}' 포맷으로 생성된다")
		void handle_FormatsRequestIdAsWalletWithWalletId() {
			// given
			Long paymentId = 3L;
			Long walletId = 999L;
			Long memberId = 200L;
			String orderId = "ORDER-789";
			Money amount = Money.of(20000);
			LocalDateTime deductedAt = LocalDateTime.of(2024, 1, 15, 12, 0, 0);

			WalletDeductedEvent event = new WalletDeductedEvent(
				walletId, memberId, paymentId, orderId, amount, deductedAt
			);

			List<OrderItemSnapshot> orderItems = List.of(
				new OrderItemSnapshot("item-1", "상품1", Money.of(20000), 1, Money.of(20000), 100L)
			);

			Payment payment = Payment.builder()
				.id(paymentId)
				.idempotencyKey("idem-key-789")
				.memberId(memberId)
				.orderId(orderId)
				.type(PaymentType.FUNDING)
				.method(PaymentMethod.DEPOSIT)
				.originAmount(amount)
				.paidAmount(amount)
				.orderItems(orderItems)
				.status(PaymentStatus.PENDING)
				.build();

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> invocation.getArgument(0));

			// when
			handler.handle(event);

			// then
			ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
			verify(paymentRepository).save(paymentCaptor.capture());

			Payment savedPayment = paymentCaptor.getValue();
			assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.PAID);
			assertThat(savedPayment.getUncommittedHistory()).hasSize(1);
		}
	}

	@Nested
	@DisplayName("예외 처리")
	class ExceptionTests {

		@Test
		@DisplayName("Payment를 찾을 수 없으면 PaymentException을 발생시킨다")
		void handle_ThrowsPaymentException_WhenPaymentNotFound() {
			// given
			Long paymentId = 999L;
			Long walletId = 100L;
			Long memberId = 200L;
			String orderId = "ORDER-NOTFOUND";
			Money amount = Money.of(10000);
			LocalDateTime deductedAt = LocalDateTime.of(2024, 1, 15, 13, 0, 0);

			WalletDeductedEvent event = new WalletDeductedEvent(
				walletId, memberId, paymentId, orderId, amount, deductedAt
			);

			given(paymentRepository.findById(paymentId)).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> handler.handle(event))
				.isInstanceOf(PaymentException.class)
				.hasMessageContaining("Payment를 찾을 수 없습니다")
				.hasFieldOrPropertyWithValue("errorCode", PaymentErrorCode.PAYMENT_NOT_FOUND);

			verify(paymentRepository).findById(paymentId);
			verify(paymentRepository, never()).save(any(Payment.class));
			verify(eventPublisher, never()).publish(any());
		}

		@Test
		@DisplayName("PAID 상태가 아닌 Payment에 대해 처리하려 하면 예외를 발생시킨다")
		void handle_ThrowsPaymentException_WhenPaymentStatusIsNotPayable() {
			// given
			Long paymentId = 4L;
			Long walletId = 100L;
			Long memberId = 200L;
			String orderId = "ORDER-PAID";
			Money amount = Money.of(10000);
			LocalDateTime deductedAt = LocalDateTime.of(2024, 1, 15, 14, 0, 0);

			WalletDeductedEvent event = new WalletDeductedEvent(
				walletId, memberId, paymentId, orderId, amount, deductedAt
			);

			List<OrderItemSnapshot> orderItems = List.of(
				new OrderItemSnapshot("item-1", "상품1", Money.of(10000), 1, Money.of(10000), 100L)
			);

			Payment payment = Payment.builder()
				.id(paymentId)
				.idempotencyKey("idem-key-paid")
				.memberId(memberId)
				.orderId(orderId)
				.type(PaymentType.FUNDING)
				.method(PaymentMethod.DEPOSIT)
				.originAmount(amount)
				.paidAmount(amount)
				.orderItems(orderItems)
				.status(PaymentStatus.PAID)
				.paidAt(LocalDateTime.of(2024, 1, 15, 9, 0, 0))
				.build();

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

			// when & then
			assertThatThrownBy(() -> handler.handle(event))
				.isInstanceOf(PaymentException.class)
				.hasMessageContaining("결제 완료 불가능한 상태")
				.hasFieldOrPropertyWithValue("errorCode", PaymentErrorCode.NOT_PAYABLE);

			verify(paymentRepository).findById(paymentId);
			verify(paymentRepository, never()).save(any(Payment.class));
			verify(eventPublisher, never()).publish(any());
		}
	}

	@Nested
	@DisplayName("Payment 저장 검증")
	class PaymentSaveTests {

		@Test
		@DisplayName("Payment를 저장하고 저장된 Payment로 이벤트를 발행한다")
		void handle_SavesPaymentAndPublishesEvent() {
			// given
			Long paymentId = 5L;
			Long walletId = 100L;
			Long memberId = 200L;
			String orderId = "ORDER-SAVE";
			Money amount = Money.of(40000);
			LocalDateTime deductedAt = LocalDateTime.of(2024, 1, 15, 15, 0, 0);

			WalletDeductedEvent event = new WalletDeductedEvent(
				walletId, memberId, paymentId, orderId, amount, deductedAt
			);

			List<OrderItemSnapshot> orderItems = List.of(
				new OrderItemSnapshot("item-1", "상품1", Money.of(40000), 1, Money.of(40000), 100L)
			);

			Payment payment = Payment.builder()
				.id(paymentId)
				.idempotencyKey("idem-key-save")
				.memberId(memberId)
				.orderId(orderId)
				.type(PaymentType.FUNDING)
				.method(PaymentMethod.DEPOSIT)
				.originAmount(amount)
				.paidAmount(amount)
				.orderItems(orderItems)
				.status(PaymentStatus.PENDING)
				.build();

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> invocation.getArgument(0));

			// when
			handler.handle(event);

			// then
			verify(paymentRepository).findById(paymentId);
			verify(paymentRepository).save(any(Payment.class));
			verify(eventPublisher).publish(any(PaymentConfirmedEvent.class));
		}
	}
}
