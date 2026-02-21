package app.giftify.orderDemo.domain;

import java.util.List;

public enum OrderItemStatus {
    CREATED,
    PAID,
    CANCELING,
    CANCELED,
    CONFIRMED
    ;

    public static List<OrderItemStatus> getCancelableStatuses() {
        return List.of(CREATED, PAID);
    }
}
