package app.giftify.order.domain.event;

import app.giftify.support.common.event.BaseDomainEvent;
import app.giftify.order.domain.vo.ConfirmItem;

import java.util.List;

public class OrderConfirmPendingEvent extends BaseDomainEvent {
    private final List<ConfirmItem> items;

    public OrderConfirmPendingEvent(List<ConfirmItem> items) {
        this.items = items;
    }

    public List<ConfirmItem> getItems() {
        return items;
    }
}
