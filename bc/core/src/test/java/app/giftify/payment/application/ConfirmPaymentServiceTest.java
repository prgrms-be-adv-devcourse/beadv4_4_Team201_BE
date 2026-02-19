package app.giftify.payment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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

import app.giftify.payment.adapter.outbound.pg.TossConfirmResult;
import app.giftify.payment.application.inbound.ConfirmPaymentCommand;
import app.giftify.payment.application.inbound.ConfirmPaymentResult;
import app.giftify.payment.application.outbound.PaymentFieldEncryptor;
import app.giftify.payment.application.outbound.PaymentGateway;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.OrderItemSnapshot;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.payment.domain.event.PaymentConfirmedEvent;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.payment.PaymentPaidExternalEvent;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConfirmPaymentService 테스트")
class ConfirmPaymentServiceTest {

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private PaymentGateway paymentGateway;

	@Mock
	private EventPublisher eventPublisher;

	@Mock
	private PaymentFieldEncryptor encryptor;

	@InjectMocks
	private ConfirmPaymentService confirmPaymentService;

	private Payment createPendingPayment(Long paymentId, Long memberId, String orderNumber, PaymentType type) {
		if (type == PaymentType.FUNDING) {
			return Payment.builder()
				.id(paymentId)
				.orderId(123L)
				.orderNumber(orderNumber)
				.memberId(memberId)
				.type(type)
				.method(PaymentMethod.CARD)
				.originAmount(Money.of(10000))
				.paidAmount(Money.of(10000))
				.orderItems(List.of(
					new OrderItemSnapshot(1L, Money.of(10000), 1L)
				))
				.status(PaymentStatus.PENDING)
				.build();
		}
		return Payment.builder()
			.id(paymentId)
			.orderId(123L)
			.orderNumber(orderNumber)
			.memberId(memberId)
			.type(type)
			.method(PaymentMethod.CARD)
			.originAmount(Money.of(10000))
			.paidAmount(Money.of(10000))
			.orderItems(List.of())
			.status(PaymentStatus.PENDING)
			.build();
	}

	@Nested
	@DisplayName("confirm 메서드 - 성공 케이스")
	class ConfirmSuccessTests {

		@Test
		@DisplayName("DEPOSIT_CHARGE(예치금 충전) 결제를 정상적으로 승인한다")
		void confirm_PointChargePayment_Success() {
			// given
			Long paymentId = 1L;
			Long memberId = 100L;
			String paymentKey = "payment-key-123";
			String orderNumber = "order-123";
			Money amount = Money.of(10000);

			ConfirmPaymentCommand command = new ConfirmPaymentCommand(
				paymentId, memberId, paymentKey, orderNumber, amount
			);

			Payment payment = createPendingPayment(paymentId, memberId, orderNumber, PaymentType.DEPOSIT_CHARGE);

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(paymentGateway.confirm(paymentKey, orderNumber, amount))
				.willReturn(TossConfirmResult.success(paymentKey, "txn-key-001", "12345678"));
			given(encryptor.encrypt(paymentKey)).willReturn("encrypted-payment-key");
			given(paymentRepository.save(any(Payment.class))).willReturn(payment);

			// when
			ConfirmPaymentResult result = confirmPaymentService.confirm(command);

			// then
			assertThat(result.success()).isTrue();
			assertThat(result.paymentId()).isEqualTo(paymentId);
			assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);

			verify(paymentRepository).findById(paymentId);
			verify(paymentGateway).confirm(paymentKey, orderNumber, amount);
			verify(encryptor).encrypt(paymentKey);
			verify(paymentRepository).save(payment);
		}

		@Test
		@DisplayName("결제 승인 시 PaymentConfirmedEvent 내부 이벤트를 발행한다")
		void confirm_Payment_PublishesPaymentConfirmedEvent() {
			// given
			Long paymentId = 1L;
			Long memberId = 100L;
			String paymentKey = "payment-key-123";
			String orderNumber = "order-123";
			Money amount = Money.of(10000);

			ConfirmPaymentCommand command = new ConfirmPaymentCommand(
				paymentId, memberId, paymentKey, orderNumber, amount
			);

			Payment payment = createPendingPayment(paymentId, memberId, orderNumber, PaymentType.DEPOSIT_CHARGE);

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(paymentGateway.confirm(anyString(), anyString(), any())).willReturn(TossConfirmResult.success("test-payment-key", "txn-key-001", "12345678"));
			given(encryptor.encrypt(anyString())).willReturn("encrypted");
			given(paymentRepository.save(any(Payment.class))).willReturn(payment);

			// when
			confirmPaymentService.confirm(command);

			// then
			ArgumentCaptor<PaymentConfirmedEvent> eventCaptor = ArgumentCaptor.forClass(PaymentConfirmedEvent.class);
			verify(eventPublisher).publish(eventCaptor.capture());

			PaymentConfirmedEvent paidEvent = eventCaptor.getValue();
			assertThat(paidEvent.getPaymentId()).isEqualTo(paymentId);
			assertThat(paidEvent.getMemberId()).isEqualTo(memberId);
			assertThat(paidEvent.getPaymentType()).isEqualTo(PaymentType.DEPOSIT_CHARGE);
		}

