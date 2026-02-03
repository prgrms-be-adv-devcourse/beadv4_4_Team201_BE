package app.giftify.orderDemo.domain.errorCode;

import org.springframework.http.HttpStatus;

public enum OrderErrorCode implements ErrorCode {

    INVALID_TARGET_ID(HttpStatus.BAD_REQUEST, "ORDER-100", "유효하지 않은 상품 아이디입니다.", false),
    INVALID_SELLER_ID(HttpStatus.BAD_REQUEST, "ORDER-101", "유효하지 않은 판매자 ID입니다.", false),
    AMOUNT_EXCEEDS_PRICE(HttpStatus.BAD_REQUEST, "ORDER-102", "주문 금액이 상품 금액을 초과했습니다.", false),

    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER-103", "주문을 찾을 수 없습니다.", false),
    ORDER_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER-104", "주문 아이템을 찾을 수 없습니다.", false),

    INVALID_TARGET_TYPE(HttpStatus.BAD_REQUEST, "ORDER-105", "유효하지 않은 타겟 타입입니다.", false),
    INVALID_RECEIVER_ID(HttpStatus.BAD_REQUEST, "ORDER-106", "유효하지 않은 수령자 ID입니다.", false),
    INVALID_PRICE(HttpStatus.BAD_REQUEST, "ORDER-107", "유효하지 않은 상품 가격입니다.", false),

    INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "ORDER-108", "유효하지 않은 주문 가격입니다.", false),
    INVALID_BUYER_ID(HttpStatus.BAD_REQUEST, "ORDER-109", "유효하지 않은 구매자 ID입니다.", false),
    INVALID_ORDER_ITEM(HttpStatus.BAD_REQUEST, "ORDER-110", "유효하지 않은 주문 항목입니다.", false),
    INVALID_PAYMENT_METHOD(HttpStatus.BAD_REQUEST, "ORDER-111", "유효하지 않은 결제 수단입니다.", false),

    INVALID_TOTAL_AMOUNT(HttpStatus.BAD_REQUEST, "ORDER-112", "총 금액은 1000원 이상이어야 합니다.", false),
    ORDER_ITEM_NOT_ASSOCIATED(HttpStatus.INTERNAL_SERVER_ERROR, "ORDER-113", "주문 아이템이 주문과 연결되지 않았습니다.", false),
    INVALID_ORDER_TYPE(HttpStatus.BAD_REQUEST, "ORDER-114", "유효하지 않은 주문 타입입니다.", false),

    ORDER_OWNER_MISMATCH(HttpStatus.FORBIDDEN, "ORDER-115", "주문 소유자 불일치로 접근 권한이 없습니다.", false);

    private final HttpStatus status;
    private final String code;
    private final String message;
    private final boolean retryable;

    OrderErrorCode(HttpStatus status, String code, String message, boolean retryable) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.retryable = retryable;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public boolean isRetryable() {
        return retryable;
    }
}
