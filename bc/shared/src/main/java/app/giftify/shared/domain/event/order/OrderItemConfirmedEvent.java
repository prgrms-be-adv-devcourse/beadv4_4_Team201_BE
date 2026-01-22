package app.giftify.shared.domain.event.order;

import app.giftify.shared.domain.event.BaseDomainEvent;
import app.giftify.shared.domain.vo.Money;

// 주문 아이템 확정 이벤트 (정산 트리거)
public class OrderItemConfirmedEvent extends BaseDomainEvent {
    private final Long orderId;
    private final Long orderItemId;
    private final Long sellerId;
    private final Long receiverId;
    private final Money price;
    private final Integer quantity;

    public OrderItemConfirmedEvent(Long orderId, Long orderItemId, Long sellerId, Long receiverId, Money price, Integer quantity) {
        super();
        this.orderId = orderId;
        this.orderItemId = orderItemId;
        this.sellerId = sellerId;
        this.receiverId = receiverId;
        this.price = price;
        this.quantity = quantity;
    }

    public Long getOrderId() {return orderId;}
    public Long getOrderItemId() {return orderItemId;}
    public Long getSellerId() {return sellerId;}
    public Long getReceiverId() {return receiverId;}
    public Money getPrice() {return price;}
    public Integer getQuantity() {return quantity;}
}
