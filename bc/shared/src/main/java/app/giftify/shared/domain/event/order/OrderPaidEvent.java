package app.giftify.shared.domain.event.order;

import app.giftify.shared.domain.event.BaseDomainEvent;

// 주문 결제 완료 이벤트
public class OrderPaidEvent extends BaseDomainEvent {
    private final Long orderId;
    private final String orderNumber;
    private final String paymentKey;

    public OrderPaidEvent(Long orderId, String orderNumber, String paymentKey) {
        super();
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.paymentKey = paymentKey;
    }

    public Long getOrderId() {return orderId;}
    public String getOrderNumber() {return orderNumber;}
    public String getPaymentKey() {return paymentKey;}
}
