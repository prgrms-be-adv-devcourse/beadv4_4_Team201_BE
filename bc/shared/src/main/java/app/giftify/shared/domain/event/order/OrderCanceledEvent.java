package app.giftify.shared.domain.event.order;

import app.giftify.shared.domain.event.BaseDomainEvent;

// 주문 취소 완료 이벤트
public class OrderCanceledEvent extends BaseDomainEvent {
    private final Long orderId;
    private final String orderNumber;

    public OrderCanceledEvent(Long orderId, String orderNumber) {
        super();
        this.orderId = orderId;
        this.orderNumber = orderNumber;
    }

    public Long getOrderId() { return orderId; }
    public String getOrderNumber() { return orderNumber; }
}
