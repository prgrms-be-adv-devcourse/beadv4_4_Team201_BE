package app.giftify.wallet.application;

import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.vo.Money;
import app.giftify.wallet.application.inbound.DeductWalletCommand;
import app.giftify.wallet.application.inbound.DeductWalletResult;
import app.giftify.wallet.application.outbound.WalletHistoryRepository;
import app.giftify.wallet.application.outbound.WalletRepository;
import app.giftify.wallet.domain.*;
import app.giftify.wallet.domain.event.WalletDeductedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("WalletPaymentService 테스트")
class WalletPaymentServiceTest {

	@Mock
	private WalletRepository walletRepository;

	@Mock
	private WalletHistoryRepository historyRepository;

	@Mock
	private EventPublisher eventPublisher;

	@InjectMocks
	private WalletPaymentService walletPaymentService;

	@Nested
	@DisplayName("deductForPayment 메서드")
	class DeductForPaymentTests {

		@Test
		@DisplayName("정상적으로 결제 차감한다")
		void deductForPayment_Success() {
			// given
			Long memberId = 1L;
			Long paymentId = 100L;
			String orderId = "order-123";
			Money amount = Money.of(5000);
			DeductWalletCommand command = new DeductWalletCommand(memberId, paymentId, orderId, amount);

			Wallet wallet = Wallet.restore(new WalletSnapshot(1L, memberId, Money.of(10000)));
			Wallet savedWallet = Wallet.restore(new WalletSnapshot(1L, memberId, Money.of(5000)));

			given(historyRepository.existsByReferenceIdAndReferenceType(orderId, ReferenceType.PAYMENT))
				.willReturn(false);
			given(walletRepository.findByMemberId(memberId)).willReturn(Optional.of(wallet));
			given(walletRepository.save(any(Wallet.class))).willReturn(savedWallet);

			// when
			DeductWalletResult result = walletPaymentService.deductForPayment(command);

			// then
			assertThat(result.success()).isTrue();
			assertThat(result.walletId()).isEqualTo(1L);
			assertThat(result.balanceAfter()).isEqualTo(Money.of(5000));
			verify(historyRepository).record(any(WalletHistory.class));
			verify(eventPublisher).publish(any(WalletDeductedEvent.class));
		}

		@Test
		@DisplayName("중복 거래이면 예외가 발생한다")
		void deductForPayment_DuplicateTransaction_ThrowsException() {
			// given
			Long memberId = 1L;
			Long paymentId = 100L;
			String orderId = "order-123";
			Money amount = Money.of(5000);
			DeductWalletCommand command = new DeductWalletCommand(memberId, paymentId, orderId, amount);

			given(historyRepository.existsByReferenceIdAndReferenceType(orderId, ReferenceType.PAYMENT))
				.willReturn(true);

			// when & then
			assertThatThrownBy(() -> walletPaymentService.deductForPayment(command))
				.isInstanceOf(WalletException.class)
				.extracting("errorCode")
				.isEqualTo(WalletErrorCode.DUPLICATED_TRANSACTION);

			verify(walletRepository, never()).save(any());
		}

		@Test
		@DisplayName("지갑이 없으면 예외가 발생한다")
		void deductForPayment_WalletNotFound_ThrowsException() {
			// given
			Long memberId = 1L;
			Long paymentId = 100L;
			String orderId = "order-123";
			Money amount = Money.of(5000);
			DeductWalletCommand command = new DeductWalletCommand(memberId, paymentId, orderId, amount);

			given(historyRepository.existsByReferenceIdAndReferenceType(orderId, ReferenceType.PAYMENT))
				.willReturn(false);
			given(walletRepository.findByMemberId(memberId)).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> walletPaymentService.deductForPayment(command))
				.isInstanceOf(WalletException.class)
				.extracting("errorCode")
				.isEqualTo(WalletErrorCode.WALLET_NOT_FOUND);
		}

		@Test
		@DisplayName("잔액이 부족하면 실패 결과를 반환한다")
		void deductForPayment_InsufficientBalance_ReturnsFailure() {
			// given
			Long memberId = 1L;
			Long paymentId = 100L;
			String orderId = "order-123";
			Money amount = Money.of(15000);
			DeductWalletCommand command = new DeductWalletCommand(memberId, paymentId, orderId, amount);

			Wallet wallet = Wallet.restore(new WalletSnapshot(1L, memberId, Money.of(10000)));

			given(historyRepository.existsByReferenceIdAndReferenceType(orderId, ReferenceType.PAYMENT))
				.willReturn(false);
			given(walletRepository.findByMemberId(memberId)).willReturn(Optional.of(wallet));

			// when
			DeductWalletResult result = walletPaymentService.deductForPayment(command);

			// then
			assertThat(result.success()).isFalse();
			assertThat(result.walletId()).isEqualTo(1L);
			assertThat(result.currentBalance()).isEqualTo(Money.of(10000));
			assertThat(result.requiredAmount()).isEqualTo(amount);
			verify(walletRepository, never()).save(any());
			verify(eventPublisher, never()).publish(any());
		}
	}
}
