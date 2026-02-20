package app.giftify.wallet.application;

import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.vo.Money;
import app.giftify.wallet.application.inbound.*;
import app.giftify.wallet.application.outbound.WalletHistoryRepository;
import app.giftify.wallet.application.outbound.WalletRepository;
import app.giftify.wallet.domain.*;
import app.giftify.wallet.domain.event.WalletChargedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WalletService 테스트")
class WalletServiceTest {

	@Mock
	private WalletRepository walletRepository;

	@Mock
	private WalletHistoryRepository historyRepository;

	@Mock
	private EventPublisher eventPublisher;

	@InjectMocks
	private WalletService walletService;

	@Nested
	@DisplayName("charge 메서드")
	class ChargeTests {

		@Test
		@DisplayName("정상적으로 충전한다")
		void charge_Success() {
			// given
			Long memberId = 1L;
			Money amount = Money.of(10000);
			String chargeOrderId = "charge-order-123";
			ChargeWalletCommand command = new ChargeWalletCommand(memberId, amount, chargeOrderId);

			Wallet wallet = Wallet.create(memberId, Money.zero());
			Wallet savedWallet = Wallet.restore(new WalletSnapshot(1L, memberId, amount));

			given(historyRepository.existsByReferenceIdAndReferenceType(chargeOrderId, ReferenceType.CHARGE))
				.willReturn(false);
			given(walletRepository.findByMemberId(memberId)).willReturn(Optional.of(wallet));
			given(walletRepository.save(any(Wallet.class))).willReturn(savedWallet);

			// when
			ChargeWalletResult result = walletService.charge(command);

			// then
			assertThat(result.walletId()).isEqualTo(1L);
			assertThat(result.memberId()).isEqualTo(memberId);
			assertThat(result.chargedAmount()).isEqualTo(amount);
			verify(historyRepository).recordTransaction(
				savedWallet.getId(),
				TransactionType.CHARGE,
				amount,
				savedWallet.getBalance(),
				ReferenceType.CHARGE,
				chargeOrderId
			);
			verify(eventPublisher).publish(any(WalletChargedEvent.class));
		}

		@Test
		@DisplayName("지갑이 없으면 새로 생성하고 충전한다")
		void charge_CreatesWalletIfNotExists() {
			// given
			Long memberId = 1L;
			Money amount = Money.of(10000);
			String chargeOrderId = "charge-order-123";
			ChargeWalletCommand command = new ChargeWalletCommand(memberId, amount, chargeOrderId);

			Wallet savedWallet = Wallet.restore(new WalletSnapshot(1L, memberId, amount));

			given(historyRepository.existsByReferenceIdAndReferenceType(chargeOrderId, ReferenceType.CHARGE))
				.willReturn(false);
			given(walletRepository.findByMemberId(memberId)).willReturn(Optional.empty());
			given(walletRepository.save(any(Wallet.class))).willReturn(savedWallet);

			// when
			ChargeWalletResult result = walletService.charge(command);

			// then
			assertThat(result.walletId()).isEqualTo(1L);
			assertThat(result.chargedAmount()).isEqualTo(amount);
		}

		@Test
		@DisplayName("중복 거래이면 예외가 발생한다")
		void charge_DuplicateTransaction_ThrowsException() {
			// given
			Long memberId = 1L;
			Money amount = Money.of(10000);
			String chargeOrderId = "charge-order-123";
			ChargeWalletCommand command = new ChargeWalletCommand(memberId, amount, chargeOrderId);

			given(historyRepository.existsByReferenceIdAndReferenceType(chargeOrderId, ReferenceType.CHARGE))
				.willReturn(true);

			// when & then
			assertThatThrownBy(() -> walletService.charge(command))
				.isInstanceOf(WalletException.class)
				.extracting("errorCode")
				.isEqualTo(WalletErrorCode.DUPLICATED_TRANSACTION);

			verify(walletRepository, never()).save(any());
		}
	}

	@Nested
	@DisplayName("withdraw 메서드")
	class WithdrawTests {

		@Test
		@DisplayName("정상적으로 출금한다")
		void withdraw_Success() {
			// given
			Long memberId = 1L;
			Money amount = Money.of(5000);
			WithdrawWalletCommand command = new WithdrawWalletCommand(
				memberId, amount, "088", "1234567890"
			);

			Wallet wallet = Wallet.restore(new WalletSnapshot(1L, memberId, Money.of(10000)));
			Wallet savedWallet = Wallet.restore(new WalletSnapshot(1L, memberId, Money.of(5000)));

			given(walletRepository.findByMemberId(memberId)).willReturn(Optional.of(wallet));
			given(walletRepository.save(any(Wallet.class))).willReturn(savedWallet);

			// when
			WithdrawWalletResult result = walletService.withdraw(command);

			// then
			assertThat(result.walletId()).isEqualTo(1L);
			assertThat(result.withdrawnAmount()).isEqualTo(amount);
			assertThat(result.balanceAfter()).isEqualTo(Money.of(5000));
			assertThat(result.status()).isEqualTo(WithdrawStatus.PENDING);
			verify(historyRepository).recordTransaction(
				eq(savedWallet.getId()),
				eq(TransactionType.WITHDRAW),
				eq(amount),
				eq(savedWallet.getBalance()),
				eq(ReferenceType.WITHDRAWAL),
				any(String.class)
			);
		}

