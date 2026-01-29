package app.giftify.shared.domain.event.product;

import app.giftify.shared.domain.event.BaseDomainEvent;

import java.time.LocalDateTime;

public class ProductReplicaCreationRequestedEvent extends BaseDomainEvent {
    private final Long id;
    private final String name;
    private final int price;

    public ProductReplicaCreationRequestedEvent(
            LocalDateTime occurredAt,
            Long id,
            String name,
            int price
    ) {
        super(occurredAt);
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

}
