package payment.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;
import domain.payment.Payment;
import domain.payment.PaymentErrorCode;
import domain.payment.PaymentException;
import domain.payment.PaymentPolicy;
import domain.payment.PaymentRepository;
import domain.wallet.Wallet;
import payment.usecase.command.PaymentInitiateCommand;
import payment.usecase.result.PaymentInitiateResult;
import wallet.service.WalletService;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentInitiateService 테스트")
class PaymentInitiateServiceTest {

	@Mock
	private WalletService walletService;

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private PaymentPolicy fundingPolicy;

	private PaymentInitiateService paymentInitiateService;

	@BeforeEach
	void setUp() {
		paymentInitiateService = new PaymentInitiateService(
			walletService,
			paymentRepository,
			List.of(fundingPolicy)
		);
	}

	@Nested
	@DisplayName("initiate 테스트")
	class InitiateTest {

		@BeforeEach
		void setUpPolicy() {
			// initiate 테스트에서만 펀딩 정책 stubbing 필요
			given(fundingPolicy.support(PaymentType.FUNDING)).willReturn(true);
		}

		@Test
		@DisplayName("예치금이 충분하면 예치금으로 완납하고 completed=true 반환")
		void initiate_ShouldCompleteWithWallet_WhenBalanceSufficient() {
			// Given
			Long userId = 1L;
			Long orderId = 100L;
			Money requestAmount = Money.of(5000);
			Money walletBalance = Money.of(10000); // 예치금 > 요청금액
			Long savedPaymentId = 999L;

			Wallet wallet = Wallet.create(userId, walletBalance);
			given(walletService.getWalletByMemberId(userId)).willReturn(wallet);

			// Payment 저장 시 ID 할당 mock
			given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> {
				Payment payment = invocation.getArgument(0);
				return payment.withId(savedPaymentId);
			});

			PaymentInitiateCommand command = new PaymentInitiateCommand(
				userId, orderId, requestAmount, PaymentType.FUNDING
			);

			// When
			PaymentInitiateResult result = paymentInitiateService.initiate(command);

			// Then
			assertThat(result.completed()).isTrue();
			assertThat(result.orderId()).isEqualTo(orderId);
			assertThat(result.walletUsed()).isEqualTo(requestAmount);
			assertThat(result.pgPaymentRequired()).isEqualTo(Money.zero());
			assertThat(result.paymentId()).isEqualTo(savedPaymentId);
			assertThat(result.pgOrderId()).isNull();

			// Payment 저장 검증 (PENDING 저장 + PAID 저장 = 2회)
			verify(paymentRepository, times(2)).save(any(Payment.class));
			// 예치금 차감 검증 (referenceId로 paymentId 사용)
			verify(walletService).withdraw(eq(userId), eq(requestAmount), eq("FUNDING_PAYMENT"), eq("PAYMENT"), eq(savedPaymentId));
		}

		@Test
		@DisplayName("예치금이 부족하면 INSUFFICIENT_WALLET_BALANCE 예외 발생")
		void initiate_ShouldThrowException_WhenBalanceInsufficient() {
			// Given
			Long userId = 1L;
			Long orderId = 100L;
			Money requestAmount = Money.of(50000);
			Money walletBalance = Money.of(30000); // 예치금 < 요청금액

			Wallet wallet = Wallet.create(userId, walletBalance);
			given(walletService.getWalletByMemberId(userId)).willReturn(wallet);

			PaymentInitiateCommand command = new PaymentInitiateCommand(
				userId, orderId, requestAmount, PaymentType.FUNDING
			);

			// When & Then
			assertThatThrownBy(() -> paymentInitiateService.initiate(command))
				.isInstanceOf(PaymentException.class)
				.satisfies(ex -> {
					PaymentException paymentEx = (PaymentException) ex;
					assertThat(paymentEx.getErrorCode()).isEqualTo(PaymentErrorCode.INSUFFICIENT_WALLET_BALANCE);
				});

			// 예치금 차감 안 됨
			verify(walletService, never()).withdraw(anyLong(), any(), anyString(), anyString(), anyLong());
			// Payment 생성 안 됨
			verify(paymentRepository, never()).save(any());
		}

