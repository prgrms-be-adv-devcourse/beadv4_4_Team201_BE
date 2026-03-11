package app.giftify.shared.domain.event.order;

import app.giftify.shared.domain.event.BaseDomainEvent;
import app.giftify.shared.domain.vo.ConfirmItem;

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
