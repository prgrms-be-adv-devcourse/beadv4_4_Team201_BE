package app.giftify.payment.adapter.inbound.consumer;

import org.springframework.stereotype.Component;

import app.giftify.payment.application.inbound.RefundPaymentUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Order BC의 주문 취소 이벤트 소비자.
 *
 * TODO: Order BC 연동 시 구현 완료 필요
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCanceledEventConsumer {

	private final RefundPaymentUseCase refundPaymentUseCase;

	// TODO: OrderCanceledEvent 정의 후 활성화
	// @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	// @Transactional(propagation = Propagation.REQUIRES_NEW)
	// public void consume(OrderCanceledEvent event) {
	//     log.info("[Payment] 주문 취소 이벤트 수신. orderId={}", event.getOrderId());
	//     refundPaymentUseCase.refundByOrderId(event.getOrderId(), event.getReason());
	// }
}
