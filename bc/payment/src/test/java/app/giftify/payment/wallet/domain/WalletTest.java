package app.giftify.payment.wallet.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import app.giftify.shared.domain.vo.Money;
import app.giftify.wallet.domain.Wallet;
import app.giftify.wallet.domain.WalletErrorCode;
import app.giftify.wallet.domain.WalletException;
import app.giftify.wallet.domain.WalletSnapshot;

class WalletTest {

	private static final Long MEMBER_ID = 1L;

	private Wallet createWalletWithBalance(long balance) {
		return Wallet.create(MEMBER_ID, Money.of(balance));
	}

	@Nested
	@DisplayName("Given 잔액이 있는 지갑")
	class Given_Wallet_With_Balance {

		@Nested
		@DisplayName("When 충전을 시도하면")
		class When_Charge {

			@Test
			@DisplayName("Then 잔액이 증가한다")
			void Then_Balance_Increased() {
				Wallet wallet = createWalletWithBalance(5000);
				Money chargeAmount = Money.of(10000);

				wallet.charge(chargeAmount);

				assertThat(wallet.getBalance()).isEqualTo(Money.of(15000));
			}
		}

		@Nested
		@DisplayName("When 잔액 내에서 출금을 시도하면")
		class When_Withdraw_Valid_Amount {

			@Test
			@DisplayName("Then 잔액이 감소한다")
			void Then_Balance_Decreased() {
				Wallet wallet = createWalletWithBalance(10000);
				Money withdrawAmount = Money.of(3000);

				wallet.withdraw(withdrawAmount);

				assertThat(wallet.getBalance()).isEqualTo(Money.of(7000));
			}
		}

		@Nested
		@DisplayName("When 잔액을 초과하여 출금을 시도하면")
		class When_Withdraw_Exceeds_Balance {

			@Test
			@DisplayName("Then WalletException(INSUFFICIENT_BALANCE)이 발생한다")
			void Then_Exception() {
				Wallet wallet = createWalletWithBalance(5000);
				Money withdrawAmount = Money.of(10000);

				assertThatThrownBy(() -> wallet.withdraw(withdrawAmount))
					.isInstanceOf(WalletException.class)
					.hasFieldOrPropertyWithValue("errorCode", WalletErrorCode.INSUFFICIENT_BALANCE);
			}
		}
	}

	@Nested
	@DisplayName("Given 지갑")
	class Given_Wallet {

		@Nested
		@DisplayName("When 1000원 미만으로 충전을 시도하면")
		class When_Charge_Below_Minimum {

			@Test
			@DisplayName("Then WalletException(CHARGE_AMOUNT_BELOW_MINIMUM)이 발생한다")
			void Then_Exception() {
				Wallet wallet = createWalletWithBalance(0);
				Money invalidChargeAmount = Money.of(999);

				assertThatThrownBy(() -> wallet.charge(invalidChargeAmount))
					.isInstanceOf(WalletException.class)
					.hasFieldOrPropertyWithValue("errorCode", WalletErrorCode.CHARGE_AMOUNT_BELOW_MINIMUM);
			}
		}

		@Nested
		@DisplayName("When null 금액으로 충전을 시도하면")
		class When_Charge_With_Null_Amount {

			@Test
			@DisplayName("Then IllegalArgumentException이 발생한다")
			void Then_Exception() {
				Wallet wallet = createWalletWithBalance(0);

				assertThatThrownBy(() -> wallet.charge(null))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("amount은(는) null일 수 없습니다");
			}
		}

		@Nested
		@DisplayName("When 결제 차감을 시도하면")
		class When_DeductForPayment {

			@Test
			@DisplayName("Then 잔액이 감소한다")
			void Then_Balance_Decreased() {
				Wallet wallet = createWalletWithBalance(20000);
				Money deductAmount = Money.of(8000);

				wallet.deductForPayment(deductAmount);

				assertThat(wallet.getBalance()).isEqualTo(Money.of(12000));
			}
		}
	}

	@Nested
	@DisplayName("Given 결제 차감 시나리오")
	class Given_DeductForPayment_Scenarios {

