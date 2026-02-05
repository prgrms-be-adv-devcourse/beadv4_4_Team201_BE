package app.giftify.orderDemo.domain;

import app.giftify.shared.domain.type.PaymentMethodType;
import app.giftify.shared.domain.vo.Money;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderSnapshot(
        Long orderId,
        String orderNumber,
        Long buyerId,
        List<OrderItemSnapshot> orderItemSnapshots,
        Money totalAmount,
        PaymentMethodType paymentMethod,
        OrderStatus status,
        LocalDateTime createdAt
) {
}