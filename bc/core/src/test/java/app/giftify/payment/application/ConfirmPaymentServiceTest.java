package app.giftify.payment.application;

import app.giftify.payment.application.inbound.ConfirmPaymentCommand;
import app.giftify.payment.application.outbound.PaymentFieldEncryptor;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.*;
import app.giftify.payment.domain.event.PaymentPaidEvent;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.payment.PaymentCompletedForFunding;
import app.giftify.shared.domain.event.payment.PaymentConfirmedForOrder;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConfirmPaymentService 테스트")
class ConfirmPaymentServiceTest {

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private EventPublisher eventPublisher;

	@Mock
	private PaymentFieldEncryptor encryptor;

	@InjectMocks
	private ConfirmPaymentService confirmPaymentService;

	@Nested
	@DisplayName("confirm 메서드 - POINT_CHARGE 결제")
	class ConfirmPointChargePaymentTests {

		@Test
		@DisplayName("POINT_CHARGE 결제를 정상적으로 승인한다")
		void confirm_PointChargePayment_Success() {
			// given
			Long paymentId = 1L;
			String paymentKey = "payment-key-123";
			String approveCode = "approve-code-456";
			LocalDateTime paidAt = LocalDateTime.now();
			ConfirmPaymentCommand command = new ConfirmPaymentCommand(
				paymentId, paymentKey, approveCode, paidAt
			);

			Payment payment = Payment.builder()
				.id(paymentId)
				.idempotencyKey("idempotency-key-123")
				.orderId("order-123")
				.memberId(100L)
				.type(PaymentType.POINT_CHARGE)
				.method(PaymentMethod.CARD)
				.originAmount(Money.of(10000))
				.paidAmount(Money.of(10000))
				.orderItems(List.of())
				.status(PaymentStatus.PENDING)
				.build();

			String encryptedPaymentKey = "encrypted-payment-key";
			String encryptedApproveCode = "encrypted-approve-code";

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(encryptor.encrypt(paymentKey)).willReturn(encryptedPaymentKey);
			given(encryptor.encrypt(approveCode)).willReturn(encryptedApproveCode);
			given(paymentRepository.save(any(Payment.class))).willReturn(payment);

			// when
			confirmPaymentService.confirm(command);

			// then
			assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
			assertThat(payment.getPaymentKey()).isEqualTo(encryptedPaymentKey);
			assertThat(payment.getApproveCode()).isEqualTo(encryptedApproveCode);
			assertThat(payment.getPaidAt()).isEqualTo(paidAt);

			verify(paymentRepository).findById(paymentId);
			verify(encryptor).encrypt(paymentKey);
			verify(encryptor).encrypt(approveCode);
			verify(paymentRepository).save(payment);
		}

		@Test
		@DisplayName("POINT_CHARGE 결제 승인 시 PaymentPaidEvent와 PaymentConfirmedForOrder를 발행한다")
		void confirm_PointChargePayment_PublishesCorrectEvents() {
			// given
			Long paymentId = 1L;
			String paymentKey = "payment-key-123";
			String approveCode = "approve-code-456";
			LocalDateTime paidAt = LocalDateTime.now();
			ConfirmPaymentCommand command = new ConfirmPaymentCommand(
				paymentId, paymentKey, approveCode, paidAt
			);

			Payment payment = Payment.builder()
				.id(paymentId)
				.idempotencyKey("idempotency-key-123")
				.orderId("order-123")
				.memberId(100L)
				.type(PaymentType.POINT_CHARGE)
				.method(PaymentMethod.CARD)
				.originAmount(Money.of(10000))
				.paidAmount(Money.of(10000))
				.orderItems(List.of())
				.status(PaymentStatus.PENDING)
				.build();

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(encryptor.encrypt(anyString())).willReturn("encrypted");
			given(paymentRepository.save(any(Payment.class))).willReturn(payment);

			// when
			confirmPaymentService.confirm(command);

			// then
			ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
			verify(eventPublisher, times(2)).publish(eventCaptor.capture());

			List<Object> publishedEvents = eventCaptor.getAllValues();
			assertThat(publishedEvents).hasSize(2);

			// PaymentPaidEvent 검증
			PaymentPaidEvent paidEvent = (PaymentPaidEvent) publishedEvents.get(0);
			assertThat(paidEvent.getPaymentId()).isEqualTo(paymentId);
			assertThat(paidEvent.getMemberId()).isEqualTo(100L);
			assertThat(paidEvent.getOrderId()).isEqualTo("order-123");
			assertThat(paidEvent.getPaymentType()).isEqualTo(PaymentType.POINT_CHARGE);
			assertThat(paidEvent.getPaidAmount()).isEqualTo(Money.of(10000));
			assertThat(paidEvent.getPaidAt()).isEqualTo(paidAt);

			// PaymentConfirmedForOrder 검증
			PaymentConfirmedForOrder orderEvent = (PaymentConfirmedForOrder) publishedEvents.get(1);
			assertThat(orderEvent.paymentId()).isEqualTo(paymentId);
			assertThat(orderEvent.orderId()).isEqualTo("order-123");
			assertThat(orderEvent.amount()).isEqualTo(Money.of(10000));
			assertThat(orderEvent.occurredAt()).isEqualTo(paidAt);
		}
	}

