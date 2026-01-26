package app.giftify.funding.adapter.outbound.notification;

import app.giftify.funding.application.outbound.OrderNotificationPort;
import app.giftify.funding.domain.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConsoleOrderNotificationAdapter implements OrderNotificationPort {

    @Override
    public void notifyOrderTimeout(Order order) {
        log.info("[NOTIFICATION] 주문이 10분 동안 결제되지 않아 취소되었습니다. (주문번호: {}, 사용자ID: {})",
                order.getOrderNumber(), order.getBuyerId());
    }
}
