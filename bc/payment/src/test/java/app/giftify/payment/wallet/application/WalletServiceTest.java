package app.giftify.payment.wallet.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.wallet.application.WalletService;
import app.giftify.wallet.application.inbound.ChargeWalletCommand;
import app.giftify.wallet.application.inbound.ChargeWalletResult;
import app.giftify.wallet.application.inbound.WalletBalanceResult;
import app.giftify.wallet.application.inbound.WithdrawWalletCommand;
import app.giftify.wallet.application.inbound.WithdrawWalletResult;
import app.giftify.wallet.application.inbound.WithdrawStatus;
import app.giftify.wallet.application.outbound.WalletHistoryRepository;
import app.giftify.wallet.application.outbound.WalletRepository;
import app.giftify.wallet.domain.ReferenceType;
import app.giftify.wallet.domain.Wallet;
import app.giftify.wallet.domain.WalletErrorCode;
import app.giftify.wallet.domain.WalletException;
import app.giftify.wallet.domain.WalletHistory;
import app.giftify.wallet.domain.WalletSnapshot;
import app.giftify.wallet.domain.event.WalletChargedEvent;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.vo.Money;

/**
 * WalletService 단위 테스트.
 *
 * <p>지갑 충전, 출금, 조회 비즈니스 로직을 검증합니다.</p>
 */
