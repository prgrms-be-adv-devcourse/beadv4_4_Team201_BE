package app.giftify.product.domain.event;

import app.giftify.support.common.event.BaseDomainEvent;
import app.giftify.order.domain.vo.SellerOrderItem;

import java.util.List;

public class ProductSellerOrderReceivedEvent extends BaseDomainEvent {
    private final List<SellerOrderItem> items;

    public ProductSellerOrderReceivedEvent(List<SellerOrderItem> items) {
        super();
        this.items = items;
    }

    public List<SellerOrderItem> getItems() {
        return items;
    }

    @Override
    public String toString() {
        return "ProductSellerOrderReceivedEvent{" +
                "items=" + items +
                ", eventId='" + getEventId() + "'" +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}
