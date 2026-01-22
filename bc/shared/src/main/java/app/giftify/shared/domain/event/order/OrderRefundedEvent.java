package app.giftify.shared.domain.event.order;

import app.giftify.shared.domain.event.BaseDomainEvent;

// 주문 환불 완료 이벤트
public class OrderRefundedEvent extends BaseDomainEvent {
    private final Long orderId;
    private final String orderNumber;

    public OrderRefundedEvent(Long orderId, String orderNumber) {
        super();
        this.orderId = orderId;
        this.orderNumber = orderNumber;
    }

    public Long getOrderId() { return orderId; }
    public String getOrderNumber() { return orderNumber; }
}
