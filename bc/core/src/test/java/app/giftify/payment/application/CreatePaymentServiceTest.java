package app.giftify.payment.application;

import app.giftify.payment.application.inbound.CreatePaymentCommand;
import app.giftify.payment.application.inbound.PaymentCreatedResult;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.*;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;
import app.giftify.wallet.application.inbound.DeductWalletCommand;
import app.giftify.wallet.application.inbound.DeductWalletResult;
import app.giftify.wallet.application.inbound.DeductWalletUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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
		@DisplayName("멱등성 키가 일치하는 기존 결제가 있으면 해당 결제를 반환한다")
		void create_ReturnsExistingPayment_WhenIdempotencyKeyMatches() {
			// given
			String idempotencyKey = "idem-key-123";
			Long memberId = 1L;
			String orderId = "order-123";
			Money amount = Money.of(10000);
			List<OrderItemSnapshot> orderItems = List.of(
				new OrderItemSnapshot("item-1", "상품1", Money.of(10000), 1, Money.of(10000), 100L)
			);

			CreatePaymentCommand command = new CreatePaymentCommand(
				idempotencyKey, memberId, orderId,
				PaymentType.FUNDING, PaymentMethod.CARD,
				amount, orderItems
			);

			Payment existingPayment = Payment.builder()
				.id(999L)
				.idempotencyKey(idempotencyKey)
				.orderId(orderId)
				.memberId(memberId)
				.type(PaymentType.FUNDING)
				.method(PaymentMethod.CARD)
				.originAmount(amount)
				.paidAmount(amount)
				.orderItems(orderItems)
				.status(PaymentStatus.PENDING)
				.build();

			given(paymentRepository.findByIdempotencyKey(idempotencyKey))
				.willReturn(Optional.of(existingPayment));

			// when
			PaymentCreatedResult result = createPaymentService.create(command);

			// then
			assertThat(result.paymentId()).isEqualTo(999L);
			assertThat(result.orderId()).isEqualTo(orderId);
			assertThat(result.idempotencyKey()).isEqualTo(idempotencyKey);
			assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);
			assertThat(result.requiresPgApproval()).isTrue();

			verify(paymentRepository, never()).save(any());
			verify(deductWalletUseCase, never()).deductForPayment(any());
		}

		@Test
		@DisplayName("CARD 결제를 정상적으로 생성한다")
		void create_CreatesNewCardPayment_Successfully() {
			// given
			String idempotencyKey = "idem-key-123";
			Long memberId = 1L;
			String orderId = "order-123";
			Money amount = Money.of(10000);
			List<OrderItemSnapshot> orderItems = List.of(
				new OrderItemSnapshot("item-1", "상품1", Money.of(10000), 1, Money.of(10000), 100L)
			);

			CreatePaymentCommand command = new CreatePaymentCommand(
				idempotencyKey, memberId, orderId,
				PaymentType.FUNDING, PaymentMethod.CARD,
				amount, orderItems
			);

			Payment savedPayment = Payment.builder()
				.id(1L)
				.idempotencyKey(idempotencyKey)
				.orderId(orderId)
				.memberId(memberId)
				.type(PaymentType.FUNDING)
				.method(PaymentMethod.CARD)
				.originAmount(amount)
				.paidAmount(amount)
				.orderItems(orderItems)
				.status(PaymentStatus.PENDING)
				.build();

			given(paymentRepository.findByIdempotencyKey(idempotencyKey))
				.willReturn(Optional.empty());
			given(paymentRepository.save(any(Payment.class)))
				.willReturn(savedPayment);

			// when
			PaymentCreatedResult result = createPaymentService.create(command);

			// then
			assertThat(result.paymentId()).isEqualTo(1L);
			assertThat(result.orderId()).isEqualTo(orderId);
			assertThat(result.idempotencyKey()).isEqualTo(idempotencyKey);
			assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);
			assertThat(result.requiresPgApproval()).isTrue();
			assertThat(result.walletInfo()).isNull();

			verify(paymentRepository).save(any(Payment.class));
			verify(deductWalletUseCase, never()).deductForPayment(any());
		}

		@Test
		@DisplayName("CARD 결제는 PG 승인이 필요하다")
		void create_CardPayment_RequiresPgApproval() {
			// given
			String idempotencyKey = "idem-key-123";
			Long memberId = 1L;
			String orderId = "order-123";
			Money amount = Money.of(10000);
			List<OrderItemSnapshot> orderItems = List.of(
				new OrderItemSnapshot("item-1", "상품1", Money.of(10000), 1, Money.of(10000), 100L)
			);

			CreatePaymentCommand command = new CreatePaymentCommand(
				idempotencyKey, memberId, orderId,
				PaymentType.FUNDING, PaymentMethod.CARD,
				amount, orderItems
			);

			Payment savedPayment = Payment.builder()
				.id(1L)
				.idempotencyKey(idempotencyKey)
				.orderId(orderId)
				.memberId(memberId)
				.type(PaymentType.FUNDING)
				.method(PaymentMethod.CARD)
				.originAmount(amount)
				.paidAmount(amount)
				.orderItems(orderItems)
				.status(PaymentStatus.PENDING)
				.build();

			given(paymentRepository.findByIdempotencyKey(idempotencyKey))
				.willReturn(Optional.empty());
			given(paymentRepository.save(any(Payment.class)))
				.willReturn(savedPayment);

			// when
			PaymentCreatedResult result = createPaymentService.create(command);

			// then
			assertThat(result.requiresPgApproval()).isTrue();
		}
	}

	@Nested
	@DisplayName("WALLET 결제")
	class WalletPaymentTests {

		@Test
		@DisplayName("WALLET 결제를 정상적으로 생성하고 지갑 차감을 호출한다")
		void create_CreatesNewWalletPayment_AndCallsWalletDeduction() {
			// given
			String idempotencyKey = "idem-key-123";
			Long memberId = 1L;
			String orderId = "order-123";
			Money amount = Money.of(10000);
			List<OrderItemSnapshot> orderItems = List.of(
				new OrderItemSnapshot("item-1", "상품1", Money.of(10000), 1, Money.of(10000), 100L)
			);

			CreatePaymentCommand command = new CreatePaymentCommand(
				idempotencyKey, memberId, orderId,
				PaymentType.FUNDING, PaymentMethod.WALLET,
				amount, orderItems
			);

			Payment savedPayment = Payment.builder()
				.id(1L)
				.idempotencyKey(idempotencyKey)
				.orderId(orderId)
				.memberId(memberId)
				.type(PaymentType.FUNDING)
				.method(PaymentMethod.WALLET)
				.originAmount(amount)
				.paidAmount(amount)
				.orderItems(orderItems)
				.status(PaymentStatus.PENDING)
				.build();

			DeductWalletResult walletResult = DeductWalletResult.success(100L, Money.of(5000));

			given(paymentRepository.findByIdempotencyKey(idempotencyKey))
				.willReturn(Optional.empty());
			given(paymentRepository.save(any(Payment.class)))
				.willReturn(savedPayment);
			given(deductWalletUseCase.deductForPayment(any(DeductWalletCommand.class)))
				.willReturn(walletResult);

			// when
			PaymentCreatedResult result = createPaymentService.create(command);

			// then
			assertThat(result.paymentId()).isEqualTo(1L);
			assertThat(result.orderId()).isEqualTo(orderId);
			assertThat(result.idempotencyKey()).isEqualTo(idempotencyKey);
			assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);
			assertThat(result.requiresPgApproval()).isFalse();
			assertThat(result.walletInfo()).isNull();

			ArgumentCaptor<DeductWalletCommand> commandCaptor = ArgumentCaptor.forClass(DeductWalletCommand.class);
			verify(deductWalletUseCase).deductForPayment(commandCaptor.capture());

			DeductWalletCommand capturedCommand = commandCaptor.getValue();
			assertThat(capturedCommand.memberId()).isEqualTo(memberId);
			assertThat(capturedCommand.paymentId()).isEqualTo(1L);
			assertThat(capturedCommand.orderId()).isEqualTo(orderId);
			assertThat(capturedCommand.amount()).isEqualTo(amount);
		}

		@Test
		@DisplayName("WALLET 결제는 PG 승인이 필요하지 않다")
		void create_WalletPayment_DoesNotRequirePgApproval() {
			// given
			String idempotencyKey = "idem-key-123";
			Long memberId = 1L;
			String orderId = "order-123";
			Money amount = Money.of(10000);
			List<OrderItemSnapshot> orderItems = List.of(
				new OrderItemSnapshot("item-1", "상품1", Money.of(10000), 1, Money.of(10000), 100L)
			);

			CreatePaymentCommand command = new CreatePaymentCommand(
				idempotencyKey, memberId, orderId,
				PaymentType.FUNDING, PaymentMethod.WALLET,
				amount, orderItems
			);

			Payment savedPayment = Payment.builder()
				.id(1L)
				.idempotencyKey(idempotencyKey)
				.orderId(orderId)
				.memberId(memberId)
				.type(PaymentType.FUNDING)
				.method(PaymentMethod.WALLET)
				.originAmount(amount)
				.paidAmount(amount)
				.orderItems(orderItems)
				.status(PaymentStatus.PENDING)
				.build();

			DeductWalletResult walletResult = DeductWalletResult.success(100L, Money.of(5000));

			given(paymentRepository.findByIdempotencyKey(idempotencyKey))
				.willReturn(Optional.empty());
			given(paymentRepository.save(any(Payment.class)))
				.willReturn(savedPayment);
			given(deductWalletUseCase.deductForPayment(any(DeductWalletCommand.class)))
				.willReturn(walletResult);

			// when
			PaymentCreatedResult result = createPaymentService.create(command);

			// then
			assertThat(result.requiresPgApproval()).isFalse();
		}

		@Test
		@DisplayName("지갑 잔액이 부족하면 부족한 금액 정보를 반환한다")
		void create_ReturnsInsufficientBalanceInfo_WhenWalletBalanceInsufficient() {
			// given
			String idempotencyKey = "idem-key-123";
			Long memberId = 1L;
			String orderId = "order-123";
			Money requiredAmount = Money.of(10000);
			Money currentBalance = Money.of(3000);
			List<OrderItemSnapshot> orderItems = List.of(
				new OrderItemSnapshot("item-1", "상품1", Money.of(10000), 1, Money.of(10000), 100L)
			);

			CreatePaymentCommand command = new CreatePaymentCommand(
				idempotencyKey, memberId, orderId,
				PaymentType.FUNDING, PaymentMethod.WALLET,
				requiredAmount, orderItems
			);

			Payment savedPayment = Payment.builder()
				.id(1L)
				.idempotencyKey(idempotencyKey)
				.orderId(orderId)
				.memberId(memberId)
				.type(PaymentType.FUNDING)
				.method(PaymentMethod.WALLET)
				.originAmount(requiredAmount)
				.paidAmount(requiredAmount)
				.orderItems(orderItems)
				.status(PaymentStatus.PENDING)
				.build();

			DeductWalletResult walletResult = DeductWalletResult.insufficientBalance(
				100L, requiredAmount, currentBalance
			);

			given(paymentRepository.findByIdempotencyKey(idempotencyKey))
				.willReturn(Optional.empty());
			given(paymentRepository.save(any(Payment.class)))
				.willReturn(savedPayment);
			given(deductWalletUseCase.deductForPayment(any(DeductWalletCommand.class)))
				.willReturn(walletResult);

			// when
			PaymentCreatedResult result = createPaymentService.create(command);

			// then
			assertThat(result.paymentId()).isEqualTo(1L);
			assertThat(result.orderId()).isEqualTo(orderId);
			assertThat(result.idempotencyKey()).isEqualTo(idempotencyKey);
			assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);
			assertThat(result.requiresPgApproval()).isFalse();
			assertThat(result.hasInsufficientBalance()).isTrue();
			assertThat(result.walletInfo()).isNotNull();
			assertThat(result.walletInfo().requiredAmount()).isEqualTo(requiredAmount);
			assertThat(result.walletInfo().currentBalance()).isEqualTo(currentBalance);
			assertThat(result.walletInfo().shortfall()).isEqualTo(Money.of(7000));
		}

		@Test
		@DisplayName("기존 WALLET 결제를 반환할 때는 PG 승인이 필요하지 않다")
		void create_ReturnsExistingWalletPayment_WithoutPgApproval() {
			// given
			String idempotencyKey = "idem-key-123";
			Long memberId = 1L;
			String orderId = "order-123";
			Money amount = Money.of(10000);
			List<OrderItemSnapshot> orderItems = List.of(
				new OrderItemSnapshot("item-1", "상품1", Money.of(10000), 1, Money.of(10000), 100L)
			);

			CreatePaymentCommand command = new CreatePaymentCommand(
				idempotencyKey, memberId, orderId,
				PaymentType.FUNDING, PaymentMethod.WALLET,
				amount, orderItems
			);

			Payment existingPayment = Payment.builder()
				.id(999L)
				.idempotencyKey(idempotencyKey)
				.orderId(orderId)
				.memberId(memberId)
				.type(PaymentType.FUNDING)
				.method(PaymentMethod.WALLET)
				.originAmount(amount)
				.paidAmount(amount)
				.orderItems(orderItems)
				.status(PaymentStatus.PENDING)
				.build();

			given(paymentRepository.findByIdempotencyKey(idempotencyKey))
				.willReturn(Optional.of(existingPayment));

			// when
			PaymentCreatedResult result = createPaymentService.create(command);

			// then
			assertThat(result.paymentId()).isEqualTo(999L);
			assertThat(result.orderId()).isEqualTo(orderId);
			assertThat(result.idempotencyKey()).isEqualTo(idempotencyKey);
			assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);
			assertThat(result.requiresPgApproval()).isFalse();

			verify(paymentRepository, never()).save(any());
			verify(deductWalletUseCase, never()).deductForPayment(any());
		}
	}

	@Nested
	@DisplayName("다양한 결제 수단")
	class VariousPaymentMethodTests {

		@Test
		@DisplayName("BANK_TRANSFER 결제는 PG 승인이 필요하다")
		void create_BankTransferPayment_RequiresPgApproval() {
			// given
			String idempotencyKey = "idem-key-123";
			Long memberId = 1L;
			String orderId = "order-123";
			Money amount = Money.of(10000);
			List<OrderItemSnapshot> orderItems = List.of(
				new OrderItemSnapshot("item-1", "상품1", Money.of(10000), 1, Money.of(10000), 100L)
			);

			CreatePaymentCommand command = new CreatePaymentCommand(
				idempotencyKey, memberId, orderId,
				PaymentType.FUNDING, PaymentMethod.BANK_TRANSFER,
				amount, orderItems
			);

			Payment savedPayment = Payment.builder()
				.id(1L)
				.idempotencyKey(idempotencyKey)
				.orderId(orderId)
				.memberId(memberId)
				.type(PaymentType.FUNDING)
				.method(PaymentMethod.BANK_TRANSFER)
				.originAmount(amount)
				.paidAmount(amount)
				.orderItems(orderItems)
				.status(PaymentStatus.PENDING)
				.build();

			given(paymentRepository.findByIdempotencyKey(idempotencyKey))
				.willReturn(Optional.empty());
			given(paymentRepository.save(any(Payment.class)))
				.willReturn(savedPayment);

			// when
			PaymentCreatedResult result = createPaymentService.create(command);

			// then
			assertThat(result.requiresPgApproval()).isTrue();
			verify(deductWalletUseCase, never()).deductForPayment(any());
		}

		@Test
		@DisplayName("VIRTUAL_ACCOUNT 결제는 PG 승인이 필요하다")
		void create_VirtualAccountPayment_RequiresPgApproval() {
			// given
			String idempotencyKey = "idem-key-123";
			Long memberId = 1L;
			String orderId = "order-123";
			Money amount = Money.of(10000);
			List<OrderItemSnapshot> orderItems = List.of(
				new OrderItemSnapshot("item-1", "상품1", Money.of(10000), 1, Money.of(10000), 100L)
			);

			CreatePaymentCommand command = new CreatePaymentCommand(
				idempotencyKey, memberId, orderId,
				PaymentType.FUNDING, PaymentMethod.VIRTUAL_ACCOUNT,
				amount, orderItems
			);

			Payment savedPayment = Payment.builder()
				.id(1L)
				.idempotencyKey(idempotencyKey)
				.orderId(orderId)
				.memberId(memberId)
				.type(PaymentType.FUNDING)
				.method(PaymentMethod.VIRTUAL_ACCOUNT)
				.originAmount(amount)
				.paidAmount(amount)
				.orderItems(orderItems)
				.status(PaymentStatus.PENDING)
				.build();

			given(paymentRepository.findByIdempotencyKey(idempotencyKey))
				.willReturn(Optional.empty());
			given(paymentRepository.save(any(Payment.class)))
				.willReturn(savedPayment);

			// when
			PaymentCreatedResult result = createPaymentService.create(command);

			// then
			assertThat(result.requiresPgApproval()).isTrue();
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
			String idempotencyKey = "idem-key-123";
			Long memberId = 1L;
			String orderId = "order-123";
			Money amount = Money.of(10000);
			List<OrderItemSnapshot> orderItems = List.of(
				new OrderItemSnapshot("item-1", "상품1", Money.of(10000), 1, Money.of(10000), 100L)
			);

			CreatePaymentCommand command = new CreatePaymentCommand(
				idempotencyKey, memberId, orderId,
				PaymentType.FUNDING, PaymentMethod.CARD,
				amount, orderItems
			);

			ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);

			given(paymentRepository.findByIdempotencyKey(idempotencyKey))
				.willReturn(Optional.empty());
			given(paymentRepository.save(paymentCaptor.capture()))
				.willAnswer(invocation -> {
					Payment payment = invocation.getArgument(0);
					return Payment.builder()
						.id(1L)
						.idempotencyKey(payment.getIdempotencyKey())
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
			assertThat(capturedPayment.getIdempotencyKey()).isEqualTo(idempotencyKey);
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
