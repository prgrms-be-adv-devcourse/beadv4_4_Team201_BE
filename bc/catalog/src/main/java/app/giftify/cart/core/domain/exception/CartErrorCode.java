package app.giftify.cart.core.domain.exception;


import app.giftify.shared.api.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum CartErrorCode implements ErrorCode {
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST,"C001", "참여 금액은 1,000원 이상이여야 합니다"),
    CARTITEM_NOT_FOUND(HttpStatus.NOT_FOUND,"C002", "장바구니 상품이 존재하지 않습니다"),
    CART_NOT_FOUND(HttpStatus.NOT_FOUND,"C003", "장바구니를 찾을 수 없습니다. ID: %d"),
    FORBIDDEN(HttpStatus.FORBIDDEN,"C004", "해당 장바구니에 대한 권한이 없습니다. ID: %d"),
    INVALID_ITEM_STATUS(HttpStatus.BAD_REQUEST, "C005", "상품 상태가 유효하지 않습니다. ID: %d");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    CartErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getHttpStatus() { return httpStatus; }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}