package app.giftify.order.application.port.in;

import app.giftify.order.domain.domain.Order;
import app.giftify.shared.domain.vo.Money;
import app.giftify.shared.domain.vo.Quantity;

import java.util.List;

public interface OrderUseCase {

    // REQ-01: 펀딩 참여 시 Order + OrderItem 생성
    // REQ-02: 상태는 PAYMENT_PENDING
    // REQ-03: 주문번호 자동 생성
    Order createOrder(CreateOrderCommand command);

    // REQ-04: 결제 성공 이벤트 수신
    // REQ-05: Order → ORDERED
    // REQ-06: 모든 OrderItem → ORDERED
    void payOrder(PayOrderCommand command);

    // REQ-07: 결제 대기 유효시간 초과 시 자동 취소
    // REQ-08: 중복 취소 방지
    void cancelOrder(CancelOrderCommand command);

    // REQ-09: 수령자만 확정 가능
    // REQ-10: OrderItem → CONFIRMED
    // REQ-11: 모든 아이템 확정 시 Order → CONFIRMED
    void confirmOrderItem(ConfirmOrderItemCommand command);

    record CreateOrderCommand(
            Long buyerId,
            List<OrderItemCommand> orderItems
    ) {
        public record OrderItemCommand(
                Long fundingId,
                Long productId,
                Long sellerId,
                Long receiverId,
                Money price,
                Quantity quantity
        ) {
        }
    }

    record PayOrderCommand(
            Long orderId,
            String paymentKey
    ) {
    }

    record CancelOrderCommand(
            Long orderId
    ) {
    }

    record ConfirmOrderItemCommand(
            Long orderId,
            Long orderItemId,
            Long receiverId
    ) {
    }
}