		@Test
		@DisplayName("FUNDING 결제도 정상적으로 승인된다")
		void confirm_FundingPayment_Success() {
			// given
			Long paymentId = 2L;
			Long memberId = 200L;
			String paymentKey = "payment-key-456";
			String orderNumber = "funding-order-456";
			Money amount = Money.of(10000);

			ConfirmPaymentCommand command = new ConfirmPaymentCommand(
				paymentId, memberId, paymentKey, orderNumber, amount
			);

			Payment payment = createPendingPayment(paymentId, memberId, orderNumber, PaymentType.FUNDING);

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(paymentGateway.confirm(anyString(), anyString(), any())).willReturn(TossConfirmResult.success("test-payment-key", "txn-key-001", "12345678"));
			given(encryptor.encrypt(anyString())).willReturn("encrypted");
			given(paymentRepository.save(any(Payment.class))).willReturn(payment);

			// when
			ConfirmPaymentResult result = confirmPaymentService.confirm(command);

			// then
			assertThat(result.success()).isTrue();
			assertThat(result.paymentId()).isEqualTo(paymentId);
			verify(eventPublisher).publish(any(PaymentConfirmedEvent.class));
		}

		@Test
		@DisplayName("결제 승인 시 PaymentPaidExternalEvent 이벤트를 발행한다")
		void confirm_Payment_PublishesPaymentPaidExternalEvent() {
			// given
			Long paymentId = 1L;
			Long memberId = 100L;
			String paymentKey = "payment-key-123";
			String orderNumber = "order-123";
			Money amount = Money.of(10000);
			String encryptedKey = "encrypted-payment-key";
			String transactionKey = "txn-key-001";

			ConfirmPaymentCommand command = new ConfirmPaymentCommand(
				paymentId, memberId, paymentKey, orderNumber, amount
			);

			Payment payment = createPendingPayment(paymentId, memberId, orderNumber, PaymentType.DEPOSIT_CHARGE);

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(paymentGateway.confirm(paymentKey, orderNumber, amount))
				.willReturn(TossConfirmResult.success(paymentKey, transactionKey, "12345678"));
			given(encryptor.encrypt(paymentKey)).willReturn(encryptedKey);
			given(paymentRepository.save(any(Payment.class))).willReturn(payment);

			// when
			confirmPaymentService.confirm(command);

			// then
			ArgumentCaptor<PaymentPaidExternalEvent> captor =
				ArgumentCaptor.forClass(PaymentPaidExternalEvent.class);
			verify(eventPublisher).publish(captor.capture());

			PaymentPaidExternalEvent event = captor.getValue();
			assertThat(event.paymentId()).isEqualTo(paymentId);
			assertThat(event.orderNumber()).isEqualTo(orderNumber);
			assertThat(event.paymentKey()).isEqualTo(encryptedKey);
			assertThat(event.transactionKey()).isEqualTo(transactionKey);
			assertThat(event.paidAmount()).isEqualTo(amount);
			assertThat(event.method()).isEqualTo(PaymentMethod.CARD);
			assertThat(event.eventId()).isNotNull();
			assertThat(event.occurredAt()).isNotNull();
		}
	}

	@Nested
	@DisplayName("confirm 메서드 - PG 실패 케이스")
	class ConfirmPgFailureTests {

		@Test
		@DisplayName("PG 승인 실패 시 실패 결과를 반환한다")
		void confirm_PgFailure_ReturnsFailureResult() {
			// given
			Long paymentId = 1L;
			Long memberId = 100L;
			String paymentKey = "payment-key-123";
			String orderNumber = "order-123";
			Money amount = Money.of(10000);

			ConfirmPaymentCommand command = new ConfirmPaymentCommand(
				paymentId, memberId, paymentKey, orderNumber, amount
			);

			Payment payment = createPendingPayment(paymentId, memberId, orderNumber, PaymentType.DEPOSIT_CHARGE);

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(paymentGateway.confirm(paymentKey, orderNumber, amount))
				.willReturn(TossConfirmResult.failure("INVALID_CARD", "카드 정보가 유효하지 않습니다"));

			// when
			ConfirmPaymentResult result = confirmPaymentService.confirm(command);

			// then
			assertThat(result.success()).isFalse();
			assertThat(result.errorCode()).isEqualTo("INVALID_CARD");
			assertThat(result.errorMessage()).isEqualTo("카드 정보가 유효하지 않습니다");

			verify(paymentRepository, never()).save(any());
			verify(eventPublisher, never()).publish(any());
		}
	}

	@Nested
	@DisplayName("confirm 메서드 - 검증 실패 케이스")
	class ConfirmValidationFailureTests {

		@Test
		@DisplayName("결제를 찾을 수 없으면 예외가 발생한다")
		void confirm_PaymentNotFound_ThrowsException() {
			// given
			Long paymentId = 999L;
			ConfirmPaymentCommand command = new ConfirmPaymentCommand(
				paymentId, 100L, "payment-key", "order-123", Money.of(10000)
			);

			given(paymentRepository.findById(paymentId)).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> confirmPaymentService.confirm(command))
				.isInstanceOf(PaymentException.class)
				.extracting("errorCode")
				.isEqualTo(PaymentErrorCode.PAYMENT_NOT_FOUND);

			verify(paymentGateway, never()).confirm(any(), any(), any());
		}

		@Test
		@DisplayName("소유자가 다르면 UNAUTHORIZED_ACCESS 예외가 발생한다")
		void confirm_UnauthorizedAccess_ThrowsException() {
			// given
			Long paymentId = 1L;
			Long actualOwnerId = 100L;
			Long requesterId = 999L;

			ConfirmPaymentCommand command = new ConfirmPaymentCommand(
				paymentId, requesterId, "payment-key", "order-123", Money.of(10000)
			);

			Payment payment = createPendingPayment(paymentId, actualOwnerId, "order-123", PaymentType.DEPOSIT_CHARGE);
			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

			// when & then
			assertThatThrownBy(() -> confirmPaymentService.confirm(command))
				.isInstanceOf(PaymentException.class)
				.extracting("errorCode")
				.isEqualTo(PaymentErrorCode.UNAUTHORIZED_ACCESS);

			verify(paymentGateway, never()).confirm(any(), any(), any());
		}

		@Test
		@DisplayName("금액이 불일치하면 AMOUNT_MISMATCH 예외가 발생한다")
		void confirm_AmountMismatch_ThrowsException() {
			// given
			Long paymentId = 1L;
			Long memberId = 100L;
			Money requestedAmount = Money.of(5000);

			ConfirmPaymentCommand command = new ConfirmPaymentCommand(
				paymentId, memberId, "payment-key", "order-123", requestedAmount
			);

			Payment payment = createPendingPayment(paymentId, memberId, "order-123", PaymentType.DEPOSIT_CHARGE);
			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

			// when & then
			assertThatThrownBy(() -> confirmPaymentService.confirm(command))
				.isInstanceOf(PaymentException.class)
				.extracting("errorCode")
				.isEqualTo(PaymentErrorCode.AMOUNT_MISMATCH);

			verify(paymentGateway, never()).confirm(any(), any(), any());
		}
	}

	@Nested
	@DisplayName("confirm 메서드 - 암호화 검증")
	class ConfirmEncryptionTests {

		@Test
		@DisplayName("paymentKey는 항상 암호화된다")
		void confirm_AlwaysEncryptsPaymentKey() {
			// given
			Long paymentId = 1L;
			Long memberId = 100L;
			String paymentKey = "raw-payment-key";
			String orderNumber = "order-123";
			Money amount = Money.of(10000);

			ConfirmPaymentCommand command = new ConfirmPaymentCommand(
				paymentId, memberId, paymentKey, orderNumber, amount
			);

			Payment payment = createPendingPayment(paymentId, memberId, orderNumber, PaymentType.DEPOSIT_CHARGE);
			String encryptedPaymentKey = "encrypted-payment-key";

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(paymentGateway.confirm(anyString(), anyString(), any())).willReturn(TossConfirmResult.success("test-payment-key", "txn-key-001", "12345678"));
			given(encryptor.encrypt(paymentKey)).willReturn(encryptedPaymentKey);
			given(paymentRepository.save(any(Payment.class))).willReturn(payment);

			// when
			confirmPaymentService.confirm(command);

			// then
			verify(encryptor).encrypt(paymentKey);
			assertThat(payment.getPaymentKey()).isEqualTo(encryptedPaymentKey);
		}
	}
}
