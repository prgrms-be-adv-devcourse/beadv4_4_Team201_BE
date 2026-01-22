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

import app.giftify.shared.domain.event.payment.PaymentType;
import app.giftify.shared.domain.vo.Money;
import domain.payment.PaymentErrorCode;
import domain.payment.PaymentException;
import domain.payment.PaymentPolicy;
import domain.payment.PaymentRepository;
import domain.wallet.Wallet;
import payment.usecase.command.FundingContributeCommand;
import payment.usecase.result.FundingContributeResult;
import wallet.service.WalletService;

@ExtendWith(MockitoExtension.class)
@DisplayName("FundingPaymentService 테스트")
class FundingPaymentServiceTest {

	@Mock
	private WalletService walletService;

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private PaymentPolicy fundingPolicy;

	private FundingPaymentService fundingPaymentService;

	@BeforeEach
	void setUp() {
		fundingPaymentService = new FundingPaymentService(
			walletService,
			paymentRepository,
			List.of(fundingPolicy)
		);
	}

	@Nested
	@DisplayName("contribute 테스트")
	class ContributeTest {

		@BeforeEach
		void setUpPolicy() {
			// contribute 테스트에서만 펀딩 정책 stubbing 필요
			given(fundingPolicy.support(PaymentType.FUNDING)).willReturn(true);
		}

		@Test
		@DisplayName("예치금이 충분하면 예치금으로 완납하고 completed=true 반환")
		void contribute_ShouldCompleteWithWallet_WhenBalanceSufficient() {
			// Given
			Long userId = 1L;
			Money requestAmount = Money.of(5000);
			Money walletBalance = Money.of(10000); // 예치금 > 요청금액

			Wallet wallet = Wallet.create(userId, walletBalance);
			given(walletService.getWalletByMemberId(userId)).willReturn(wallet);

			FundingContributeCommand command = new FundingContributeCommand(userId, requestAmount);

			// When
			FundingContributeResult result = fundingPaymentService.contribute(command);

			// Then
			assertThat(result.completed()).isTrue();
			assertThat(result.walletUsed()).isEqualTo(requestAmount);
			assertThat(result.pgPaymentRequired()).isEqualTo(Money.zero());
			assertThat(result.paymentId()).isNull();
			assertThat(result.orderId()).isNull();

			// 예치금 차감 검증
			verify(walletService).withdraw(eq(userId), eq(requestAmount), anyString(), anyString(), anyLong());
			// Payment 생성 안 됨
			verify(paymentRepository, never()).save(any());
		}

		@Test
		@DisplayName("예치금이 부족하면 INSUFFICIENT_WALLET_BALANCE 예외 발생")
		void contribute_ShouldThrowException_WhenBalanceInsufficient() {
			// Given
			Long userId = 1L;
			Money requestAmount = Money.of(50000);
			Money walletBalance = Money.of(30000); // 예치금 < 요청금액

			Wallet wallet = Wallet.create(userId, walletBalance);
			given(walletService.getWalletByMemberId(userId)).willReturn(wallet);

			FundingContributeCommand command = new FundingContributeCommand(userId, requestAmount);

			// When & Then
			assertThatThrownBy(() -> fundingPaymentService.contribute(command))
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
		void contribute_ShouldThrowException_WhenWalletEmpty() {
			// Given
			Long userId = 1L;
			Money requestAmount = Money.of(10000);
			Money walletBalance = Money.zero();

			Wallet wallet = Wallet.create(userId, walletBalance);
			given(walletService.getWalletByMemberId(userId)).willReturn(wallet);

			FundingContributeCommand command = new FundingContributeCommand(userId, requestAmount);

			// When & Then
			assertThatThrownBy(() -> fundingPaymentService.contribute(command))
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
		void contribute_ShouldCompleteWithWallet_WhenBalanceEqualsRequest() {
			// Given
			Long userId = 1L;
			Money amount = Money.of(10000);

			Wallet wallet = Wallet.create(userId, amount);
			given(walletService.getWalletByMemberId(userId)).willReturn(wallet);

			FundingContributeCommand command = new FundingContributeCommand(userId, amount);

			// When
			FundingContributeResult result = fundingPaymentService.contribute(command);

			// Then
			assertThat(result.completed()).isTrue();
			assertThat(result.walletUsed()).isEqualTo(amount);
			assertThat(result.pgPaymentRequired()).isEqualTo(Money.zero());
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
			fundingPaymentService.rollbackWallet(userId, amount, paymentId);

			// Then
			verify(walletService).charge(
				eq(userId),
				eq(amount),
				eq("FUNDING_ROLLBACK"),
				eq("FUNDING_ROLLBACK"),
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
			fundingPaymentService.rollbackWallet(userId, amount, paymentId);

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
			fundingPaymentService.rollbackWallet(userId, null, paymentId);

			// Then
			verify(walletService, never()).charge(anyLong(), any(), anyString(), anyString(), anyLong());
		}
	}
}
