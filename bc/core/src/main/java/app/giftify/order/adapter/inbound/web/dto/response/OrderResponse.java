package app.giftify.order.adapter.inbound.web.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import app.giftify.order.domain.Order;
import app.giftify.order.domain.OrderItem;
import app.giftify.order.domain.OrderStatus;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.TargetType;
import lombok.Builder;

@Builder
public record OrderResponse(
        Long id,
        String orderNumber,
        Long buyerId,
        long totalAmount,
        PaymentMethod paymentMethod,
        OrderStatus status,
        LocalDateTime createdAt,
        List<OrderItemResponse> items
) {
    public static OrderResponse from(Order order, List<OrderItemResponse> items) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .buyerId(order.getBuyerId())
                .totalAmount(order.getTotalAmount().amount().longValue())
                .paymentMethod(order.getPaymentMethod())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }

    public record OrderItemResponse(
            Long id,
            Long targetSnapshotId,
            TargetType targetType,
            Long sellerId,
            Long receiverId,
            long price,
            int quantity
    ) {
        public static OrderItemResponse from(OrderItem item) {
            return new OrderItemResponse(
                    item.getId(),
                    item.getTargetSnapshotId(),
                    item.getTargetType(),
                    item.getSellerId(),
                    item.getReceiverId(),
                    item.getPrice().amount().longValue(),
                    item.getQuantity().getValue()
            );
        }
    }
}
