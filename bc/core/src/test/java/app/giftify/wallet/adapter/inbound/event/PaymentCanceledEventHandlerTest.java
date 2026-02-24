package app.giftify.wallet.adapter.inbound.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.shared.domain.event.payment.PaymentCanceledEvent;
import app.giftify.shared.domain.event.payment.PaymentEventData;
import app.giftify.shared.domain.type.CancelType;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;
import app.giftify.wallet.application.inbound.RestoreWalletCommand;
import app.giftify.wallet.application.inbound.RestoreWalletUseCase;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentCanceledEventHandler 테스트")
class PaymentCanceledEventHandlerTest {

	@Mock
	RestoreWalletUseCase restoreWalletUseCase;

	@InjectMocks
	PaymentCanceledEventHandler handler;

	@Test
	@DisplayName("Wallet 결제(DEPOSIT) 취소 시 Wallet 복원 호출")
	void handleWalletPaymentCancel() {
		PaymentEventData data = PaymentEventData.forCancel(
			1L,
			100L,
			200L,
			"ORD-001",
			Money.of(5000),
			PaymentMethod.DEPOSIT,
			PaymentType.FUNDING,
			CancelType.CANCEL,
			"고객 요청",
			"txKey-123"
		);
		PaymentCanceledEvent event = PaymentCanceledEvent.create(data);

		handler.handle(event);

		verify(restoreWalletUseCase).restore(new RestoreWalletCommand(
			200L,
			Money.of(5000),
			"txKey-123"
		));
	}

	@Test
	@DisplayName("비-Wallet 결제(CARD) 취소 시 Wallet 복원 호출 안 함")
	void skipNonWalletPaymentCancel() {
		PaymentEventData data = PaymentEventData.forCancel(
			2L,
			101L,
			201L,
			"ORD-002",
			Money.of(10000),
			PaymentMethod.CARD,
			PaymentType.FUNDING,
			CancelType.CANCEL,
			"고객 요청",
			"txKey-456"
		);
		PaymentCanceledEvent event = PaymentCanceledEvent.create(data);

		handler.handle(event);

		verify(restoreWalletUseCase, never()).restore(any());
	}
}
