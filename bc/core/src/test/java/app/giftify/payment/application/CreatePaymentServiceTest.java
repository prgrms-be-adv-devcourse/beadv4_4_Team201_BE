package app.giftify.payment.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.payment.application.inbound.CreateFundingPaymentCommand;
import app.giftify.payment.application.inbound.PaymentCreatedResult;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.OrderItemSnapshot;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;
import app.giftify.wallet.application.inbound.DeductWalletCommand;
import app.giftify.wallet.application.inbound.DeductWalletResult;
import app.giftify.wallet.application.inbound.DeductWalletUseCase;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreatePaymentService 테스트")
class CreatePaymentServiceTest {

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private DeductWalletUseCase deductWalletUseCase;

	@InjectMocks
	private CreatePaymentService createPaymentService;

	@Nested
	@DisplayName("create 메서드")
	class CreateTests {

		@Test
		@DisplayName("멱등성 키(orderId)가 일치하는 기존 결제가 있으면 해당 결제를 반환한다")
		void create_ReturnsExistingPayment_WhenIdempotencyKeyMatches() {
			// given
			Long memberId = 1L;
			String orderId = "order-123";
			Money amount = Money.of(10000);
			List<OrderItemSnapshot> orderItems = List.of(
				new OrderItemSnapshot(1L, Money.of(10000), 100L)
			);

			CreateFundingPaymentCommand command = new CreateFundingPaymentCommand(
				memberId, orderId,
				PaymentMethod.CARD,
				amount, orderItems
			);

			Payment existingPayment = Payment.builder()
				.id(999L)
				.orderId(orderId)
				.memberId(memberId)
				.type(PaymentType.FUNDING)
				.method(PaymentMethod.CARD)
				.originAmount(amount)
				.paidAmount(amount)
				.orderItems(orderItems)
				.status(PaymentStatus.PENDING)
				.build();

			given(paymentRepository.findByOrderId(orderId))
				.willReturn(List.of(existingPayment));

			// when
			PaymentCreatedResult result = createPaymentService.create(command);

			// then
			assertThat(result.paymentId()).isEqualTo(999L);
			assertThat(result.orderId()).isEqualTo(orderId);
			assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);
			assertThat(result.paymentKey()).isNull();
			assertThat(result.lastTransactionKey()).isNull();
			assertThat(result.createdAt()).isNull();

			verify(paymentRepository, never()).save(any());
			verify(deductWalletUseCase, never()).deductForPayment(any());
		}

		@Test
		@DisplayName("CARD 결제를 정상적으로 생성한다")
		void create_CreatesNewCardPayment_Successfully() {
			// given
			Long memberId = 1L;
			String orderId = "order-123";
			Money amount = Money.of(10000);
			List<OrderItemSnapshot> orderItems = List.of(
				new OrderItemSnapshot(1L, Money.of(10000), 100L)
			);

			CreateFundingPaymentCommand command = new CreateFundingPaymentCommand(
				memberId, orderId,
				PaymentMethod.CARD,
				amount, orderItems
			);

			LocalDateTime createdAt = LocalDateTime.of(2025, 1, 15, 10, 30, 0);
			Payment savedPayment = Payment.builder()
				.id(1L)
				.orderId(orderId)
				.memberId(memberId)
				.type(PaymentType.FUNDING)
				.method(PaymentMethod.CARD)
				.originAmount(amount)
				.paidAmount(amount)
				.orderItems(orderItems)
				.status(PaymentStatus.PENDING)
				.createdAt(createdAt)
				.build();

			given(paymentRepository.findByOrderId(orderId))
				.willReturn(List.of());
			given(paymentRepository.save(any(Payment.class)))
				.willReturn(savedPayment);

			// when
			PaymentCreatedResult result = createPaymentService.create(command);

			// then
			assertThat(result.paymentId()).isEqualTo(1L);
			assertThat(result.orderId()).isEqualTo(orderId);
			assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);
			assertThat(result.paymentKey()).isNull();
			assertThat(result.lastTransactionKey()).isNull();
			assertThat(result.createdAt()).isEqualTo(createdAt);

