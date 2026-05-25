package app.giftify.wallet.domain;

import app.giftify.support.common.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Wallet 도메인 테스트")
class WalletTest {

	@Nested
	@DisplayName("create 메서드")
	class CreateTests {

		@Test
		@DisplayName("정상적으로 지갑을 생성한다")
		void create_Success() {
			// given
			Long memberId = 1L;
			Money initialBalance = Money.of(10000);

			// when
			Wallet wallet = Wallet.create(memberId, initialBalance);

			// then
			assertThat(wallet.getId()).isNull();
			assertThat(wallet.getMemberId()).isEqualTo(memberId);
			assertThat(wallet.getBalance()).isEqualTo(initialBalance);
		}

		@Test
		@DisplayName("memberId가 null이면 예외가 발생한다")
		void create_NullMemberId_ThrowsException() {
			// given
			Money balance = Money.of(10000);

			// when & then
			assertThatThrownBy(() -> Wallet.create(null, balance))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("memberId");
		}

		@Test
		@DisplayName("balance가 null이면 예외가 발생한다")
		void create_NullBalance_ThrowsException() {
			// given
			Long memberId = 1L;

			// when & then
			assertThatThrownBy(() -> Wallet.create(memberId, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("balance");
		}
	}

	@Nested
	@DisplayName("charge 메서드")
	class ChargeTests {

		@Test
		@DisplayName("정상적으로 충전한다")
		void charge_Success() {
			// given
			Wallet wallet = Wallet.create(1L, Money.of(10000));
			Money chargeAmount = Money.of(5000);

			// when
			wallet.charge(chargeAmount);

			// then
			assertThat(wallet.getBalance()).isEqualTo(Money.of(15000));
		}

		@Test
		@DisplayName("최소 충전 금액(1000원) 이상이면 충전된다")
		void charge_MinimumAmount_Success() {
			// given
			Wallet wallet = Wallet.create(1L, Money.zero());
			Money chargeAmount = Money.of(1000);

			// when
			wallet.charge(chargeAmount);

			// then
			assertThat(wallet.getBalance()).isEqualTo(Money.of(1000));
		}

		@Test
		@DisplayName("최소 충전 금액 미만이면 예외가 발생한다")
		void charge_BelowMinimum_ThrowsException() {
			// given
			Wallet wallet = Wallet.create(1L, Money.zero());
			Money chargeAmount = Money.of(999);

			// when & then
			assertThatThrownBy(() -> wallet.charge(chargeAmount))
				.isInstanceOf(WalletException.class)
				.extracting("errorCode")
				.isEqualTo(WalletErrorCode.CHARGE_AMOUNT_BELOW_MINIMUM);
		}

		@Test
		@DisplayName("충전 금액이 null이면 예외가 발생한다")
		void charge_NullAmount_ThrowsException() {
			// given
			Wallet wallet = Wallet.create(1L, Money.zero());

			// when & then
			assertThatThrownBy(() -> wallet.charge(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("amount");
		}
	}

	@Nested
	@DisplayName("withdraw 메서드")
	class WithdrawTests {

		@Test
		@DisplayName("정상적으로 출금한다")
		void withdraw_Success() {
			// given
			Wallet wallet = Wallet.create(1L, Money.of(10000));
			Money withdrawAmount = Money.of(5000);

			// when
			wallet.withdraw(withdrawAmount);

			// then
			assertThat(wallet.getBalance()).isEqualTo(Money.of(5000));
		}

		@Test
		@DisplayName("전액 출금이 가능하다")
		void withdraw_FullBalance_Success() {
			// given
			Wallet wallet = Wallet.create(1L, Money.of(10000));
			Money withdrawAmount = Money.of(10000);

			// when
			wallet.withdraw(withdrawAmount);

			// then
			assertThat(wallet.getBalance()).isEqualTo(Money.zero());
		}

		@Test
		@DisplayName("잔액보다 많은 금액을 출금하면 예외가 발생한다")
		void withdraw_InsufficientBalance_ThrowsException() {
			// given
			Wallet wallet = Wallet.create(1L, Money.of(5000));
			Money withdrawAmount = Money.of(10000);

			// when & then
			assertThatThrownBy(() -> wallet.withdraw(withdrawAmount))
				.isInstanceOf(WalletException.class)
				.extracting("errorCode")
				.isEqualTo(WalletErrorCode.INSUFFICIENT_BALANCE);
		}

		@Test
		@DisplayName("출금 금액이 null이면 예외가 발생한다")
		void withdraw_NullAmount_ThrowsException() {
			// given
			Wallet wallet = Wallet.create(1L, Money.of(10000));

			// when & then
			assertThatThrownBy(() -> wallet.withdraw(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("amount");
		}
	}

	@Nested
	@DisplayName("deductForPayment 메서드")
	class DeductForPaymentTests {

		@Test
		@DisplayName("정상적으로 결제 차감한다")
		void deductForPayment_Success() {
			// given
			Wallet wallet = Wallet.create(1L, Money.of(10000));
			Money paymentAmount = Money.of(3000);

			// when
			wallet.deductForPayment(paymentAmount);

			// then
			assertThat(wallet.getBalance()).isEqualTo(Money.of(7000));
		}

		@Test
		@DisplayName("잔액보다 많은 금액을 차감하면 예외가 발생한다")
		void deductForPayment_InsufficientBalance_ThrowsException() {
			// given
			Wallet wallet = Wallet.create(1L, Money.of(5000));
			Money paymentAmount = Money.of(10000);

			// when & then
			assertThatThrownBy(() -> wallet.deductForPayment(paymentAmount))
				.isInstanceOf(WalletException.class)
				.extracting("errorCode")
				.isEqualTo(WalletErrorCode.INSUFFICIENT_BALANCE);
		}
	}

	@Nested
	@DisplayName("snapshot 및 restore 메서드")
	class SnapshotRestoreTests {

		@Test
		@DisplayName("스냅샷을 생성하고 복원한다")
		void snapshotAndRestore_Success() {
			// given
			Wallet original = Wallet.create(1L, Money.of(10000));

			// when
			WalletSnapshot snapshot = original.snapshot();
			Wallet restored = Wallet.restore(snapshot);

			// then
			assertThat(restored.getMemberId()).isEqualTo(original.getMemberId());
			assertThat(restored.getBalance()).isEqualTo(original.getBalance());
		}

		@Test
		@DisplayName("null 스냅샷으로 복원하면 예외가 발생한다")
		void restore_NullSnapshot_ThrowsException() {
			// when & then
			assertThatThrownBy(() -> Wallet.restore(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("snapshot");
		}
	}
}
