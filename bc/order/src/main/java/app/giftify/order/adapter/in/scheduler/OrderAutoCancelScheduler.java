package app.giftify.order.adapter.in.scheduler;

import app.giftify.order.application.port.in.OrderUseCase;
import app.giftify.order.application.port.out.OrderRepositoryPort;
import app.giftify.order.domain.domain.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

// 결제 대기 중인 주문을 자동으로 취소하는 스케줄러
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderAutoCancelScheduler {

    private final OrderRepositoryPort orderRepositoryPort;
    private final OrderUseCase orderUseCase;
    
    // 자동 취소 기준 시간 (30분)
    private static final int AUTO_CANCEL_MINUTES = 30;

    // 1분마다 실행하여 결제 대기 상태로 30분이 지난 주문을 찾아 취소 처리
    @Scheduled(fixedDelay = 60000)
    public void autoCancelPendingOrders() {
        log.info("결제 대기 중인 주문 자동 취소 스케줄러 실행");
        List<Order> expiredOrders = orderRepositoryPort.findPaymentPendingOrdersOlderThan(AUTO_CANCEL_MINUTES);

        for (Order order : expiredOrders) {
            try {
                orderUseCase.cancelOrder(new OrderUseCase.CancelOrderCommand(order.getId()));
                log.info("주문 자동 취소 완료: {}", order.getOrderNumber());
            } catch (Exception e) {
                log.error("주문 자동 취소 실패: {}", order.getOrderNumber(), e);
            }
        }
    }
}
