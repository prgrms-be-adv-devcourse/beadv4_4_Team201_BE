package app.giftify.order.application.port.in;

import app.giftify.order.domain.domain.Order;

import java.util.List;

public interface OrderQueryPortUseCase {
    // 주문 조회
    Order getOrder(Long orderId);

    // 판매자 기준 조회
    List<Order> getOrdersBySeller(Long sellerId);

    // 구매자 기준 조회
    List<Order> getOrdersByBuyer(Long buyerId);
}
