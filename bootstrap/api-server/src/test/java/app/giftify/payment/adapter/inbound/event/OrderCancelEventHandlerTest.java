package app.giftify.payment.adapter.inbound.event;

import static app.giftify.payment.domain.SystemConstants.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.payment.application.inbound.CancelPaymentCommand;
import app.giftify.payment.application.inbound.CancelPaymentUseCase;
import app.giftify.order.domain.event.OrderCancelRequestedEvent;
import app.giftify.support.common.money.Money;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderCancelEventHandler 테스트")
class OrderCancelEventHandlerTest {

	@Mock
	CancelPaymentUseCase cancelPaymentUseCase;

	@InjectMocks
	OrderCancelEventHandler handler;

	@Test
	@DisplayName("주문 취소 이벤트를 수신하면 CancelPaymentUseCase에 partial 커맨드로 위임한다")
	void handle_DelegatesToUseCase() {
		// given
		OrderCancelRequestedEvent event = new OrderCancelRequestedEvent(
			100L, "ORD-001", 1L, "txn-001", Money.of(3000)
		);

		// when
		handler.handle(event);

		// then
		ArgumentCaptor<CancelPaymentCommand> captor = ArgumentCaptor.forClass(CancelPaymentCommand.class);
		verify(cancelPaymentUseCase).cancel(captor.capture());

		CancelPaymentCommand command = captor.getValue();
		assertThat(command.paymentId()).isEqualTo(1L);
		assertThat(command.requesterId()).isEqualTo(SYSTEM_REQUESTER_ID);
		assertThat(command.reason()).isEqualTo("주문 취소");
		assertThat(command.cancelAmount()).isEqualTo(Money.of(3000));
	}
}
