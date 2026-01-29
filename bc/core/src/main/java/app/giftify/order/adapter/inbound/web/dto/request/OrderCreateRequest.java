package app.giftify.order.adapter.inbound.web.dto.request;

import java.util.List;

import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.TargetType;
import lombok.Builder;

@Builder
public record OrderCreateRequest (
        Long buyerId,
        PaymentMethod paymentMethod,
        List<OrderItemRequest> items
) {
    public OrderCreateRequest withBuyerId(Long buyerId) {
        return new OrderCreateRequest(buyerId, this.paymentMethod, this.items);
    }

    public record OrderItemRequest(
            Long targetSnapshotId,
            TargetType targetType,
            Long sellerId,
            Long receiverId,
            long price,
            int quantity
    ) {}
}