		@Test
		@DisplayName("예치금이 0원이면 INSUFFICIENT_WALLET_BALANCE 예외 발생")
		void initiate_ShouldThrowException_WhenWalletEmpty() {
			// Given
			Long userId = 1L;
			Long orderId = 100L;
			Money requestAmount = Money.of(10000);
			Money walletBalance = Money.zero();

			Wallet wallet = Wallet.create(userId, walletBalance);
			given(walletService.getWalletByMemberId(userId)).willReturn(wallet);

			PaymentInitiateCommand command = new PaymentInitiateCommand(
				userId, orderId, requestAmount, PaymentType.FUNDING
			);

			// When & Then
			assertThatThrownBy(() -> paymentInitiateService.initiate(command))
				.isInstanceOf(PaymentException.class)
				.satisfies(ex -> {
					PaymentException paymentEx = (PaymentException) ex;
					assertThat(paymentEx.getErrorCode()).isEqualTo(PaymentErrorCode.INSUFFICIENT_WALLET_BALANCE);
				});

			// 예치금 차감 안 됨
			verify(walletService, never()).withdraw(anyLong(), any(), anyString(), anyString(), anyLong());
			// Payment 생성 안 됨
			verify(paymentRepository, never()).save(any());
		}

		@Test
		@DisplayName("예치금과 요청금액이 정확히 같으면 예치금으로 완납")
		void initiate_ShouldCompleteWithWallet_WhenBalanceEqualsRequest() {
			// Given
			Long userId = 1L;
			Long orderId = 100L;
			Money amount = Money.of(10000);
			Long savedPaymentId = 888L;

			Wallet wallet = Wallet.create(userId, amount);
			given(walletService.getWalletByMemberId(userId)).willReturn(wallet);

			// Payment 저장 시 ID 할당 mock
			given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> {
				Payment payment = invocation.getArgument(0);
				return payment.withId(savedPaymentId);
			});

			PaymentInitiateCommand command = new PaymentInitiateCommand(
				userId, orderId, amount, PaymentType.FUNDING
			);

			// When
			PaymentInitiateResult result = paymentInitiateService.initiate(command);

			// Then
			assertThat(result.completed()).isTrue();
			assertThat(result.orderId()).isEqualTo(orderId);
			assertThat(result.walletUsed()).isEqualTo(amount);
			assertThat(result.pgPaymentRequired()).isEqualTo(Money.zero());
			assertThat(result.paymentId()).isEqualTo(savedPaymentId);
		}
	}

	@Nested
	@DisplayName("rollbackWallet 테스트")
	class RollbackWalletTest {

		@Test
		@DisplayName("금액이 있으면 예치금을 복구한다")
		void rollbackWallet_ShouldChargeWallet_WhenAmountPositive() {
			// Given
			Long userId = 1L;
			Money amount = Money.of(30000);
			Long paymentId = 100L;

			// When
			paymentInitiateService.rollbackWallet(userId, amount, paymentId);

			// Then
			verify(walletService).charge(
				eq(userId),
				eq(amount),
				eq("PAYMENT_ROLLBACK"),
				eq("PAYMENT"),  // referenceType은 PAYMENT 레코드를 참조
				eq(paymentId)
			);
		}

		@Test
		@DisplayName("금액이 0원이면 복구하지 않는다")
		void rollbackWallet_ShouldNotCharge_WhenAmountZero() {
			// Given
			Long userId = 1L;
			Money amount = Money.zero();
			Long paymentId = 100L;

			// When
			paymentInitiateService.rollbackWallet(userId, amount, paymentId);

			// Then
			verify(walletService, never()).charge(anyLong(), any(), anyString(), anyString(), anyLong());
		}

		@Test
		@DisplayName("금액이 null이면 복구하지 않는다")
		void rollbackWallet_ShouldNotCharge_WhenAmountNull() {
			// Given
			Long userId = 1L;
			Long paymentId = 100L;

			// When
			paymentInitiateService.rollbackWallet(userId, null, paymentId);

			// Then
			verify(walletService, never()).charge(anyLong(), any(), anyString(), anyString(), anyLong());
		}
	}
}
