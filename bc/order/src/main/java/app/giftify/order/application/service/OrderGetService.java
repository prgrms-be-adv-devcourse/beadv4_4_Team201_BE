package app.giftify.order.application.service;

import app.giftify.order.application.port.in.OrderQueryPortUseCase;
import app.giftify.order.application.port.out.OrderQueryPort;
import app.giftify.order.domain.domain.Order;
import app.giftify.shared.domain.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 조회 관련 비즈니스 로직 처리
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class OrderGetService implements OrderQueryPortUseCase {

    private final OrderQueryPort orderQueryPort;

    @Override
    public Order getOrder(Long orderId) {
        return orderQueryPort.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("orderId로 주문을 찾을 수 없습니다. [orderId=" + orderId + "]"));
    }

    @Override
    public List<Order> getOrdersBySeller(Long sellerId) {
        return orderQueryPort.findBySellerId(sellerId);
    }

    @Override
    public List<Order> getOrdersByBuyer(Long buyerId) {
        return orderQueryPort.findByBuyerId(buyerId);
    }
}
