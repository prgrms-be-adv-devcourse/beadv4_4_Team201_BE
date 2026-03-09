package app.giftify.payment.adapter.inbound.event;

import static app.giftify.payment.domain.SystemConstants.*;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import app.giftify.payment.application.inbound.CancelPaymentCommand;
import app.giftify.payment.application.inbound.CancelPaymentUseCase;
import app.giftify.shared.domain.event.order.OrderCancelRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancelEventHandler {

	private final CancelPaymentUseCase cancelPaymentUseCase;

	@ApplicationModuleListener
	public void handle(OrderCancelRequestedEvent event) {
		log.info("[OrderCancelEventHandler] 주문 취소 요청 수신. orderId={}, paymentId={}, cancelAmount={}",
			event.getOrderId(), event.getPaymentId(), event.getCancelAmount());

		CancelPaymentCommand command = CancelPaymentCommand.withAmount(
			event.getPaymentId(),
			SYSTEM_REQUESTER_ID,
			"주문 취소",
			event.getCancelAmount()
		);

		cancelPaymentUseCase.cancel(command);

		log.info("[OrderCancelEventHandler] 주문 취소 처리 위임 완료. paymentId={}", event.getPaymentId());
	}
}
