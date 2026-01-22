package app.giftify.order.application.port.out;

import app.giftify.order.domain.domain.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepositoryPort {

    // Order 저장 (생성/수정 모두 포함)
    Order save(Order order);

    // Order 조회 (비관적 락 사용 가능)
    Optional<Order> findByIdWithLock(Long orderId);

    // Order 조회
    Optional<Order> findById(Long orderId);

    // PAYMENT_PENDING 상태 중 특정 시간 이전 주문 조회
    // 자동 취소 스케줄러/배치에서 사용
    List<Order> findPaymentPendingOrdersOlderThan(long minutes);

    // 특정 펀딩 ID를 포함하는 주문 목록 조회
    List<Order> findAllByFundingId(Long fundingId);
}