		@Test
		@DisplayName("지갑이 없으면 예외가 발생한다")
		void withdraw_WalletNotFound_ThrowsException() {
			// given
			Long memberId = 1L;
			Money amount = Money.of(5000);
			WithdrawWalletCommand command = new WithdrawWalletCommand(
				memberId, amount, "088", "1234567890"
			);

			given(walletRepository.findByMemberId(memberId)).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> walletService.withdraw(command))
				.isInstanceOf(WalletException.class)
				.extracting("errorCode")
				.isEqualTo(WalletErrorCode.WALLET_NOT_FOUND);
		}

		@Test
		@DisplayName("잔액 부족하면 예외가 발생한다")
		void withdraw_InsufficientBalance_ThrowsException() {
			// given
			Long memberId = 1L;
			Money amount = Money.of(15000);
			WithdrawWalletCommand command = new WithdrawWalletCommand(
				memberId, amount, "088", "1234567890"
			);

			Wallet wallet = Wallet.restore(new WalletSnapshot(1L, memberId, Money.of(10000)));
			given(walletRepository.findByMemberId(memberId)).willReturn(Optional.of(wallet));

			// when & then
			assertThatThrownBy(() -> walletService.withdraw(command))
				.isInstanceOf(WalletException.class)
				.extracting("errorCode")
				.isEqualTo(WalletErrorCode.INSUFFICIENT_BALANCE);
		}
	}

	@Nested
	@DisplayName("getBalance 메서드")
	class GetBalanceTests {

		@Test
		@DisplayName("지갑 잔액을 조회한다")
		void getBalance_Success() {
			// given
			Long memberId = 1L;
			Wallet wallet = Wallet.restore(new WalletSnapshot(1L, memberId, Money.of(10000)));
			given(walletRepository.findByMemberId(memberId)).willReturn(Optional.of(wallet));

			// when
			WalletBalanceResult result = walletService.getBalance(memberId);

			// then
			assertThat(result.walletId()).isEqualTo(1L);
			assertThat(result.memberId()).isEqualTo(memberId);
			assertThat(result.balance()).isEqualTo(Money.of(10000));
		}

		@Test
		@DisplayName("지갑이 없으면 잔액 0원으로 반환한다")
		void getBalance_WalletNotFound_ReturnsZeroBalance() {
			// given
			Long memberId = 1L;
			given(walletRepository.findByMemberId(memberId)).willReturn(Optional.empty());

			// when
			WalletBalanceResult result = walletService.getBalance(memberId);

			// then
			assertThat(result.memberId()).isEqualTo(memberId);
			assertThat(result.balance()).isEqualTo(Money.zero());
		}
	}

	@Nested
	@DisplayName("getHistory 메서드")
	class GetHistoryTests {

		@Test
		@DisplayName("거래 내역을 조회한다")
		void getHistory_Success() {
			// given
			Long memberId = 1L;
			WalletHistoryQuery query = new WalletHistoryQuery(memberId, null, 0, 20);

			Wallet wallet = Wallet.restore(new WalletSnapshot(1L, memberId, Money.of(10000)));
			WalletHistory history = WalletHistory.create(
				1L, TransactionType.CHARGE, Money.of(10000), Money.of(10000),
				ReferenceType.CHARGE, "ref-123", LocalDateTime.now()
			);
			Page<WalletHistory> historyPage = new PageImpl<>(List.of(history));

			given(walletRepository.findByMemberId(memberId)).willReturn(Optional.of(wallet));
			given(historyRepository.findByWalletId(eq(1L), isNull(), any(PageRequest.class)))
				.willReturn(historyPage);

			// when
			Page<WalletHistoryResult> result = walletService.getHistory(query);

			// then
			assertThat(result.getContent()).hasSize(1);
			assertThat(result.getContent().get(0).type()).isEqualTo(TransactionType.CHARGE);
		}

		@Test
		@DisplayName("지갑이 없으면 빈 결과를 반환한다")
		void getHistory_WalletNotFound_ReturnsEmpty() {
			// given
			Long memberId = 1L;
			WalletHistoryQuery query = new WalletHistoryQuery(memberId, null, 0, 20);

			given(walletRepository.findByMemberId(memberId)).willReturn(Optional.empty());

			// when
			Page<WalletHistoryResult> result = walletService.getHistory(query);

			// then
			assertThat(result.getContent()).isEmpty();
		}
	}

	@Nested
	@DisplayName("createIfNotExists 메서드")
	class CreateIfNotExistsTests {

		@Test
		@DisplayName("지갑이 없으면 새로 생성한다")
		void createIfNotExists_CreatesNewWallet() {
			// given
			Long memberId = 1L;
			Wallet savedWallet = Wallet.restore(new WalletSnapshot(1L, memberId, Money.zero()));

			given(walletRepository.findByMemberId(memberId)).willReturn(Optional.empty());
			given(walletRepository.save(any(Wallet.class))).willReturn(savedWallet);

			// when
			CreateWalletUseCase.CreateWalletResult result = walletService.createIfNotExists(memberId);

			// then
			assertThat(result.walletId()).isEqualTo(1L);
			assertThat(result.created()).isTrue();
			verify(walletRepository).save(any(Wallet.class));
		}

		@Test
		@DisplayName("지갑이 있으면 기존 지갑 정보를 반환한다")
		void createIfNotExists_ReturnsExistingWallet() {
			// given
			Long memberId = 1L;
			Wallet existingWallet = Wallet.restore(new WalletSnapshot(1L, memberId, Money.of(10000)));

			given(walletRepository.findByMemberId(memberId)).willReturn(Optional.of(existingWallet));

			// when
			CreateWalletUseCase.CreateWalletResult result = walletService.createIfNotExists(memberId);

			// then
			assertThat(result.walletId()).isEqualTo(1L);
			assertThat(result.created()).isFalse();
			verify(walletRepository, never()).save(any(Wallet.class));
		}
	}
}
