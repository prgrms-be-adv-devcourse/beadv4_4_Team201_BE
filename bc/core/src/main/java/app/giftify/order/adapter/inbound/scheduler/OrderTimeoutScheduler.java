package app.giftify.order.adapter.inbound.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import app.giftify.order.application.inbound.OrderTimeoutUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutScheduler {

    private final OrderTimeoutUseCase orderTimeoutUseCase;

    // 1분마다 만료된 주문(결제 대기 상태 10분 이상)을 체크하는 스케쥴러
    @Scheduled(fixedDelay = 60000)
    public void processTimedOutOrders() {
        log.debug("[OrderTimeoutScheduler] 주문 만료(결제 대기 상태 10분) 체크 시작");
        orderTimeoutUseCase.handleTimedOutOrders();
    }
}
