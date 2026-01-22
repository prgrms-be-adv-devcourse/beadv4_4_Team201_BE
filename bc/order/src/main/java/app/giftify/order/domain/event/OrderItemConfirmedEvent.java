package app.giftify.order.domain.event;

import app.giftify.shared.domain.event.BaseDomainEvent;
import app.giftify.shared.domain.vo.Money;
import lombok.Getter;

// 주문 아이템 확정 이벤트 (정산 트리거)
@Getter
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
}