		@Nested
		@DisplayName("When 잔액보다 큰 금액으로 결제 차감을 시도하면")
		class When_Deduct_Exceeds_Balance {

			@Test
			@DisplayName("Then WalletException(INSUFFICIENT_BALANCE)이 발생한다")
			void Then_Exception() {
				Wallet wallet = createWalletWithBalance(3000);
				Money deductAmount = Money.of(5000);

				assertThatThrownBy(() -> wallet.deductForPayment(deductAmount))
					.isInstanceOf(WalletException.class)
					.hasFieldOrPropertyWithValue("errorCode", WalletErrorCode.INSUFFICIENT_BALANCE);
			}
		}

		@Nested
		@DisplayName("When null 금액으로 결제 차감을 시도하면")
		class When_Deduct_With_Null_Amount {

			@Test
			@DisplayName("Then IllegalArgumentException이 발생한다")
			void Then_Exception() {
				Wallet wallet = createWalletWithBalance(10000);

				assertThatThrownBy(() -> wallet.deductForPayment(null))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("amount은(는) null일 수 없습니다");
			}
		}
	}

	@Nested
	@DisplayName("Given 출금 시나리오")
	class Given_Withdraw_Scenarios {

		@Nested
		@DisplayName("When null 금액으로 출금을 시도하면")
		class When_Withdraw_With_Null_Amount {

			@Test
			@DisplayName("Then IllegalArgumentException이 발생한다")
			void Then_Exception() {
				Wallet wallet = createWalletWithBalance(10000);

				assertThatThrownBy(() -> wallet.withdraw(null))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("amount은(는) null일 수 없습니다");
			}
		}
	}

	@Nested
	@DisplayName("Given 지갑 생성")
	class Given_Wallet_Creation {

		@Test
		@DisplayName("초기 잔액으로 지갑을 생성할 수 있다")
		void Can_Create_Wallet_With_Initial_Balance() {
			Money initialBalance = Money.of(50000);

			Wallet wallet = Wallet.create(MEMBER_ID, initialBalance);

			assertThat(wallet.getMemberId()).isEqualTo(MEMBER_ID);
			assertThat(wallet.getBalance()).isEqualTo(initialBalance);
		}

		@Test
		@DisplayName("memberId가 null이면 IllegalArgumentException이 발생한다")
		void Cannot_Create_Wallet_With_Null_MemberId() {
			assertThatThrownBy(() -> Wallet.create(null, Money.of(0)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("memberId은(는) null일 수 없습니다");
		}

		@Test
		@DisplayName("balance가 null이면 IllegalArgumentException이 발생한다")
		void Cannot_Create_Wallet_With_Null_Balance() {
			assertThatThrownBy(() -> Wallet.create(MEMBER_ID, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("balance은(는) null일 수 없습니다");
		}
	}

	@Nested
	@DisplayName("Given 지갑 스냅샷")
	class Given_Wallet_Snapshot {

		@Test
		@DisplayName("현재 상태를 스냅샷으로 변환할 수 있다")
		void Can_Create_Snapshot() {
			Wallet wallet = Wallet.create(MEMBER_ID, Money.of(10000));

			WalletSnapshot snapshot = wallet.snapshot();

			assertThat(snapshot.memberId()).isEqualTo(MEMBER_ID);
			assertThat(snapshot.balance()).isEqualTo(Money.of(10000));
		}

		@Test
		@DisplayName("스냅샷으로부터 지갑을 복원할 수 있다")
		void Can_Restore_From_Snapshot() {
			WalletSnapshot snapshot = new WalletSnapshot(1L, MEMBER_ID, Money.of(20000));

			Wallet wallet = Wallet.restore(snapshot);

			assertThat(wallet.getId()).isEqualTo(1L);
			assertThat(wallet.getMemberId()).isEqualTo(MEMBER_ID);
			assertThat(wallet.getBalance()).isEqualTo(Money.of(20000));
		}

		@Test
		@DisplayName("null 스냅샷으로 복원 시 IllegalArgumentException이 발생한다")
		void Cannot_Restore_From_Null_Snapshot() {
			assertThatThrownBy(() -> Wallet.restore(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("snapshot은(는) null일 수 없습니다");
		}
	}
}
