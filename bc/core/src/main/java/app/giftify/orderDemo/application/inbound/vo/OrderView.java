package app.giftify.orderDemo.application.inbound.vo;

import app.giftify.orderDemo.domain.Order;
import app.giftify.orderDemo.domain.OrderStatus;
import app.giftify.shared.domain.type.PaymentMethodType;
import app.giftify.shared.domain.vo.Money;

import java.time.LocalDateTime;

public record OrderView(
        Long orderId,
        String orderNumber,
        Long quantity,
        Money totalAmount,
        OrderStatus status,
        PaymentMethodType paymentMethod,
        LocalDateTime createdAt,
        LocalDateTime confirmedAt,
        LocalDateTime cancelledAt
) {
    public static OrderView of(Order order) {
        return new OrderView(
                order.getId(),
                order.getOrderNumber(),
                order.getQuantity(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getPaymentMethod(),
                order.getCreatedAt(),
                order.getConfirmedAt(),
                order.getCancelledAt()
        );
    }
}
