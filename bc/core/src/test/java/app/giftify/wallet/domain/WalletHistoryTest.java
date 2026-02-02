package app.giftify.wallet.domain;

import app.giftify.shared.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("WalletHistory 도메인 테스트")
class WalletHistoryTest {

	@Nested
	@DisplayName("create 메서드")
	class CreateTests {

		@Test
		@DisplayName("정상적으로 거래 이력을 생성한다")
		void create_Success() {
			// given
			Long walletId = 1L;
			TransactionType transactionType = TransactionType.CHARGE;
			Money amount = Money.of(10000);
			Money balanceAfter = Money.of(10000);
			ReferenceType referenceType = ReferenceType.CHARGE;
			String referenceId = "charge-123";
			LocalDateTime occurredAt = LocalDateTime.now();

			// when
			WalletHistory history = WalletHistory.create(
				walletId, transactionType, amount, balanceAfter, referenceType, referenceId, occurredAt
			);

			// then
			assertThat(history.getId()).isNull();
			assertThat(history.getWalletId()).isEqualTo(walletId);
			assertThat(history.getTransactionType()).isEqualTo(transactionType);
			assertThat(history.getAmount()).isEqualTo(amount);
			assertThat(history.getBalanceAfter()).isEqualTo(balanceAfter);
			assertThat(history.getReferenceType()).isEqualTo(referenceType);
			assertThat(history.getReferenceId()).isEqualTo(referenceId);
			assertThat(history.getOccurredAt()).isEqualTo(occurredAt);
		}

		@Test
		@DisplayName("walletId가 null이면 예외가 발생한다")
		void create_NullWalletId_ThrowsException() {
			// given
			LocalDateTime occurredAt = LocalDateTime.now();

			// when & then
			assertThatThrownBy(() -> WalletHistory.create(
				null, TransactionType.CHARGE, Money.of(10000), Money.of(10000),
				ReferenceType.CHARGE, "ref-123", occurredAt
			))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("walletId");
		}

		@Test
		@DisplayName("transactionType이 null이면 예외가 발생한다")
		void create_NullTransactionType_ThrowsException() {
			// given
			LocalDateTime occurredAt = LocalDateTime.now();

			// when & then
			assertThatThrownBy(() -> WalletHistory.create(
				1L, null, Money.of(10000), Money.of(10000),
				ReferenceType.CHARGE, "ref-123", occurredAt
			))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("transactionType");
		}

		@Test
		@DisplayName("amount가 null이면 예외가 발생한다")
		void create_NullAmount_ThrowsException() {
			// given
			LocalDateTime occurredAt = LocalDateTime.now();

			// when & then
			assertThatThrownBy(() -> WalletHistory.create(
				1L, TransactionType.CHARGE, null, Money.of(10000),
				ReferenceType.CHARGE, "ref-123", occurredAt
			))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("amount");
		}

		@Test
		@DisplayName("balanceAfter가 null이면 예외가 발생한다")
		void create_NullBalanceAfter_ThrowsException() {
			// given
			LocalDateTime occurredAt = LocalDateTime.now();

			// when & then
			assertThatThrownBy(() -> WalletHistory.create(
				1L, TransactionType.CHARGE, Money.of(10000), null,
				ReferenceType.CHARGE, "ref-123", occurredAt
			))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("balanceAfter");
		}

		@Test
		@DisplayName("referenceType이 null이면 예외가 발생한다")
		void create_NullReferenceType_ThrowsException() {
			// given
			LocalDateTime occurredAt = LocalDateTime.now();

			// when & then
			assertThatThrownBy(() -> WalletHistory.create(
				1L, TransactionType.CHARGE, Money.of(10000), Money.of(10000),
				null, "ref-123", occurredAt
			))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("referenceType");
		}

		@Test
		@DisplayName("referenceId가 null이면 예외가 발생한다")
		void create_NullReferenceId_ThrowsException() {
			// given
			LocalDateTime occurredAt = LocalDateTime.now();

			// when & then
			assertThatThrownBy(() -> WalletHistory.create(
				1L, TransactionType.CHARGE, Money.of(10000), Money.of(10000),
				ReferenceType.CHARGE, null, occurredAt
			))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("referenceId");
		}

		@Test
		@DisplayName("occurredAt가 null이면 예외가 발생한다")
		void create_NullOccurredAt_ThrowsException() {
			// when & then
			assertThatThrownBy(() -> WalletHistory.create(
				1L, TransactionType.CHARGE, Money.of(10000), Money.of(10000),
				ReferenceType.CHARGE, "ref-123", null
			))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("occurredAt");
		}
	}

	@Nested
	@DisplayName("restore 메서드")
	class RestoreTests {

		@Test
		@DisplayName("정상적으로 거래 이력을 복원한다")
		void restore_Success() {
			// given
			Long id = 1L;
			Long walletId = 1L;
			TransactionType transactionType = TransactionType.WITHDRAW;
			Money amount = Money.of(5000);
			Money balanceAfter = Money.of(5000);
			ReferenceType referenceType = ReferenceType.WITHDRAWAL;
			String referenceId = "withdraw-123";
			LocalDateTime occurredAt = LocalDateTime.now();

			// when
			WalletHistory history = WalletHistory.restore(
				id, walletId, transactionType, amount, balanceAfter, referenceType, referenceId, occurredAt
			);

			// then
			assertThat(history.getId()).isEqualTo(id);
			assertThat(history.getWalletId()).isEqualTo(walletId);
			assertThat(history.getTransactionType()).isEqualTo(transactionType);
			assertThat(history.getAmount()).isEqualTo(amount);
			assertThat(history.getBalanceAfter()).isEqualTo(balanceAfter);
		}
	}

	@Nested
	@DisplayName("거래 유형별 이력 생성 테스트")
	class TransactionTypeTests {

		@Test
		@DisplayName("CHARGE 타입 이력을 생성한다")
		void create_ChargeType() {
			// given & when
			WalletHistory history = WalletHistory.create(
				1L, TransactionType.CHARGE, Money.of(10000), Money.of(10000),
				ReferenceType.CHARGE, "charge-123", LocalDateTime.now()
			);

			// then
			assertThat(history.getTransactionType()).isEqualTo(TransactionType.CHARGE);
		}

		@Test
		@DisplayName("WITHDRAW 타입 이력을 생성한다")
		void create_WithdrawType() {
			// given & when
			WalletHistory history = WalletHistory.create(
				1L, TransactionType.WITHDRAW, Money.of(5000), Money.of(5000),
				ReferenceType.WITHDRAWAL, "withdraw-123", LocalDateTime.now()
			);

			// then
			assertThat(history.getTransactionType()).isEqualTo(TransactionType.WITHDRAW);
		}

		@Test
		@DisplayName("PAYMENT 타입 이력을 생성한다")
		void create_PaymentType() {
			// given & when
			WalletHistory history = WalletHistory.create(
				1L, TransactionType.PAYMENT, Money.of(3000), Money.of(7000),
				ReferenceType.PAYMENT, "payment-123", LocalDateTime.now()
			);

			// then
			assertThat(history.getTransactionType()).isEqualTo(TransactionType.PAYMENT);
		}
	}
}
