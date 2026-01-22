package app.giftify.payment.adapter.in.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import payment.handler.PaymentRefundHandler;

/**
 * OrderCanceledEvent Consumer.
 * Order BC에서 발행한 주문 취소 이벤트를 수신하여 결제 환불을 처리합니다.
 *
 * <p>TODO: Order BC 연동 시 아래 작업 필요:
 * <ol>
 *   <li>OrderCanceledEvent 클래스 정의 (shared 모듈)</li>
 *   <li>consume() 메서드에 @TransactionalEventListener 추가</li>
 *   <li>이벤트에서 orderUuid 추출하여 handler.handleOrderCanceled() 호출</li>
 * </ol>
 */
@Component
public class OrderCanceledEventConsumer {

	private static final Logger log = LoggerFactory.getLogger(OrderCanceledEventConsumer.class);

	private final PaymentRefundHandler handler;

	public OrderCanceledEventConsumer(PaymentRefundHandler handler) {
		this.handler = handler;
	}

	/**
	 * TODO: Order BC 연동 시 이벤트 리스너 어노테이션 추가
	 *
	 * 예시 (Spring Event):
	 * <pre>
	 * {@code @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)}
	 * {@code @Transactional(propagation = Propagation.REQUIRES_NEW)}
	 * public void consume(OrderCanceledEvent event) {
	 *     handler.handleOrderCanceled(event.getOrderUuid(), event.getReason());
	 * }
	 * </pre>
	 */
	public void consume(/* OrderCanceledEvent event */) {
		// TODO: Order BC 연동 후 구현
		// handler.handleOrderCanceled(event.getOrderUuid(), event.getReason());
		log.warn("[OrderCanceled] Order BC 미연동 상태. Consumer 비활성화.");
	}
}
