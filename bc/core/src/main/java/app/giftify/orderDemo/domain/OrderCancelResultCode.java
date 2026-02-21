package app.giftify.orderDemo.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OrderCancelResultCode {
    SUCCESS(200, "주문 취소 요청이 성공적으로 처리되었습니다."),
    PARTIAL_SUCCESS(200, "일부 주문 취소 요청이 성공적으로 처리되었습니다."),
    ACCEPTED(202, "주문 취소 요청이 접수되어 처리 중입니다."),
    ALREADY_PROCESSED(200, "이미 처리가 완료된 주문 취소 요청입니다."),
    IN_PROGRESS(202, "현재 동일한 주문 취소 요청이 처리되고 있습니다."),
    ALREADY_CONFIRMED(400, "주문 확정 건은 취소가 불가능합니다."),
    MANUAL_INTERVENTION_REQUIRED(500, "시스템 자동 처리가 불가하여 관리자 확인이 필요합니다.");

    private final int statusCode;
    private final String message;

    public static OrderCancelResultCode determineCancelResultCode(OrderItemStatus status) {
        return switch (status) {
            case OrderItemStatus.CREATED -> SUCCESS;
            case OrderItemStatus.PAID -> ACCEPTED;
            case OrderItemStatus.CANCELING -> IN_PROGRESS;
            case OrderItemStatus.CANCELED -> ALREADY_PROCESSED;
            case OrderItemStatus.CONFIRMED -> ALREADY_CONFIRMED;
            default -> MANUAL_INTERVENTION_REQUIRED;
        };
    }
}