	@Nested
	@DisplayName("confirm 메서드 - FUNDING 결제")
	class ConfirmFundingPaymentTests {

		@Test
		@DisplayName("FUNDING 결제를 정상적으로 승인한다")
		void confirm_FundingPayment_Success() {
			// given
			Long paymentId = 2L;
			String paymentKey = "payment-key-456";
			String approveCode = "approve-code-789";
			LocalDateTime paidAt = LocalDateTime.now();
			ConfirmPaymentCommand command = new ConfirmPaymentCommand(
				paymentId, paymentKey, approveCode, paidAt
			);

			Payment payment = Payment.builder()
				.id(paymentId)
				.idempotencyKey("idempotency-key-456")
				.orderId("funding-order-456")
				.memberId(200L)
				.type(PaymentType.FUNDING)
				.method(PaymentMethod.CARD)
				.originAmount(Money.of(50000))
				.paidAmount(Money.of(50000))
				.orderItems(List.of(
					new OrderItemSnapshot("item-1", "Funding Item", Money.of(50000), 1, Money.of(50000), 1L)
				))
				.status(PaymentStatus.PENDING)
				.build();

			String encryptedPaymentKey = "encrypted-payment-key";
			String encryptedApproveCode = "encrypted-approve-code";

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(encryptor.encrypt(paymentKey)).willReturn(encryptedPaymentKey);
			given(encryptor.encrypt(approveCode)).willReturn(encryptedApproveCode);
			given(paymentRepository.save(any(Payment.class))).willReturn(payment);

			// when
			confirmPaymentService.confirm(command);

			// then
			assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
			assertThat(payment.getPaymentKey()).isEqualTo(encryptedPaymentKey);
			assertThat(payment.getApproveCode()).isEqualTo(encryptedApproveCode);

			verify(paymentRepository).save(payment);
		}

		@Test
		@DisplayName("FUNDING 결제 승인 시 PaymentPaidEvent와 PaymentCompletedForFunding을 발행한다")
		void confirm_FundingPayment_PublishesPaymentCompletedForFundingEvent() {
			// given
			Long paymentId = 2L;
			String paymentKey = "payment-key-456";
			String approveCode = "approve-code-789";
			LocalDateTime paidAt = LocalDateTime.now();
			ConfirmPaymentCommand command = new ConfirmPaymentCommand(
				paymentId, paymentKey, approveCode, paidAt
			);

			Payment payment = Payment.builder()
				.id(paymentId)
				.idempotencyKey("idempotency-key-456")
				.orderId("funding-order-456")
				.memberId(200L)
				.type(PaymentType.FUNDING)
				.method(PaymentMethod.CARD)
				.originAmount(Money.of(50000))
				.paidAmount(Money.of(50000))
				.orderItems(List.of(
					new OrderItemSnapshot("item-1", "Funding Item", Money.of(50000), 1, Money.of(50000), 1L)
				))
				.status(PaymentStatus.PENDING)
				.build();

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(encryptor.encrypt(anyString())).willReturn("encrypted");
			given(paymentRepository.save(any(Payment.class))).willReturn(payment);

			// when
			confirmPaymentService.confirm(command);

			// then
			ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
			verify(eventPublisher, times(2)).publish(eventCaptor.capture());

			List<Object> publishedEvents = eventCaptor.getAllValues();
			assertThat(publishedEvents).hasSize(2);

			// PaymentPaidEvent 검증
			PaymentPaidEvent paidEvent = (PaymentPaidEvent) publishedEvents.get(0);
			assertThat(paidEvent.getPaymentId()).isEqualTo(paymentId);
			assertThat(paidEvent.getMemberId()).isEqualTo(200L);
			assertThat(paidEvent.getPaymentType()).isEqualTo(PaymentType.FUNDING);

			// PaymentCompletedForFunding 검증
			PaymentCompletedForFunding fundingEvent = (PaymentCompletedForFunding) publishedEvents.get(1);
			assertThat(fundingEvent.paymentId()).isEqualTo(paymentId);
			assertThat(fundingEvent.orderId()).isEqualTo("funding-order-456");
			assertThat(fundingEvent.participantId()).isEqualTo(200L);
			assertThat(fundingEvent.amount()).isEqualTo(Money.of(50000));
			assertThat(fundingEvent.occurredAt()).isEqualTo(paidAt);
		}
	}


