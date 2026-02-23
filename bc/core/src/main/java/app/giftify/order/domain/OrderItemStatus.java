package app.giftify.order.domain;

import java.util.EnumSet;

public enum OrderItemStatus {
    CREATED,
    PAID,
    CANCELING,
    CANCELED,
    CONFIRMED,
    ;

    public static boolean isSettleable(OrderItemStatus status) {
        return EnumSet.of(CONFIRMED, PAID).contains(status);
    }
}
