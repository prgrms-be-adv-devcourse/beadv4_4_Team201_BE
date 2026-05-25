package app.giftify.order.domain;

import app.giftify.payment.domain.type.PaymentMethod;
import app.giftify.support.common.money.Money;
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
        PaymentMethod paymentMethod,
        OrderStatus status,
        LocalDateTime createdAt
) {
}
