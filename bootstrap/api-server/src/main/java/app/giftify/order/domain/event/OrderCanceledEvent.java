package app.giftify.order.domain.event;

import app.giftify.support.common.event.BaseDomainEvent;
import app.giftify.order.domain.vo.CanceledItemSnapshot;

import java.util.List;

public class OrderCanceledEvent extends BaseDomainEvent {
    private final Long orderId;
    private final List<CanceledItemSnapshot> items;

    public OrderCanceledEvent(Long orderId, List<CanceledItemSnapshot> items) {
        super();
        this.orderId = orderId;
        this.items = items;
    }

    public Long getOrderId() {
        return orderId;
    }

    public List<CanceledItemSnapshot> getItems() {
        return items;
    }
}