	@Nested
	@DisplayName("confirm 메서드 - approveCode가 null인 경우")
	class ConfirmWithNullApproveCodeTests {

		@Test
		@DisplayName("approveCode가 null이어도 정상적으로 승인한다")
		void confirm_WithNullApproveCode_Success() {
			// given
			Long paymentId = 4L;
			String paymentKey = "payment-key-no-approve";
			String approveCode = null;
			LocalDateTime paidAt = LocalDateTime.now();
			ConfirmPaymentCommand command = new ConfirmPaymentCommand(
				paymentId, paymentKey, approveCode, paidAt
			);

			Payment payment = Payment.builder()
				.id(paymentId)
				.idempotencyKey("idempotency-key-no-approve")
				.orderId("order-no-approve")
				.memberId(400L)
				.type(PaymentType.FUNDING)
				.method(PaymentMethod.VIRTUAL_ACCOUNT)
				.originAmount(Money.of(20000))
				.paidAmount(Money.of(20000))
				.orderItems(List.of(
					new OrderItemSnapshot("item-2", "Virtual Account Item", Money.of(20000), 1, Money.of(20000), 2L)
				))
				.status(PaymentStatus.PENDING)
				.build();

			String encryptedPaymentKey = "encrypted-payment-key";

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(encryptor.encrypt(paymentKey)).willReturn(encryptedPaymentKey);
			given(paymentRepository.save(any(Payment.class))).willReturn(payment);

			// when
			confirmPaymentService.confirm(command);

			// then
			assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
			assertThat(payment.getPaymentKey()).isEqualTo(encryptedPaymentKey);
			assertThat(payment.getApproveCode()).isNull();

			verify(encryptor, times(1)).encrypt(paymentKey);
			verify(encryptor, never()).encrypt(null);
			verify(paymentRepository).save(payment);
		}

		@Test
		@DisplayName("approveCode가 null일 때 암호화하지 않는다")
		void confirm_WithNullApproveCode_DoesNotEncryptApproveCode() {
			// given
			Long paymentId = 4L;
			String paymentKey = "payment-key-no-approve";
			String approveCode = null;
			LocalDateTime paidAt = LocalDateTime.now();
			ConfirmPaymentCommand command = new ConfirmPaymentCommand(
				paymentId, paymentKey, approveCode, paidAt
			);

			Payment payment = Payment.builder()
				.id(paymentId)
				.idempotencyKey("idempotency-key-no-approve")
				.orderId("order-no-approve")
				.memberId(400L)
				.type(PaymentType.POINT_CHARGE)
				.method(PaymentMethod.VIRTUAL_ACCOUNT)
				.originAmount(Money.of(20000))
				.paidAmount(Money.of(20000))
				.orderItems(List.of())
				.status(PaymentStatus.PENDING)
				.build();

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(encryptor.encrypt(paymentKey)).willReturn("encrypted-payment-key");
			given(paymentRepository.save(any(Payment.class))).willReturn(payment);

			// when
			confirmPaymentService.confirm(command);

			// then
			ArgumentCaptor<String> encryptCaptor = ArgumentCaptor.forClass(String.class);
			verify(encryptor, times(1)).encrypt(encryptCaptor.capture());

			List<String> encryptedValues = encryptCaptor.getAllValues();
			assertThat(encryptedValues).hasSize(1);
			assertThat(encryptedValues.get(0)).isEqualTo(paymentKey);
		}
	}

