package app.giftify.order.domain.exception;

import app.giftify.shared.api.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum OrderErrorCode implements ErrorCode {

    // [000 ~ 099] 공통 / 입력값 검증
    INVALID_INPUT_VALUE (HttpStatus.BAD_REQUEST.value(), "O001", "유효하지 않은 입력값입니다."),
    INVALID_ORDER_STATUS (HttpStatus.BAD_REQUEST.value(), "O002", "유효하지 않은 주문 상태입니다."),
    INVALID_ORDER_ITEM_STATUS (HttpStatus.BAD_REQUEST.value(), "O003", "유효하지 않은 주문 아이템 상태입니다."),

    // [100 ~ 199] 조회 / 리소스 존재
    ORDER_NOT_FOUND (HttpStatus.NOT_FOUND.value(), "O101", "주문을 찾을 수 없습니다."),
    ORDER_ITEM_NOT_FOUND (HttpStatus.NOT_FOUND.value(), "O102", "주문 아이템을 찾을 수 없습니다."),

    // [200 ~ 299] 주문 상태 / 비즈니스 규칙
    ORDER_ALREADY_CONFIRMED (HttpStatus.BAD_REQUEST.value(), "O201", "이미 확정된 주문입니다."),
    ORDER_ALREADY_CANCELED (HttpStatus.BAD_REQUEST.value(), "O202", "이미 취소된 주문입니다."),
    ORDER_CANNOT_BE_CANCELED (HttpStatus.BAD_REQUEST.value(), "O203", "현재 상태에서는 주문을 취소할 수 없습니다."),
    ORDER_NOT_PAYABLE (HttpStatus.BAD_REQUEST.value(), "O204", "결제할 수 없는 주문 상태입니다."),
    ORDER_NOT_CONFIRMABLE (HttpStatus.BAD_REQUEST.value(), "O205", "확정할 수 없는 주문 상태입니다."),
    ORDER_CANNOT_REFUND (HttpStatus.BAD_REQUEST.value(), "O206", "결제 이력이 없어 환불 가능한 상태가 아닙니다."),

    // [300 ~ 399] 주문 아이템 관련
    ORDER_ITEM_ALREADY_CONFIRMED (HttpStatus.BAD_REQUEST.value(), "O301", "이미 확정된 주문 아이템입니다."),
    ORDER_ITEM_ALREADY_CANCELED (HttpStatus.BAD_REQUEST.value(), "O302", "이미 취소된 주문 아이템입니다."),
    ORDER_ITEM_CANNOT_BE_CANCELED (HttpStatus.BAD_REQUEST.value(), "O303", "주문 아이템을 취소할 수 없는 상태입니다."),

    // [400 ~ 499] 펀딩 / 결제 연동 정합성
    FUNDING_EVENT_MISMATCH (HttpStatus.BAD_REQUEST.value(), "O401", "펀딩 이벤트와 주문 정보가 일치하지 않습니다."),
    PAYMENT_KEY_ALREADY_EXISTS (HttpStatus.CONFLICT.value(), "O402", "이미 결제가 완료된 주문입니다."),
    PAYMENT_NOT_COMPLETED (HttpStatus.BAD_REQUEST.value(), "O403", "결제가 완료되지 않은 주문입니다."),

    // [900 ~ 999] 시스템
    INTERNAL_SERVER_ERROR (HttpStatus.INTERNAL_SERVER_ERROR.value(), "O999", "주문 처리 중 서버 오류가 발생했습니다.");

    private final int statusCode;
    private final String code;
    private final String message;

    OrderErrorCode(int statusCode, String code, String message) {
        this.statusCode = statusCode;
        this.code = code;
        this.message = message;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
