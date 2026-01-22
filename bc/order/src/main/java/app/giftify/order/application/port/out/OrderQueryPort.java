package app.giftify.order.application.port.out;

import app.giftify.order.domain.domain.Order;

import java.util.List;
import java.util.Optional;

public interface OrderQueryPort {
    // 주문 id 기준 조회
    Optional<Order> findById(Long orderId);

    // 구매자 기준 조회
    List<Order> findByBuyerId(Long buyerId);

    // 판매자 기준 조회
    List<Order> findBySellerId(Long sellerId);
}