	@Nested
	@DisplayName("confirm 메서드 - 예외 케이스")
	class ConfirmExceptionTests {

		@Test
		@DisplayName("결제를 찾을 수 없으면 예외가 발생한다")
		void confirm_PaymentNotFound_ThrowsException() {
			// given
			Long paymentId = 999L;
			String paymentKey = "payment-key-999";
			String approveCode = "approve-code-999";
			LocalDateTime paidAt = LocalDateTime.now();
			ConfirmPaymentCommand command = new ConfirmPaymentCommand(
				paymentId, paymentKey, approveCode, paidAt
			);

			given(paymentRepository.findById(paymentId)).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> confirmPaymentService.confirm(command))
				.isInstanceOf(PaymentException.class)
				.extracting("errorCode")
				.isEqualTo(PaymentErrorCode.PAYMENT_NOT_FOUND);

			verify(paymentRepository).findById(paymentId);
			verify(encryptor, never()).encrypt(anyString());
			verify(paymentRepository, never()).save(any());
			verify(eventPublisher, never()).publish(any());
		}
	}

	@Nested
	@DisplayName("confirm 메서드 - 암호화 검증")
	class ConfirmEncryptionTests {

		@Test
		@DisplayName("paymentKey는 항상 암호화된다")
		void confirm_AlwaysEncryptsPaymentKey() {
			// given
			Long paymentId = 5L;
			String paymentKey = "raw-payment-key";
			String approveCode = "raw-approve-code";
			LocalDateTime paidAt = LocalDateTime.now();
			ConfirmPaymentCommand command = new ConfirmPaymentCommand(
				paymentId, paymentKey, approveCode, paidAt
			);

			Payment payment = Payment.builder()
				.id(paymentId)
				.idempotencyKey("idempotency-key-encrypt")
				.orderId("order-encrypt")
				.memberId(500L)
				.type(PaymentType.FUNDING)
				.method(PaymentMethod.CARD)
				.originAmount(Money.of(10000))
				.paidAmount(Money.of(10000))
				.orderItems(List.of(
					new OrderItemSnapshot("item-3", "Encrypt Item", Money.of(10000), 1, Money.of(10000), 3L)
				))
				.status(PaymentStatus.PENDING)
				.build();

			String encryptedPaymentKey = "encrypted-payment-key";
			String encryptedApproveCode = "encrypted-approve-code";

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(encryptor.encrypt(paymentKey)).willReturn(encryptedPaymentKey);
			given(encryptor.encrypt(approveCode)).willReturn(encryptedApproveCode);
			given(paymentRepository.save(any(Payment.class))).willReturn(payment);

			// when
			confirmPaymentService.confirm(command);

			// then
			verify(encryptor).encrypt(paymentKey);
			verify(encryptor).encrypt(approveCode);
			assertThat(payment.getPaymentKey()).isEqualTo(encryptedPaymentKey);
			assertThat(payment.getApproveCode()).isEqualTo(encryptedApproveCode);
		}

		@Test
		@DisplayName("approveCode가 null이 아니면 암호화된다")
		void confirm_EncryptsApproveCodeWhenNotNull() {
			// given
			Long paymentId = 6L;
			String paymentKey = "raw-payment-key";
			String approveCode = "raw-approve-code";
			LocalDateTime paidAt = LocalDateTime.now();
			ConfirmPaymentCommand command = new ConfirmPaymentCommand(
				paymentId, paymentKey, approveCode, paidAt
			);

			Payment payment = Payment.builder()
				.id(paymentId)
				.idempotencyKey("idempotency-key-approve")
				.orderId("order-approve")
				.memberId(600L)
				.type(PaymentType.POINT_CHARGE)
				.method(PaymentMethod.CARD)
				.originAmount(Money.of(15000))
				.paidAmount(Money.of(15000))
				.orderItems(List.of())
				.status(PaymentStatus.PENDING)
				.build();

			String encryptedApproveCode = "encrypted-approve-code";

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(encryptor.encrypt(paymentKey)).willReturn("encrypted-payment-key");
			given(encryptor.encrypt(approveCode)).willReturn(encryptedApproveCode);
			given(paymentRepository.save(any(Payment.class))).willReturn(payment);

			// when
			confirmPaymentService.confirm(command);

			// then
			verify(encryptor).encrypt(approveCode);
			assertThat(payment.getApproveCode()).isEqualTo(encryptedApproveCode);
		}
	}
}
