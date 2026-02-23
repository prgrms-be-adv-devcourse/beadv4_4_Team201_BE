package app.giftify.orderDemo.adapter.inbound.event;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import app.giftify.orderDemo.application.OrderService;
import app.giftify.shared.api.exception.InfraException;
import app.giftify.shared.domain.event.payment.PaymentCancelFailedEvent;
import app.giftify.shared.domain.event.payment.PaymentCanceledEvent;
import app.giftify.shared.domain.event.payment.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

	private final OrderService orderService;

	@Retryable(
		retryFor = InfraException.class,
		exceptionExpression = "@retryService.isRetryable(#root)",
		backoff = @Backoff(delay = 100, multiplier = 2.0, random = true)
	)
	@ApplicationModuleListener
	public void on(PaymentCanceledEvent event) {
		log.info("[이벤트 수신] 결제 취소 완료 -> 주문 상태 변경 시작. OrderId: {}", event.data().orderId());
		orderService.completeCancel(event.data().orderId());
	}

	@Retryable(
		retryFor = InfraException.class,
		exceptionExpression = "@retryService.isRetryable(#root)",
		backoff = @Backoff(delay = 100, multiplier = 2.0, random = true)
	)

	@ApplicationModuleListener
	public void on(PaymentCancelFailedEvent event) {
		log.info("[이벤트 수신] 결제 취소 실패 -> 주문 실패 반영 시작. OrderId: {}", event.data().orderId());
		orderService.failCancel(event.data().orderId());
	}

	/**
	 * 모든 재시도 실패 시 실행되는 공통 복구 로직
	 */
	@Recover
	public void recover(InfraException e, PaymentEvent event) {
		Long orderId = event.data().orderId();
		log.error("================================================================");
		log.error("[최종 장애] 주문 취소 처리 최종 실패 (시스템 자동 복구 불가)");
		log.error("OrderId: {}", orderId);
		log.error("Event Type: {}", event.getClass().getSimpleName());
		log.error("Reason: {}", e.getMessage());
		log.error("조치 사항: DB 상태 확인 후 수동 정정 필요");
		log.error("================================================================");

		throw e;
	}

}
