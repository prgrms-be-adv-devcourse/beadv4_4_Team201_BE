package app.giftify.wallet.adapter.inbound.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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

import static org.assertj.core.api.Assertions.assertThat;

import app.giftify.shared.domain.event.payment.PaymentEventData;
import app.giftify.shared.domain.event.payment.PaymentSucceededEvent;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;
import app.giftify.wallet.application.inbound.ChargeWalletCommand;
import app.giftify.wallet.application.inbound.ChargeWalletResult;
import app.giftify.wallet.application.inbound.ChargeWalletUseCase;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentSucceededEventHandler 테스트")
class PaymentSucceededEventHandlerTest {

	@Mock
	private ChargeWalletUseCase chargeWalletUseCase;

	@InjectMocks
	private PaymentSucceededEventHandler eventHandler;

	@Nested
	@DisplayName("handle 메서드")
	class HandleTests {

		@Test
		@DisplayName("DEPOSIT_CHARGE 타입 결제이면 예치금을 충전한다")
		void handle_DepositChargePayment_ChargesWallet() {
			PaymentEventData data = PaymentEventData.forSuccess(
				1L, 100L, 10L, "order-123", Money.of(10000),
				PaymentMethod.CARD, PaymentType.DEPOSIT_CHARGE, "pk_test", "txn_test"
			);
			PaymentSucceededEvent event = PaymentSucceededEvent.create(data);

			ChargeWalletResult chargeResult = new ChargeWalletResult(1L, 10L, Money.of(10000), Money.of(10000), "order-123");
			given(chargeWalletUseCase.charge(any(ChargeWalletCommand.class))).willReturn(chargeResult);

			eventHandler.handle(event);

			ArgumentCaptor<ChargeWalletCommand> commandCaptor = ArgumentCaptor.forClass(ChargeWalletCommand.class);
			verify(chargeWalletUseCase).charge(commandCaptor.capture());

			ChargeWalletCommand capturedCommand = commandCaptor.getValue();
			assertThat(capturedCommand.memberId()).isEqualTo(10L);
			assertThat(capturedCommand.amount()).isEqualTo(Money.of(10000));
			assertThat(capturedCommand.chargeOrderId()).isEqualTo("order-123");
		}

		@Test
		@DisplayName("FUNDING 타입 결제이면 무시한다")
		void handle_FundingPayment_Ignores() {
			PaymentEventData data = PaymentEventData.forSuccess(
				1L, 100L, 10L, "order-123", Money.of(10000),
				PaymentMethod.CARD, PaymentType.FUNDING, "pk_test", "txn_test"
			);
			PaymentSucceededEvent event = PaymentSucceededEvent.create(data);

			eventHandler.handle(event);

			verify(chargeWalletUseCase, never()).charge(any());
		}
	}
}
