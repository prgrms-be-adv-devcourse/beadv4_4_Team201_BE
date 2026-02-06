package app.giftify.wallet.adapter.inbound.event;

import app.giftify.payment.domain.event.PaymentConfirmedEvent;
import app.giftify.shared.domain.vo.Money;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.wallet.application.inbound.ChargeWalletCommand;
import app.giftify.wallet.application.inbound.ChargeWalletResult;
import app.giftify.wallet.application.inbound.ChargeWalletUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentConfirmedEventHandler 테스트")
class PaymentConfirmedEventHandlerTest {

	@Mock
	private ChargeWalletUseCase chargeWalletUseCase;

	@InjectMocks
	private PaymentConfirmedEventHandler eventHandler;

	@Nested
	@DisplayName("handle 메서드")
	class HandleTests {

		@Test
		@DisplayName("DEPOSIT_CHARGE(예치금 충전) 타입 결제이면 예치금을 충전한다")
		void handle_PointChargePayment_ChargesWallet() {
			// given
			Long paymentId = 1L;
			Long memberId = 100L;
			String orderId = "order-123";
			Money amount = Money.of(10000);
			LocalDateTime paidAt = LocalDateTime.now();

			PaymentConfirmedEvent event = new PaymentConfirmedEvent(
				paymentId, memberId, orderId, PaymentType.DEPOSIT_CHARGE, amount, paidAt
			);

			ChargeWalletResult chargeResult = new ChargeWalletResult(1L, memberId, amount, amount, orderId);
			given(chargeWalletUseCase.charge(any(ChargeWalletCommand.class))).willReturn(chargeResult);

			// when
			eventHandler.handle(event);

			// then
			ArgumentCaptor<ChargeWalletCommand> commandCaptor = ArgumentCaptor.forClass(ChargeWalletCommand.class);
			verify(chargeWalletUseCase).charge(commandCaptor.capture());

			ChargeWalletCommand capturedCommand = commandCaptor.getValue();
			assertThat(capturedCommand.memberId()).isEqualTo(memberId);
			assertThat(capturedCommand.amount()).isEqualTo(amount);
			assertThat(capturedCommand.chargeOrderId()).isEqualTo(orderId);
		}

		@Test
		@DisplayName("FUNDING 타입 결제이면 무시한다")
		void handle_FundingPayment_Ignores() {
			// given
			PaymentConfirmedEvent event = new PaymentConfirmedEvent(
				1L, 100L, "order-123", PaymentType.FUNDING, Money.of(10000), LocalDateTime.now()
			);

			// when
			eventHandler.handle(event);

			// then
			verify(chargeWalletUseCase, never()).charge(any());
		}


	}
}
