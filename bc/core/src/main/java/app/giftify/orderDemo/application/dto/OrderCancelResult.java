package app.giftify.orderDemo.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OrderCancelResult {
    CANCEL_SUCCESS(200, "주문 취소가 완료되었습니다."),
    CANCEL_PENDING(202, "주문 취소 요청이 접수되었습니다."),
    ALREADY_CANCELED(200, "이미 처리된 주문 취소 건입니다."),
    IN_PROGRESS(202, "주문 취소 요청이 진행 중입니다.");

    private final int statusCode;
    private final String message;
}