@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

	@Mock
	private WalletRepository walletRepository;

	@Mock
	private WalletHistoryRepository historyRepository;

	@Mock
	private EventPublisher eventPublisher;

	@InjectMocks
	private WalletService sut;

	// ========== 테스트 픽스처 ========== //

	private static final Long MEMBER_ID = 100L;
	private static final String CHARGE_ORDER_ID = "CHARGE-100-123456789";
	private static final String PAYMENT_KEY = "toss-payment-key-123";
	private static final Money CHARGE_AMOUNT = Money.of(10000);
	private static final Money INITIAL_BALANCE = Money.of(50000);
	private static final String BANK_CODE = "004";
	private static final String ACCOUNT_NUMBER = "1234567890";

	private Wallet createWallet(Long walletId, Long memberId, Money balance) {
		return Wallet.restore(new WalletSnapshot(
			walletId,
			memberId,
			balance
		));
	}

	// ========== Charge Tests ========== //

	@Nested
	@DisplayName("Given 유효한 충전 요청")
	class Given_Valid_Charge_Request {

		@Nested
		@DisplayName("When charge 호출하면")
		class When_Charge_Called {

			@Test
			@DisplayName("Then 지갑 잔액이 증가하고 이벤트가 발행된다")
			void Then_Balance_Increased_And_Event_Published() {
				// given
				ChargeWalletCommand command = new ChargeWalletCommand(
					MEMBER_ID,
					CHARGE_AMOUNT,
					PAYMENT_KEY,
					CHARGE_ORDER_ID
				);

				Wallet wallet = createWallet(1L, MEMBER_ID, INITIAL_BALANCE);
				Money expectedBalance = INITIAL_BALANCE.plus(CHARGE_AMOUNT);

				given(historyRepository.existsByReferenceIdAndReferenceType(
					CHARGE_ORDER_ID, ReferenceType.CHARGE)).willReturn(false);
				given(walletRepository.findByMemberId(MEMBER_ID)).willReturn(Optional.of(wallet));
				given(walletRepository.save(any(Wallet.class))).willAnswer(invocation -> invocation.getArgument(0));

				// when
				ChargeWalletResult result = sut.charge(command);

				// then
				assertThat(result.memberId()).isEqualTo(MEMBER_ID);
				assertThat(result.chargedAmount()).isEqualTo(CHARGE_AMOUNT);
				assertThat(result.balanceAfter()).isEqualTo(expectedBalance);
				assertThat(result.transactionId()).isEqualTo(CHARGE_ORDER_ID);

				verify(walletRepository).save(any(Wallet.class));
				verify(historyRepository).record(any(WalletHistory.class));
				verify(eventPublisher).publish(any(WalletChargedEvent.class));
			}
		}
	}

	@Nested
	@DisplayName("Given 지갑이 없는 신규 회원")
	class Given_New_Member_Without_Wallet {

		@Nested
		@DisplayName("When charge 호출하면")
		class When_Charge_Called {

			@Test
			@DisplayName("Then 새 지갑이 생성되고 충전된다")
			void Then_New_Wallet_Created_And_Charged() {
				// given
				ChargeWalletCommand command = new ChargeWalletCommand(
					MEMBER_ID,
					CHARGE_AMOUNT,
					PAYMENT_KEY,
					CHARGE_ORDER_ID
				);

				given(historyRepository.existsByReferenceIdAndReferenceType(
					CHARGE_ORDER_ID, ReferenceType.CHARGE)).willReturn(false);
				given(walletRepository.findByMemberId(MEMBER_ID)).willReturn(Optional.empty());
				given(walletRepository.save(any(Wallet.class))).willAnswer(invocation -> {
					Wallet savedWallet = invocation.getArgument(0);
					return Wallet.restore(new WalletSnapshot(
						1L,
						savedWallet.getMemberId(),
						savedWallet.getBalance()
					));
				});

				// when
				ChargeWalletResult result = sut.charge(command);

				// then
				assertThat(result.memberId()).isEqualTo(MEMBER_ID);
				assertThat(result.balanceAfter()).isEqualTo(CHARGE_AMOUNT);
				verify(walletRepository).save(any(Wallet.class));
			}
		}
	}

	@Nested
	@DisplayName("Given 중복된 충전 거래")
	class Given_Duplicate_Transaction {

		@Nested
		@DisplayName("When charge 호출하면")
		class When_Charge_Called {

			@Test
			@DisplayName("Then DUPLICATED_TRANSACTION 예외가 발생한다")
			void Then_Throws_DUPLICATED_TRANSACTION_Exception() {
				// given
				ChargeWalletCommand command = new ChargeWalletCommand(
					MEMBER_ID,
					CHARGE_AMOUNT,
					PAYMENT_KEY,
					CHARGE_ORDER_ID
				);

				given(historyRepository.existsByReferenceIdAndReferenceType(
					CHARGE_ORDER_ID, ReferenceType.CHARGE)).willReturn(true);

				// when & then
				assertThatThrownBy(() -> sut.charge(command))
					.isInstanceOf(WalletException.class)
					.hasFieldOrPropertyWithValue("errorCode", WalletErrorCode.DUPLICATED_TRANSACTION)
					.hasMessageContaining("중복된 충전 거래");

				verify(walletRepository, never()).save(any(Wallet.class));
				verify(eventPublisher, never()).publish(any());
			}
		}
	}

	// ========== Withdraw Tests ========== //

	@Nested
	@DisplayName("Given 충분한 잔액을 가진 지갑")
	class Given_Wallet_With_Sufficient_Balance {

		@Nested
		@DisplayName("When withdraw 호출하면")
		class When_Withdraw_Called {

			@Test
			@DisplayName("Then 출금이 성공하고 잔액이 감소한다")
			void Then_Withdraw_Success_And_Balance_Decreased() {
				// given
				Money withdrawAmount = Money.of(10000);
				WithdrawWalletCommand command = new WithdrawWalletCommand(
					MEMBER_ID, withdrawAmount, BANK_CODE, ACCOUNT_NUMBER);

				Wallet wallet = createWallet(1L, MEMBER_ID, INITIAL_BALANCE);
				Money expectedBalance = INITIAL_BALANCE.minus(withdrawAmount);

				given(walletRepository.findByMemberId(MEMBER_ID)).willReturn(Optional.of(wallet));
				given(walletRepository.save(any(Wallet.class))).willAnswer(invocation -> invocation.getArgument(0));

				// when
				WithdrawWalletResult result = sut.withdraw(command);

				// then
				assertThat(result.memberId()).isEqualTo(MEMBER_ID);
				assertThat(result.withdrawnAmount()).isEqualTo(withdrawAmount);
				assertThat(result.balanceAfter()).isEqualTo(expectedBalance);
				assertThat(result.status()).isEqualTo(WithdrawStatus.PENDING);
				assertThat(result.transactionId()).isNotNull();

				verify(walletRepository).save(any(Wallet.class));
				verify(historyRepository).record(any(WalletHistory.class));
			}
		}
	}

	@Nested
	@DisplayName("Given 잔액이 부족한 지갑")
	class Given_Wallet_With_Insufficient_Balance {

		@Nested
		@DisplayName("When withdraw 호출하면")
		class When_Withdraw_Called {

			@Test
			@DisplayName("Then INSUFFICIENT_BALANCE 예외가 발생한다")
			void Then_Throws_INSUFFICIENT_BALANCE_Exception() {
				// given
				Money withdrawAmount = Money.of(100000); // 잔액보다 큰 금액
				WithdrawWalletCommand command = new WithdrawWalletCommand(
					MEMBER_ID, withdrawAmount, BANK_CODE, ACCOUNT_NUMBER);

				Wallet wallet = createWallet(1L, MEMBER_ID, INITIAL_BALANCE);

				given(walletRepository.findByMemberId(MEMBER_ID)).willReturn(Optional.of(wallet));

				// when & then
				assertThatThrownBy(() -> sut.withdraw(command))
					.isInstanceOf(WalletException.class)
					.hasFieldOrPropertyWithValue("errorCode", WalletErrorCode.INSUFFICIENT_BALANCE);

				verify(walletRepository, never()).save(any(Wallet.class));
			}
		}
	}

	@Nested
	@DisplayName("Given 지갑이 없는 회원")
	class Given_Member_Without_Wallet {

		@Nested
		@DisplayName("When withdraw 호출하면")
		class When_Withdraw_Called {

			@Test
			@DisplayName("Then WALLET_NOT_FOUND 예외가 발생한다")
			void Then_Throws_WALLET_NOT_FOUND_Exception() {
				// given
				Money withdrawAmount = Money.of(10000);
				WithdrawWalletCommand command = new WithdrawWalletCommand(
					MEMBER_ID, withdrawAmount, BANK_CODE, ACCOUNT_NUMBER);

				given(walletRepository.findByMemberId(MEMBER_ID)).willReturn(Optional.empty());

				// when & then
				assertThatThrownBy(() -> sut.withdraw(command))
					.isInstanceOf(WalletException.class)
					.hasFieldOrPropertyWithValue("errorCode", WalletErrorCode.WALLET_NOT_FOUND)
					.hasMessageContaining("지갑을 찾을 수 없습니다");
			}
		}
	}

	// ========== GetBalance Tests ========== //

	@Nested
	@DisplayName("Given 기존 지갑을 가진 회원")
	class Given_Member_With_Existing_Wallet {

		@Nested
		@DisplayName("When getBalance 호출하면")
		class When_GetBalance_Called {

			@Test
			@DisplayName("Then 현재 잔액이 반환된다")
			void Then_Current_Balance_Returned() {
				// given
				Wallet wallet = createWallet(1L, MEMBER_ID, INITIAL_BALANCE);
				given(walletRepository.findByMemberId(MEMBER_ID)).willReturn(Optional.of(wallet));

				// when
				WalletBalanceResult result = sut.getBalance(MEMBER_ID);

				// then
				assertThat(result.memberId()).isEqualTo(MEMBER_ID);
				assertThat(result.balance()).isEqualTo(INITIAL_BALANCE);
				assertThat(result.walletId()).isEqualTo(1L);
			}
		}
	}

	@Nested
	@DisplayName("Given 지갑이 없는 신규 회원")
	class Given_New_Member_Without_Wallet_For_Balance {

		@Nested
		@DisplayName("When getBalance 호출하면")
		class When_GetBalance_Called {

			@Test
			@DisplayName("Then 잔액 0원이 반환된다")
			void Then_Returns_Zero_Balance() {
				// given
				given(walletRepository.findByMemberId(MEMBER_ID)).willReturn(Optional.empty());

				// when
				WalletBalanceResult result = sut.getBalance(MEMBER_ID);

				// then
				assertThat(result.memberId()).isEqualTo(MEMBER_ID);
				assertThat(result.balance()).isEqualTo(Money.zero());
				assertThat(result.walletId()).isNull();
			}
		}
	}
}
