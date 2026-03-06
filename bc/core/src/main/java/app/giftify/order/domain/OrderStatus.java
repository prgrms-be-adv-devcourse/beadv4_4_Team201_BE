package app.giftify.order.domain;

import java.util.EnumSet;
import java.util.List;

public enum OrderStatus {
    CREATED,            // 모든 아이템이 CREATED인 경우
    PAID,               // 모든 아이템이 PAID인 경우
    PARTIAL_CONFIRMED,  // 하나라도 아이템에 CONFIRMED가 포함된 경우(우선순위: 2)
    CONFIRMED,          // 모든 아이템이 CONFIRMED인 경우
    PARTIAL_CANCELING,    // 하나라도 아이템에 CANCELING이 포함된 경우 (우선순위: 1)
    CANCELING,          // 모든 아이템이 CANCELING인 경우
    PARTIAL_CANCELED,   // 일부 취소 후 남은 아이템들이 PAID인 경우 (우선순위: 3)
    CANCELED,            // 모든 아이템이 CANCELED인 경우
    EXPIRED
    ;

    /**
     * EXPIRED는 주문 아이템 단위가 아닌 주문 단위이다.
     * 따라서 주문 아이템을 기준으로 주문 상태를 결정하는 deriveStatus 메서드 내에서 주문 만료는 고려하지 않는다.
     */
    public static OrderStatus deriveStatus(List<OrderItemStatus> itemStatuses) {
        // 1. 모든 아이템이 동일한 최종 상태인가? (CANCELING, CANCELED, CONFIRMED, PAID 등)
        if (isAllSameStatus(itemStatuses)) {
            return OrderStatus.valueOf(itemStatuses.get(0).name());
        }

        // 2. 복합 상태: 취소 진행 중인 건이 있는가?
        if (itemStatuses.contains(OrderItemStatus.CANCELING)) {
            return OrderStatus.PARTIAL_CANCELING;
        }

        // 3. 복합 상태 - 확정된 건이 하나라도 있는가?
        if (itemStatuses.contains(OrderItemStatus.CONFIRMED)) {
            return OrderStatus.PARTIAL_CONFIRMED;
        }

        // 4. 복합 상태 - 취소된 건이 섞여 있는가?
        if (itemStatuses.contains(OrderItemStatus.CANCELED)) {
            return OrderStatus.PARTIAL_CANCELED;
        }

        return OrderStatus.PAID;
    }

    private static boolean isAllSameStatus(List<OrderItemStatus> itemStatuses) {
        return itemStatuses.stream()
                .distinct()
                .count() == 1;
    }

    public boolean isFullCancelable() {
        return EnumSet.of(CREATED, PAID).contains(this);
    }
}
