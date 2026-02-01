package app.giftify.shared.domain.event.order;

import app.giftify.shared.domain.event.BaseDomainEvent;

import java.math.BigDecimal;

/**
 * targetType == FUNDING 일 때만 발행
 */
public class OrderItemCreatedEvent extends BaseDomainEvent {
    private final Long orderItemId;
    private final Long fundingId;
    private final Long orderId;
    private final Long sellerId;
    private final BigDecimal price;
    private final BigDecimal amount;

    public OrderItemCreatedEvent(Long orderItemId, Long fundingId, Long orderId, Long sellerId, BigDecimal price, BigDecimal amount) {
        this.orderItemId = orderItemId;
        this.fundingId = fundingId;
        this.orderId = orderId;
        this.sellerId = sellerId;
        this.price = price;
        this.amount = amount;
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public Long getFundingId() {
        return fundingId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
