package app.giftify.wallet.adapter.inbound.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.support.common.event.EventPublisher;
import app.giftify.settlement.domain.event.SettlementCreatedEvent;
import app.giftify.wallet.domain.event.WalletSettlementCompletedEvent;
import app.giftify.wallet.domain.event.WalletSettlementFailedEvent;
import app.giftify.support.common.money.Money;
import app.giftify.wallet.application.inbound.SettlementPayoutCommand;
import app.giftify.wallet.application.inbound.SettlementPayoutUseCase;
import app.giftify.wallet.domain.WalletErrorCode;
import app.giftify.wallet.domain.WalletException;

@ExtendWith(MockitoExtension.class)
class SettlementCreatedEventListenerTest {

	@Mock
	SettlementPayoutUseCase settlementPayoutUseCase;

	@Mock
	EventPublisher eventPublisher;

	@InjectMocks
	SettlementCreatedEventListener sut;

	@Nested
	@DisplayName("handle - 성공")
	class Success {

		@Test
		@DisplayName("SettlementCreatedEvent 수신 시 payout을 호출한다")
		void callsPayoutUseCase() {
			// given
			SettlementCreatedEvent event = new SettlementCreatedEvent(1L, 100L, Money.of(50000));
			willDoNothing().given(settlementPayoutUseCase).payout(any());

			// when
			sut.handle(event);

			// then
			ArgumentCaptor<SettlementPayoutCommand> captor =
				ArgumentCaptor.forClass(SettlementPayoutCommand.class);
			verify(settlementPayoutUseCase).payout(captor.capture());

			SettlementPayoutCommand cmd = captor.getValue();
			assertThat(cmd.settlementId()).isEqualTo(1L);
			assertThat(cmd.sellerId()).isEqualTo(100L);
			assertThat(cmd.amount()).isEqualTo(Money.of(50000));
			assertThat(cmd.referenceId()).isEqualTo(event.getEventId());
		}

		@Test
		@DisplayName("성공 시 WalletSettlementCompletedEvent를 발행한다")
		void publishesCompletedEvent() {
			// given
			SettlementCreatedEvent event = new SettlementCreatedEvent(1L, 100L, Money.of(50000));
			willDoNothing().given(settlementPayoutUseCase).payout(any());

			// when
			sut.handle(event);

			// then
			ArgumentCaptor<WalletSettlementCompletedEvent> captor =
				ArgumentCaptor.forClass(WalletSettlementCompletedEvent.class);
			verify(eventPublisher).publish(captor.capture());

			WalletSettlementCompletedEvent completedEvent = captor.getValue();
			assertThat(completedEvent.getSettlementId()).isEqualTo(1L);
			assertThat(completedEvent.getSellerId()).isEqualTo(100L);
			assertThat(completedEvent.getTotalAmount()).isEqualTo(Money.of(50000));
		}

		@Test
		@DisplayName("성공 시 WalletPayoutFailedEvent를 발행하지 않는다")
		void doesNotPublishFailedEvent() {
			// given
			SettlementCreatedEvent event = new SettlementCreatedEvent(1L, 100L, Money.of(50000));
			willDoNothing().given(settlementPayoutUseCase).payout(any());

			// when
			sut.handle(event);

			// then
			verify(eventPublisher, never()).publish(any(WalletSettlementFailedEvent.class));
		}
	}

	@Nested
	@DisplayName("handle - 영구 실패 (WalletException)")
	class PermanentFailure {

		@Test
		@DisplayName("WalletException 발생 시 WalletPayoutFailedEvent를 발행한다")
		void publishesFailedEvent() {
			// given
			SettlementCreatedEvent event = new SettlementCreatedEvent(2L, 200L, Money.of(-30000));
			willThrow(new WalletException(WalletErrorCode.WALLET_NOT_FOUND))
				.given(settlementPayoutUseCase).payout(any());

			// when
			sut.handle(event);

			// then
			ArgumentCaptor<WalletSettlementFailedEvent> captor =
				ArgumentCaptor.forClass(WalletSettlementFailedEvent.class);
			verify(eventPublisher).publish(captor.capture());

			WalletSettlementFailedEvent failedEvent = captor.getValue();
			assertThat(failedEvent.getSettlementId()).isEqualTo(2L);
			assertThat(failedEvent.getSellerId()).isEqualTo(200L);
			assertThat(failedEvent.getTotalAmount()).isEqualTo(Money.of(-30000));
			assertThat(failedEvent.getReason()).contains("지갑을 찾을 수 없습니다");
		}

		@Test
		@DisplayName("WalletException 발생 시 예외를 삼켜서 Modulith가 완료 처리하도록 한다")
		void doesNotRethrowWalletException() {
			// given
			SettlementCreatedEvent event = new SettlementCreatedEvent(3L, 300L, Money.of(10000));
			willThrow(new WalletException(WalletErrorCode.INSUFFICIENT_BALANCE))
				.given(settlementPayoutUseCase).payout(any());

			// when & then — 예외가 전파되지 않음
			sut.handle(event);
		}
	}

	@Nested
	@DisplayName("handle - 일시 실패 (인프라 예외)")
	class TransientFailure {

		@Test
		@DisplayName("RuntimeException 발생 시 그대로 전파하여 Modulith가 재시도하도록 한다")
		void rethrowsRuntimeException() {
			// given
			SettlementCreatedEvent event = new SettlementCreatedEvent(4L, 400L, Money.of(20000));
			willThrow(new RuntimeException("DB timeout"))
				.given(settlementPayoutUseCase).payout(any());

			// when & then
			assertThatThrownBy(() -> sut.handle(event))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("DB timeout");

			verify(eventPublisher, never()).publish(any());
		}
	}
}
