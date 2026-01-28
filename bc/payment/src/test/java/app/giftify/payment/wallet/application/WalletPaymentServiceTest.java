package app.giftify.payment.wallet.application;

import app.giftify.wallet.application.WalletPaymentService;
import app.giftify.wallet.application.inbound.DeductWalletCommand;
import app.giftify.wallet.application.inbound.DeductWalletResult;
import app.giftify.wallet.application.outbound.WalletHistoryRepository;
import app.giftify.wallet.application.outbound.WalletRepository;
import app.giftify.wallet.domain.ReferenceType;
import app.giftify.wallet.domain.TransactionType;
import app.giftify.wallet.domain.Wallet;
import app.giftify.wallet.domain.WalletErrorCode;
import app.giftify.wallet.domain.WalletException;
import app.giftify.wallet.domain.WalletHistory;
import app.giftify.wallet.domain.WalletSnapshot;
import app.giftify.wallet.domain.event.WalletDeductedEvent;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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
	@DisplayName("Given_Sufficient_Balance")
	class Given_Sufficient_Balance {

		@Nested
		@DisplayName("When_DeductForPayment_Called")
		class When_DeductForPayment_Called {

			@Test
			@DisplayName("Then_Deduct_Success_And_Publish_WalletDeductedEvent")
			void Then_Deduct_Success_And_Publish_WalletDeductedEvent() {
				// given
				Long memberId = 1L;
				Long paymentId = 100L;
				String orderId = "order-123";
				Money amount = Money.of(5000);

				Wallet wallet = Wallet.restore(new WalletSnapshot(1L, memberId, Money.of(10000)));
				when(walletRepository.findByMemberId(memberId)).thenReturn(Optional.of(wallet));
				when(historyRepository.existsByReferenceIdAndReferenceType(orderId, ReferenceType.PAYMENT))
					.thenReturn(false);
				when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

				DeductWalletCommand command = new DeductWalletCommand(memberId, paymentId, orderId, amount);

				// when
				DeductWalletResult result = walletPaymentService.deductForPayment(command);

				// then
				assertThat(result.success()).isTrue();
				assertThat(result.balanceAfter()).isEqualTo(Money.of(5000));

				ArgumentCaptor<WalletHistory> historyCaptor = ArgumentCaptor.forClass(WalletHistory.class);
				verify(historyRepository).record(historyCaptor.capture());
				WalletHistory capturedHistory = historyCaptor.getValue();
				assertThat(capturedHistory.getTransactionType()).isEqualTo(TransactionType.PAYMENT);
				assertThat(capturedHistory.getAmount()).isEqualTo(amount);
				assertThat(capturedHistory.getReferenceType()).isEqualTo(ReferenceType.PAYMENT);
				assertThat(capturedHistory.getReferenceId()).isEqualTo(orderId);

				ArgumentCaptor<WalletDeductedEvent> eventCaptor = ArgumentCaptor.forClass(WalletDeductedEvent.class);
				verify(eventPublisher).publish(eventCaptor.capture());
				WalletDeductedEvent capturedEvent = eventCaptor.getValue();
				assertThat(capturedEvent.getMemberId()).isEqualTo(memberId);
				assertThat(capturedEvent.getPaymentId()).isEqualTo(paymentId);
				assertThat(capturedEvent.getOrderId()).isEqualTo(orderId);
				assertThat(capturedEvent.getAmount()).isEqualTo(amount);
			}
		}
	}

	@Nested
	@DisplayName("Given_Insufficient_Balance")
	class Given_Insufficient_Balance {

		@Nested
		@DisplayName("When_DeductForPayment_Called")
		class When_DeductForPayment_Called {

			@Test
			@DisplayName("Then_Returns_InsufficientBalance_Result")
			void Then_Returns_InsufficientBalance_Result() {
				// given
				Long memberId = 1L;
				Long paymentId = 100L;
				String orderId = "order-123";
				Money currentBalance = Money.of(5000);
				Money requiredAmount = Money.of(10000);

				Wallet wallet = Wallet.restore(new WalletSnapshot(1L, memberId, currentBalance));
				when(walletRepository.findByMemberId(memberId)).thenReturn(Optional.of(wallet));
				when(historyRepository.existsByReferenceIdAndReferenceType(orderId, ReferenceType.PAYMENT))
					.thenReturn(false);

				DeductWalletCommand command = new DeductWalletCommand(memberId, paymentId, orderId, requiredAmount);

				// when
				DeductWalletResult result = walletPaymentService.deductForPayment(command);

				// then
				assertThat(result.success()).isFalse();
				assertThat(result.errorCode()).isEqualTo("INSUFFICIENT_BALANCE");
				assertThat(result.requiredAmount()).isEqualTo(requiredAmount);
				assertThat(result.currentBalance()).isEqualTo(currentBalance);

				verify(walletRepository, never()).save(any());
				verify(historyRepository, never()).record(any());
				verify(eventPublisher, never()).publish(any());
			}
		}
	}

	@Nested
	@DisplayName("Given_Duplicate_Transaction")
	class Given_Duplicate_Transaction {

		@Nested
		@DisplayName("When_DeductForPayment_Called")
		class When_DeductForPayment_Called {

			@Test
			@DisplayName("Then_Throw_WalletException_With_DUPLICATED_TRANSACTION")
			void Then_Throw_WalletException_With_DUPLICATED_TRANSACTION() {
				// given
				Long memberId = 1L;
				Long paymentId = 100L;
				String orderId = "order-123";
				Money amount = Money.of(5000);

				when(historyRepository.existsByReferenceIdAndReferenceType(orderId, ReferenceType.PAYMENT))
					.thenReturn(true);

				DeductWalletCommand command = new DeductWalletCommand(memberId, paymentId, orderId, amount);

				// when & then
				assertThatThrownBy(() -> walletPaymentService.deductForPayment(command))
					.isInstanceOf(WalletException.class)
					.extracting(ex -> ((WalletException) ex).getErrorCode())
					.isEqualTo(WalletErrorCode.DUPLICATED_TRANSACTION);

				verify(walletRepository, never()).findByMemberId(any());
				verify(walletRepository, never()).save(any());
				verify(historyRepository, never()).record(any());
				verify(eventPublisher, never()).publish(any());
			}
		}
	}

	@Nested
	@DisplayName("Given_Wallet_Not_Found")
	class Given_Wallet_Not_Found {

		@Nested
		@DisplayName("When_DeductForPayment_Called")
		class When_DeductForPayment_Called {

			@Test
			@DisplayName("Then_Throw_WalletException_With_WALLET_NOT_FOUND")
			void Then_Throw_WalletException_With_WALLET_NOT_FOUND() {
				// given
				Long memberId = 999L;
				Long paymentId = 100L;
				String orderId = "order-123";
				Money amount = Money.of(5000);

				when(historyRepository.existsByReferenceIdAndReferenceType(orderId, ReferenceType.PAYMENT))
					.thenReturn(false);
				when(walletRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

				DeductWalletCommand command = new DeductWalletCommand(memberId, paymentId, orderId, amount);

				// when & then
				assertThatThrownBy(() -> walletPaymentService.deductForPayment(command))
					.isInstanceOf(WalletException.class)
					.extracting(ex -> ((WalletException) ex).getErrorCode())
					.isEqualTo(WalletErrorCode.WALLET_NOT_FOUND);

				verify(walletRepository, never()).save(any());
				verify(historyRepository, never()).record(any());
				verify(eventPublisher, never()).publish(any());
			}
		}
	}
}