			verify(paymentRepository).save(any(Payment.class));
			verify(deductWalletUseCase, never()).deductForPayment(any());
		}
	}

	@Nested
	@DisplayName("DEPOSIT(예치금) 결제")
	class WalletPaymentTests {

		@Test
		@DisplayName("DEPOSIT 결제를 정상적으로 생성하고 지갑 차감을 호출한다")
		void create_CreatesNewWalletPayment_AndCallsWalletDeduction() {
			// given
			Long memberId = 1L;
			String orderId = "order-123";
			Money amount = Money.of(10000);
			List<OrderItemSnapshot> orderItems = List.of(
				new OrderItemSnapshot(1L, Money.of(10000), 100L)
			);

			CreateFundingPaymentCommand command = new CreateFundingPaymentCommand(
				memberId, orderId,
				PaymentMethod.DEPOSIT,
				amount, orderItems
			);

			LocalDateTime createdAt = LocalDateTime.of(2025, 1, 15, 10, 30, 0);
			Payment savedPayment = Payment.builder()
				.id(1L)
				.orderId(orderId)
				.memberId(memberId)
				.type(PaymentType.FUNDING)
				.method(PaymentMethod.DEPOSIT)
				.originAmount(amount)
				.paidAmount(amount)
				.orderItems(orderItems)
				.status(PaymentStatus.PENDING)
				.createdAt(createdAt)
				.build();

			DeductWalletResult walletResult = DeductWalletResult.success(100L, Money.of(5000));

			given(paymentRepository.findByOrderId(orderId))
				.willReturn(List.of());
			given(paymentRepository.save(any(Payment.class)))
				.willReturn(savedPayment);
			given(deductWalletUseCase.deductForPayment(any(DeductWalletCommand.class)))
				.willReturn(walletResult);

			// when
			PaymentCreatedResult result = createPaymentService.create(command);

			// then
			assertThat(result.paymentId()).isEqualTo(1L);
			assertThat(result.orderId()).isEqualTo(orderId);
			assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);
			assertThat(result.paymentKey()).isNull();
			assertThat(result.lastTransactionKey()).isNull();
			assertThat(result.createdAt()).isEqualTo(createdAt);

			ArgumentCaptor<DeductWalletCommand> commandCaptor = ArgumentCaptor.forClass(DeductWalletCommand.class);
			verify(deductWalletUseCase).deductForPayment(commandCaptor.capture());

			DeductWalletCommand capturedCommand = commandCaptor.getValue();
			assertThat(capturedCommand.memberId()).isEqualTo(memberId);
			assertThat(capturedCommand.paymentId()).isEqualTo(1L);
			assertThat(capturedCommand.orderId()).isEqualTo(orderId);
			assertThat(capturedCommand.amount()).isEqualTo(amount);
		}

		@Test
		@DisplayName("지갑 잔액이 부족하면 예외를 던진다")
		void create_ThrowsException_WhenWalletBalanceInsufficient() {
			// given
			Long memberId = 1L;
			String orderId = "order-123";
			Money requiredAmount = Money.of(10000);
			Money currentBalance = Money.of(3000);
			List<OrderItemSnapshot> orderItems = List.of(
				new OrderItemSnapshot(1L, Money.of(10000), 100L)
			);

			CreateFundingPaymentCommand command = new CreateFundingPaymentCommand(
				memberId, orderId,
				PaymentMethod.DEPOSIT,
				requiredAmount, orderItems
			);

			Payment savedPayment = Payment.builder()
				.id(1L)
				.orderId(orderId)
				.memberId(memberId)
				.type(PaymentType.FUNDING)
				.method(PaymentMethod.DEPOSIT)
				.originAmount(requiredAmount)
				.paidAmount(requiredAmount)
				.orderItems(orderItems)
				.status(PaymentStatus.PENDING)
				.build();

			DeductWalletResult walletResult = DeductWalletResult.insufficientBalance(
				100L, requiredAmount, currentBalance
			);

			given(paymentRepository.findByOrderId(orderId))
				.willReturn(List.of());
			given(paymentRepository.save(any(Payment.class)))
				.willReturn(savedPayment);
			given(deductWalletUseCase.deductForPayment(any(DeductWalletCommand.class)))
				.willReturn(walletResult);

			// when & then
			assertThatThrownBy(() -> createPaymentService.create(command))
				.isInstanceOf(PaymentException.class)
				.satisfies(thrown -> {
					PaymentException exception = (PaymentException)thrown;
					assertThat(exception.getErrorCode()).isEqualTo(PaymentErrorCode.INSUFFICIENT_WALLET_BALANCE);
				});
		}

		@Test
		@DisplayName("기존 DEPOSIT 결제가 있으면 해당 결제를 반환한다")
		void create_ReturnsExistingWalletPayment() {
			// given
			Long memberId = 1L;
			String orderId = "order-123";
			Money amount = Money.of(10000);
			List<OrderItemSnapshot> orderItems = List.of(
				new OrderItemSnapshot(1L, Money.of(10000), 100L)
			);

			CreateFundingPaymentCommand command = new CreateFundingPaymentCommand(
				memberId, orderId,
				PaymentMethod.DEPOSIT,
				amount, orderItems
			);

			Payment existingPayment = Payment.builder()
				.id(999L)
				.orderId(orderId)
				.memberId(memberId)
				.type(PaymentType.FUNDING)
				.method(PaymentMethod.DEPOSIT)
				.originAmount(amount)
				.paidAmount(amount)
				.orderItems(orderItems)
				.status(PaymentStatus.PENDING)
				.build();

			given(paymentRepository.findByOrderId(orderId))
				.willReturn(List.of(existingPayment));

			// when
			PaymentCreatedResult result = createPaymentService.create(command);

			// then
			assertThat(result.paymentId()).isEqualTo(999L);
			assertThat(result.orderId()).isEqualTo(orderId);
			assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);
			assertThat(result.paymentKey()).isNull();
			assertThat(result.lastTransactionKey()).isNull();
			assertThat(result.createdAt()).isNull();

			verify(paymentRepository, never()).save(any());
			verify(deductWalletUseCase, never()).deductForPayment(any());
		}
	}

	@Nested
	@DisplayName("Payment 저장 검증")
	class PaymentSaveVerificationTests {

		@Test
		@DisplayName("Payment 저장 시 올바른 값이 전달된다")
		void create_SavesPaymentWithCorrectValues() {
			// given
			Long memberId = 1L;
			String orderId = "order-123";
			Money amount = Money.of(10000);
			List<OrderItemSnapshot> orderItems = List.of(
				new OrderItemSnapshot(1L, Money.of(10000), 100L)
			);

			CreateFundingPaymentCommand command = new CreateFundingPaymentCommand(
				memberId, orderId,
				PaymentMethod.CARD,
				amount, orderItems
			);

			ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);

			given(paymentRepository.findByOrderId(orderId))
				.willReturn(List.of());
			given(paymentRepository.save(paymentCaptor.capture()))
				.willAnswer(invocation -> {
					Payment payment = invocation.getArgument(0);
					return Payment.builder()
						.id(1L)
						.orderId(payment.getOrderId())
						.memberId(payment.getMemberId())
						.type(payment.getType())
						.method(payment.getMethod())
						.originAmount(payment.getOriginAmount())
						.paidAmount(payment.getPaidAmount())
						.orderItems(payment.getOrderItems())
						.status(payment.getStatus())
						.build();
				});

			// when
			createPaymentService.create(command);

			// then
			Payment capturedPayment = paymentCaptor.getValue();
			assertThat(capturedPayment.getOrderId()).isEqualTo(orderId);
			assertThat(capturedPayment.getMemberId()).isEqualTo(memberId);
			assertThat(capturedPayment.getType()).isEqualTo(PaymentType.FUNDING);
			assertThat(capturedPayment.getMethod()).isEqualTo(PaymentMethod.CARD);
			assertThat(capturedPayment.getOriginAmount()).isEqualTo(amount);
			assertThat(capturedPayment.getPaidAmount()).isEqualTo(amount);
			assertThat(capturedPayment.getStatus()).isEqualTo(PaymentStatus.PENDING);
			assertThat(capturedPayment.getOrderItems()).hasSize(1);
		}
	}
}
