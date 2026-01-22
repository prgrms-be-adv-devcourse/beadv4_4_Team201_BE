package app.giftify.order.domain.event;

import app.giftify.shared.domain.event.BaseDomainEvent;
import lombok.Getter;

import java.util.List;

// 주문 생성 완료 이벤트
@Getter
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
}
