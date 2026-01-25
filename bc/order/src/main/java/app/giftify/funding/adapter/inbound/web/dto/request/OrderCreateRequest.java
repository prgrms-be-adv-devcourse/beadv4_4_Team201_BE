package app.giftify.funding.adapter.inbound.web.dto.request;

import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.TargetType;
import lombok.Builder;

import java.util.List;

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
