package app.giftify.orderDemo.domain.errorCode;

import app.giftify.shared.api.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum OrderErrorCode implements ErrorCode {

    INVALID_TARGET_ID(HttpStatus.BAD_REQUEST.value(), "ORDER-100", "유효하지 않은 상품 아이디입니다."),
    INVALID_SELLER_ID(HttpStatus.BAD_REQUEST.value(), "ORDER-101", "유효하지 않은 판매자 ID입니다."),
    AMOUNT_EXCEEDS_PRICE(HttpStatus.BAD_REQUEST.value(), "ORDER-102", "주문 금액이 상품 금액을 초과했습니다."),

    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "ORDER-103", "주문을 찾을 수 없습니다."),
    ORDER_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "ORDER-104", "주문 아이템을 찾을 수 없습니다."),

    INVALID_TARGET_TYPE(HttpStatus.BAD_REQUEST.value(), "ORDER-105", "유효하지 않은 타겟 타입입니다."),
    INVALID_RECEIVER_ID(HttpStatus.BAD_REQUEST.value(), "ORDER-106", "유효하지 않은 수령자 ID입니다."),
    INVALID_PRICE(HttpStatus.BAD_REQUEST.value(), "ORDER-107", "유효하지 않은 상품 가격입니다."),

    INVALID_AMOUNT(HttpStatus.BAD_REQUEST.value(), "ORDER-108", "유효하지 않은 주문 가격입니다."),
    INVALID_BUYER_ID(HttpStatus.BAD_REQUEST.value(), "ORDER-109", "유효하지 않은 구매자 ID입니다."),
    INVALID_ORDER_ITEM(HttpStatus.BAD_REQUEST.value(), "ORDER-110", "유효하지 않은 주문 항목입니다."),
    INVALID_PAYMENT_METHOD(HttpStatus.BAD_REQUEST.value(), "ORDER-111", "유효하지 않은 결제 수단입니다."),

    INVALID_TOTAL_AMOUNT(HttpStatus.BAD_REQUEST.value(), "ORDER-112", "총 금액은 1000원 이상이어야 합니다."),
    ORDER_ITEM_NOT_ASSOCIATED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "ORDER-113", "주문 아이템이 주문과 연결되지 않았습니다."),
    INVALID_ORDER_TYPE(HttpStatus.BAD_REQUEST.value(), "ORDER-114", "유효하지 않은 주문 타입입니다."),

    ORDER_OWNER_MISMATCH(HttpStatus.FORBIDDEN.value(), "ORDER-115", "주문 소유자 불일치로 접근 권한이 없습니다."),
    ALREADY_FUNDING_IN_PROGRESS(HttpStatus.CONFLICT.value(), "ORDER-116", "이미 펀딩이 진행된 상품으로 일반 선물이 불가능합니다"),
    UNSUPPORTED_ORDER_COMBINATION(HttpStatus.BAD_REQUEST.value(), "ORDER-117", "해당 상품 상태에서는 요청하신 주문 처리가 불가능합니다."),
    SNAPSHOTS_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "ORDER-118", "주문 상품의 상세 정보(펀딩, 위시리스트 아이템)를 불러올 수 없습니다."),

    INVALID_STATUS_TRANSITION(HttpStatus.CONFLICT.value(), "ORDER-119", "잘못된 상태 변경 요청입니다."),
    ALREADY_CANCELED(HttpStatus.OK.value(), "ORDER-120", "이미 취소된 주문입니다."),
    IN_PROGRESS_CANCEL(HttpStatus.ACCEPTED.value(), "ORDER-121", "취소가 진행 중입니다."),
    INVALID_STATUS_CANCEL(HttpStatus.BAD_REQUEST.value(), "ORDER-122", "취소가 불가능한 주문 상태입니다."),
    ;

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
