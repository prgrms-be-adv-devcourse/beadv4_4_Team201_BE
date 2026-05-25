package app.giftify.wallet.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.support.common.money.Money;
import app.giftify.wallet.application.inbound.SettlementPayoutCommand;
import app.giftify.wallet.application.outbound.WalletHistoryRepository;
import app.giftify.wallet.application.outbound.WalletRepository;
import app.giftify.wallet.domain.ReferenceType;
import app.giftify.wallet.domain.TransactionType;
import app.giftify.wallet.domain.Wallet;
import app.giftify.wallet.domain.WalletException;
import app.giftify.wallet.domain.WalletSnapshot;

@ExtendWith(MockitoExtension.class)
class WalletSettlementServiceTest {

	@Mock
	WalletRepository walletRepository;

	@Mock
	WalletHistoryRepository historyRepository;

	@InjectMocks
	WalletSettlementService sut;

	@Nested
	@DisplayName("payout - 정산 지급 (양수 금액)")
	class PayoutCharge {

		@Test
		@DisplayName("판매자 지갑에 정산금을 지급하고 이력을 기록한다")
		void chargesWalletAndRecordsHistory() {
			// given
			Money payoutAmount = Money.of(50000);
			SettlementPayoutCommand command = new SettlementPayoutCommand(
				1L, 100L, payoutAmount, "evt-001"
			);

			Wallet wallet = Wallet.restore(new WalletSnapshot(1L, 100L, Money.of(10000)));
			given(walletRepository.findByMemberId(100L)).willReturn(Optional.of(wallet));
			given(walletRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
			given(historyRepository.existsByReferenceIdAndReferenceType("evt-001", ReferenceType.SETTLEMENT))
				.willReturn(false);

			// when
			sut.payout(command);

			// then
			assertThat(wallet.getBalance()).isEqualTo(Money.of(60000));

			verify(historyRepository).recordTransaction(
				wallet.getId(),
				TransactionType.SETTLEMENT_PAYOUT,
				Money.of(50000),
				Money.of(60000),
				ReferenceType.SETTLEMENT,
				"evt-001"
			);
		}

		@Test
		@DisplayName("지급 시 지갑이 없으면 WalletException을 던진다")
		void throwsWhenWalletNotFoundForCharge() {
			// given
			SettlementPayoutCommand command = new SettlementPayoutCommand(
				1L, 200L, Money.of(30000), "evt-002"
			);

			given(walletRepository.findByMemberId(200L)).willReturn(Optional.empty());
			given(historyRepository.existsByReferenceIdAndReferenceType("evt-002", ReferenceType.SETTLEMENT))
				.willReturn(false);

			// when & then
			assertThatThrownBy(() -> sut.payout(command))
				.isInstanceOf(WalletException.class);
		}
	}

	@Nested
	@DisplayName("payout - 정산 차감 (음수 금액)")
	class PayoutDeduct {

		@Test
		@DisplayName("판매자 지갑에서 환불분을 차감한다")
		void deductsFromWallet() {
			// given
			Money deductAmount = Money.of(-20000);
			SettlementPayoutCommand command = new SettlementPayoutCommand(
				2L, 100L, deductAmount, "evt-003"
			);

			Wallet wallet = Wallet.restore(new WalletSnapshot(1L, 100L, Money.of(50000)));
			given(walletRepository.findByMemberId(100L)).willReturn(Optional.of(wallet));
			given(walletRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
			given(historyRepository.existsByReferenceIdAndReferenceType("evt-003", ReferenceType.SETTLEMENT))
				.willReturn(false);

			// when
			sut.payout(command);

			// then
			assertThat(wallet.getBalance()).isEqualTo(Money.of(30000));
		}

		@Test
		@DisplayName("잔액 부족 시 WalletException을 던진다")
		void throwsWhenInsufficientBalance() {
			// given
			SettlementPayoutCommand command = new SettlementPayoutCommand(
				3L, 100L, Money.of(-100000), "evt-004"
			);

			Wallet wallet = Wallet.restore(new WalletSnapshot(1L, 100L, Money.of(5000)));
			given(walletRepository.findByMemberId(100L)).willReturn(Optional.of(wallet));
			given(historyRepository.existsByReferenceIdAndReferenceType("evt-004", ReferenceType.SETTLEMENT))
				.willReturn(false);

			// when & then
			assertThatThrownBy(() -> sut.payout(command))
				.isInstanceOf(WalletException.class);
		}

		@Test
		@DisplayName("차감 시 지갑이 없으면 WalletException을 던진다")
		void throwsWhenWalletNotFoundForDeduct() {
			// given
			SettlementPayoutCommand command = new SettlementPayoutCommand(
				4L, 999L, Money.of(-10000), "evt-005"
			);

			given(walletRepository.findByMemberId(999L)).willReturn(Optional.empty());
			given(historyRepository.existsByReferenceIdAndReferenceType("evt-005", ReferenceType.SETTLEMENT))
				.willReturn(false);

			// when & then
			assertThatThrownBy(() -> sut.payout(command))
				.isInstanceOf(WalletException.class);
		}
	}

	@Nested
	@DisplayName("payout - 멱등성")
	class Idempotency {

		@Test
		@DisplayName("중복 referenceId면 아무 작업 없이 반환한다")
		void skipsWhenDuplicate() {
			// given
			SettlementPayoutCommand command = new SettlementPayoutCommand(
				5L, 100L, Money.of(10000), "evt-dup"
			);

			given(historyRepository.existsByReferenceIdAndReferenceType("evt-dup", ReferenceType.SETTLEMENT))
				.willReturn(true);

			// when
			sut.payout(command);

			// then
			verify(walletRepository, never()).findByMemberId(any());
			verify(walletRepository, never()).save(any());
		}
	}
}
