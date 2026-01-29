package app.giftify.order.application.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.order.application.inbound.OrderTimeoutUseCase;
import app.giftify.order.application.outbound.OrderItemRepositoryPort;
import app.giftify.order.application.outbound.OrderNotificationPort;
import app.giftify.order.application.outbound.OrderRepositoryPort;
import app.giftify.order.domain.Order;
import app.giftify.order.domain.OrderItem;
import app.giftify.order.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderTimeoutService implements OrderTimeoutUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final OrderItemRepositoryPort orderItemRepositoryPort;
    private final OrderNotificationPort orderNotificationPort;

    @Override
    public void handleTimedOutOrders() {
        // 결제 시간 최대 10분
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(10);

        List<Order> timedOutOrders = orderRepositoryPort.findByStatusAndCreatedAtBefore(OrderStatus.PAYMENT_PENDING, threshold);

        if (timedOutOrders.isEmpty()) {
            return;
        }

        log.info("[OrderTimeout] {}건의 시간 초과의 만료된 주문을 삭제합니다.", timedOutOrders.size());
        for (Order order : timedOutOrders) {
            try {
                processTimeout(order);
            } catch (Exception e) {
                log.error("[OrderTimeout] 삭제 처리 중 오류 발생. orderId={}", order.getId(), e);
            }
        }
    }


    private void processTimeout(Order order) {
        // 알림 전송
        orderNotificationPort.notifyOrderTimeout(order);

        // 주문 아이템 삭제
        List<OrderItem> items = orderItemRepositoryPort.findByOrderId(order.getId());
        if (!items.isEmpty()) {
            orderItemRepositoryPort.deleteAll(items);
        }

        // 주문 삭제
        orderRepositoryPort.delete(order);
        
        log.info("[OrderTimeout] 주문이 만료되어 삭제되었습니다. orderId={}, orderNumber={}", order.getId(), order.getOrderNumber());
    }
}
