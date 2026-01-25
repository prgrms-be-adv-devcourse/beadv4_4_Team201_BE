package app.giftify.funding.domain.exception;

import app.giftify.shared.api.exception.ErrorCode;

public enum OrderErrorCode implements ErrorCode {

    // [000 ~ 099] 공통 / 입력값 검증
    INVALID_INPUT_VALUE("O001", "유효하지 않은 입력값입니다."),
    INVALID_ORDER_STATUS("O002", "유효하지 않은 주문 상태입니다."),
    INVALID_ORDER_ITEM_STATUS("O003", "유효하지 않은 주문 아이템 상태입니다."),

    // [100 ~ 199] 조회 / 리소스 존재
    ORDER_NOT_FOUND("O101", "주문을 찾을 수 없습니다."),
    ORDER_ITEM_NOT_FOUND("O102", "주문 아이템을 찾을 수 없습니다."),

    // [200 ~ 299] 주문 상태 / 비즈니스 규칙
    ORDER_ALREADY_CONFIRMED("O201", "이미 확정된 주문입니다."),
    ORDER_ALREADY_CANCELED("O202", "이미 취소된 주문입니다."),
    ORDER_CANNOT_BE_CANCELED("O203", "현재 상태에서는 주문을 취소할 수 없습니다."),
    ORDER_NOT_PAYABLE("O204", "결제할 수 없는 주문 상태입니다."),
    ORDER_NOT_CONFIRMABLE("O205", "확정할 수 없는 주문 상태입니다."),
    ORDER_CANNOT_REFUND("O206", "결제 이력이 없어 환불 가능한 상태가 아닙니다."),

    // [300 ~ 399] 주문 아이템 관련
    ORDER_ITEM_ALREADY_CONFIRMED("O301", "이미 확정된 주문 아이템입니다."),
    ORDER_ITEM_ALREADY_CANCELED("O302", "이미 취소된 주문 아이템입니다."),
    ORDER_ITEM_CANNOT_BE_CANCELED("O303", "주문 아이템을 취소할 수 없는 상태입니다."),

    // [400 ~ 499] 펀딩 / 결제 연동 정합성
    FUNDING_EVENT_MISMATCH("O401", "펀딩 이벤트와 주문 정보가 일치하지 않습니다."),
    PAYMENT_KEY_ALREADY_EXISTS("O402", "이미 결제가 완료된 주문입니다."),
    PAYMENT_NOT_COMPLETED("O403", "결제가 완료되지 않은 주문입니다."),

    // [900 ~ 999] 시스템
    INTERNAL_SERVER_ERROR("O999", "주문 처리 중 서버 오류가 발생했습니다.");

    private final String code;
    private final String message;

    OrderErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
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