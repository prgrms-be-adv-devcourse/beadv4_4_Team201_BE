package app.giftify.shared.domain.event.order;

import app.giftify.shared.domain.event.BaseDomainEvent;
import app.giftify.shared.domain.vo.CanceledItemSnapshot;

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
