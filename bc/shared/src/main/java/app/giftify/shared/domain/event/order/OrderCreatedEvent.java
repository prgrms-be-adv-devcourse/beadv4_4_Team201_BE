package app.giftify.shared.domain.event.order;

import app.giftify.shared.domain.event.BaseDomainEvent;

import java.util.List;

// 주문 생성 완료 이벤트
public class OrderCreatedEvent extends BaseDomainEvent {
    private final Long orderId;
    private final String orderNumber;
    private final Long buyerId;
    private final List<OrderItemInfo> items;

    public OrderCreatedEvent(Long orderId, String orderNumber, Long buyerId, List<OrderItemInfo> items) {
        super();
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.buyerId = buyerId;
        this.items = items;
    }

    public record OrderItemInfo(
            Long orderItemId,
            Long fundingId,
            Long productId,
            Long receiverId
    ) {}

    public Long getOrderId() {return orderId;}
    public String getOrderNumber() {return orderNumber;}
    public Long getBuyerId() {return buyerId;}
    public List<OrderItemInfo> getItems() {return items;}
}
