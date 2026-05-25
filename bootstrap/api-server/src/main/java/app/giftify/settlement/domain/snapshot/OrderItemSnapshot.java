package app.giftify.settlement.domain.snapshot;

import app.giftify.settlement.domain.errorCode.SettlementErrorCode;
import app.giftify.support.common.api.exception.DomainException;
import app.giftify.order.domain.event.OrderItemConfirmedEvent;
import app.giftify.order.domain.type.TargetType;
import app.giftify.support.common.money.Money;

import java.time.LocalDateTime;

public record OrderItemSnapshot(
        Long orderId,
        Long orderItemId,
        Long sellerId,
        Long targetId,
        TargetType targetType,
        Long paymentId,
        Money amount,
        LocalDateTime confirmedAt
) {
    public OrderItemSnapshot {
        if (orderId == null) throw new DomainException(SettlementErrorCode.MISSING_FIELD, "OrderID");
        if (orderItemId == null) throw new DomainException(SettlementErrorCode.MISSING_FIELD, "OrderItemID");
        if (sellerId == null) throw new DomainException(SettlementErrorCode.MISSING_FIELD, "SellerID");
        if (targetId == null) throw new DomainException(SettlementErrorCode.MISSING_FIELD, "TargetID");
        if (targetType == null) throw new DomainException(SettlementErrorCode.MISSING_FIELD, "TargetType");
        if (paymentId == null) throw new DomainException(SettlementErrorCode.MISSING_FIELD, "PaymentID");
        if (amount == null) throw new DomainException(SettlementErrorCode.MISSING_FIELD, "Amount");
        if (confirmedAt == null) throw new DomainException(SettlementErrorCode.MISSING_FIELD, "ConfirmedAt");
    }

    public static OrderItemSnapshot of(OrderItemConfirmedEvent event) {
        return new OrderItemSnapshot(
                event.getOrderId(),
                event.getOrderItemId(),
                event.getSellerId(),
                event.getTargetId(),
                event.getTargetType(),
                event.getPaymentId(),
                event.getAmount(),
                event.getConfirmedAt()
        );
    }
}
