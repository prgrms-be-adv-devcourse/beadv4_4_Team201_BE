package app.giftify.shared.domain.event.product;

import app.giftify.shared.domain.event.BaseDomainEvent;
import app.giftify.shared.domain.vo.SellerOrderItem;

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
