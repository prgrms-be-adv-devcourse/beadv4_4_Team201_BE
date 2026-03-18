package app.giftify.order.adapter.inbound.event;

import app.giftify.order.application.FundingOrderService;
import app.giftify.order.application.OrderService;
import app.giftify.order.application.inbound.command.CancelFundingOrderCommand;
import app.giftify.order.application.inbound.command.ConfirmFundingOrderCommand;
import app.giftify.shared.api.exception.BusinessException;
import app.giftify.shared.api.exception.InfraException;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.funding.FundingAcceptedEvent;
import app.giftify.shared.domain.event.funding.FundingConfirmPendingEvent;
import app.giftify.shared.domain.event.funding.FundingCanceledEvent;
import app.giftify.shared.domain.event.funding.FundingExpiredEvent;
import app.giftify.shared.domain.event.order.OrderConfirmFailedEvent;
import app.giftify.shared.domain.event.order.OrderConfirmedEvent;
import app.giftify.shared.domain.event.payment.PaymentCancelFailedEvent;
import app.giftify.shared.domain.event.payment.PaymentCanceledEvent;
import app.giftify.shared.domain.event.payment.PaymentEvent;
import app.giftify.shared.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final OrderService orderService;
    private final FundingOrderService fundingOrderService;
	private final EventPublisher eventPublisher;

	@Retryable(
			retryFor = InfraException.class,
			exceptionExpression = "#root.errorCode.isRetryable",
			backoff = @Backoff(delay = 100, multiplier = 2.0, random = true)
	)
	@ApplicationModuleListener
	public void on(PaymentCanceledEvent event) {
		log.info("[이벤트 수신] 결제 취소 완료 -> 주문 상태 변경 시작. OrderId: {}", event.data().orderId());
		orderService.completeCancel(event.data().orderId());
	}

	@Retryable(
			retryFor = InfraException.class,
			exceptionExpression = "#root.errorCode.isRetryable",
			backoff = @Backoff(delay = 100, multiplier = 2.0, random = true)
	)

	@ApplicationModuleListener
	public void on(PaymentCancelFailedEvent event) {
		log.info("[이벤트 수신] 결제 취소 실패 -> 주문 실패 반영 시작. OrderId: {}", event.data().orderId());
		orderService.failCancel(event.data().orderId());
	}

    @Retryable(
            retryFor = InfraException.class,
			exceptionExpression = "#root.errorCode.isRetryable",
            backoff = @Backoff(delay = 100, multiplier = 2.0, random = true)
    )
    @ApplicationModuleListener
    public void on (FundingExpiredEvent event) {
        log.info("[이벤트 수신] 펀딩 만료 -> 주문 취소 반영 시작. FundingId: {}", event.getFundingId());
        fundingOrderService.requestCancelFundingOrder(
                new CancelFundingOrderCommand(
                        event.getFundingId(),
                        Money.of(event.getExpiredAmount()
                        )
                ));
    }

    @Retryable(
            retryFor = InfraException.class,
			exceptionExpression = "#root.errorCode.isRetryable",
            backoff = @Backoff(delay = 100, multiplier = 2.0, random = true)
    )
    @ApplicationModuleListener
    public void on(FundingCanceledEvent event) {
        log.info("[이벤트 수신] 펀딩 취소 -> 주문 취소 반영 시작. FundingId: {}", event.getFundingId());
        fundingOrderService.requestCancelFundingOrder(
                new CancelFundingOrderCommand(
                        event.getFundingId(),
                        Money.of(event.getCanceledAmount())
                ));
    }

	@ApplicationModuleListener
	public void on(FundingConfirmPendingEvent event) {
		log.info("[이벤트 수신] 펀딩 수령 확정 대기 -> 주문 확정 시작. FundingId: {}", event.getFundingId());

		ConfirmFundingOrderCommand command = new ConfirmFundingOrderCommand(event.getFundingId(), event.getProductId());

		try {
			fundingOrderService.confirmOrderItemsByFunding(command);

			eventPublisher.publish(new OrderConfirmedEvent(event.getFundingId()));
		} catch (BusinessException | InfraException e) {
			log.error("[주문 확정 실패] 정의된 예외 발생. ErrorCode: {}, FundingId: {}, productId: {}", e.getErrorCode().getCode(), event.getFundingId(), event.getProductId(), e);

			eventPublisher.publish(new OrderConfirmFailedEvent(event.getFundingId(), e.getMessage()));
			throw e;
		} catch (Exception e) {
			log.error("[주문 확정 실패] 알 수 없는 예외 발생. FundingId: {}, productId: {}", event.getFundingId(), event.getProductId(), e);

			eventPublisher.publish(new OrderConfirmFailedEvent(event.getFundingId(), "예기치 못한 오류 발생"));
			throw e;
		}
	}

	/**
	 * 모든 재시도 실패 시 실행되는 공통 복구 로직
	 */
	@Recover
	public void recover(InfraException e, PaymentCanceledEvent event) {
		log(e, event);
		throw e;
	}

	@Recover
	public void recover(InfraException e, PaymentCancelFailedEvent event) {
		log(e, event);
		throw e;
	}

	@Recover
	public void recover(InfraException e, FundingAcceptedEvent event) {
		log.error("================================================================");
		log.error("[최종 장애] 주문 취소 처리 최종 실패 (시스템 자동 복구 불가)");
		log.error("Funding ID: {}", event.getFundingId());
		log.error("Event ID: {}", event.getEventId());
		log.error("Event Type: {}", event.getClass().getSimpleName());
		log.error("Reason: {}", e.getMessage());
		log.error("조치 사항: DB 상태 확인 후 수동 정정 필요");
		log.error("================================================================");
		throw e;
	}

	private static void log(InfraException e, PaymentEvent event) {
		Long orderId = event.data().orderId();

		log.error("================================================================");
		log.error("[최종 장애] 주문 취소 처리 최종 실패 (시스템 자동 복구 불가)");
		log.error("Order ID: {}", orderId);
		log.error("Event ID: {}", event.id());
		log.error("Event Type: {}", event.getClass().getSimpleName());
		log.error("Reason: {}", e.getMessage());
		log.error("조치 사항: DB 상태 확인 후 수동 정정 필요");
		log.error("================================================================");
	}
}
