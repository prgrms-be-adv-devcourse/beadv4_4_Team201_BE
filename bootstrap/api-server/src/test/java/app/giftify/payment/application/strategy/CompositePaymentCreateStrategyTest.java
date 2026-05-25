package app.giftify.payment.application.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.payment.application.inbound.CreatePaymentCommand;
import app.giftify.payment.application.inbound.PaymentCreatedResult;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentException;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.payment.domain.type.PaymentMethod;
import app.giftify.payment.domain.type.PaymentType;
import app.giftify.support.common.money.Money;
import app.giftify.wallet.application.inbound.DeductWalletCommand;
import app.giftify.wallet.application.inbound.DeductWalletResult;
import app.giftify.wallet.application.inbound.DeductWalletUseCase;

@ExtendWith(MockitoExtension.class)
class CompositePaymentCreateStrategyTest {

	@InjectMocks
	private CompositePaymentCreateStrategy strategy;

	@Mock
	private DeductWalletUseCase deductWalletUseCase;

	private CreatePaymentCommand command(PaymentMethod method, Money walletDeduct) {
		return new CreatePaymentCommand(
				100L,
				200L,
				"ORDER-1",
				PaymentType.FUNDING,
				method,
				Money.of(50000),
				walletDeduct
		);
	}

	private Payment savedPaymentMock() {
		Payment payment = mock(Payment.class);
		lenient().when(payment.getId()).thenReturn(1000L);
		lenient().when(payment.getOrderNumber()).thenReturn("ORDER-1");
		lenient().when(payment.getStatus()).thenReturn(PaymentStatus.PENDING);
		lenient().when(payment.getPaymentKey()).thenReturn(null);
		lenient().when(payment.getLastTransactionKey()).thenReturn(null);
		lenient().when(payment.getCreatedAt()).thenReturn(null);
		return payment;
	}

	@Test
	@DisplayName("canHandle: PG 결제 + 예치금 차감액 > 0 이면 true")
	void canHandle_True_PgPlusWallet() {
		CreatePaymentCommand cmd = command(PaymentMethod.CARD, Money.of(10000));

		assertThat(strategy.canHandle(cmd)).isTrue();
	}

	@Test
	@DisplayName("canHandle: 예치금 단일 결제(DEPOSIT)면 false")
	void canHandle_False_WalletOnly() {
		CreatePaymentCommand cmd = command(PaymentMethod.DEPOSIT, Money.of(10000));

		assertThat(strategy.canHandle(cmd)).isFalse();
	}

	@Test
	@DisplayName("canHandle: PG 결제지만 예치금 차감액 == 0 이면 false")
	void canHandle_False_NoWalletPortion() {
		CreatePaymentCommand cmd = command(PaymentMethod.CARD, Money.zero());

		assertThat(strategy.canHandle(cmd)).isFalse();
	}

	@Test
	@DisplayName("execute 성공: deduct 성공 시 DeductWalletCommand 발행 + PaymentCreatedResult 반환")
	void execute_Success() {
		CreatePaymentCommand cmd = command(PaymentMethod.CARD, Money.of(10000));
		Payment payment = savedPaymentMock();
		given(deductWalletUseCase.deductForPayment(any(DeductWalletCommand.class)))
				.willReturn(DeductWalletResult.success(99L, Money.of(40000)));

		PaymentCreatedResult result = strategy.execute(payment, cmd);

		ArgumentCaptor<DeductWalletCommand> captor =
				ArgumentCaptor.forClass(DeductWalletCommand.class);
		then(deductWalletUseCase).should().deductForPayment(captor.capture());
		DeductWalletCommand sent = captor.getValue();
		assertThat(sent.memberId()).isEqualTo(100L);
		assertThat(sent.paymentId()).isEqualTo(1000L);
		assertThat(sent.orderId()).isEqualTo("ORDER-1");
		assertThat(sent.amount()).isEqualTo(Money.of(10000));
		assertThat(result.paymentId()).isEqualTo(1000L);
	}

	@Test
	@DisplayName("execute 실패: 예치금 부족 시 INSUFFICIENT_WALLET_BALANCE 예외")
	void execute_Fail_InsufficientBalance() {
		CreatePaymentCommand cmd = command(PaymentMethod.CARD, Money.of(10000));
		Payment payment = savedPaymentMock();
		given(deductWalletUseCase.deductForPayment(any(DeductWalletCommand.class)))
				.willReturn(DeductWalletResult.insufficientBalance(99L, Money.of(10000), Money.of(5000)));

		assertThatThrownBy(() -> strategy.execute(payment, cmd))
				.isInstanceOf(PaymentException.class);
	}

	private static <T> T any(Class<T> clazz) {
		return org.mockito.ArgumentMatchers.any(clazz);
	}
}
