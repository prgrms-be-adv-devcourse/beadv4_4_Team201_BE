package app.giftify.cart.core.domain.exception;


import app.giftify.support.common.api.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum CartErrorCode implements ErrorCode {
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST.value(), "C001", "참여 금액은 1,000원 이상이여야 합니다"),
    CARTITEM_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "C002", "장바구니 상품이 존재하지 않습니다"),
    CART_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "C003", "장바구니를 찾을 수 없습니다. ID: %d"),
    FORBIDDEN(HttpStatus.FORBIDDEN.value(), "C004", "해당 장바구니에 대한 권한이 없습니다. ID: %d"),
    INVALID_ITEM_STATUS(HttpStatus.BAD_REQUEST.value(), "C005", "상품 상태가 유효하지 않습니다. ID: %d"),
    CANNOT_ADD_ITEM(HttpStatus.BAD_REQUEST.value(), "C006", "상품을 장바구니에 담을 수 없습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "C007", "상품을 찾을 수 없습니다."),
    WISHLIST_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "C009", "위시리스트 아이템을 찾을 수 없습니다. wishlistItemId: %d"),
    INVALID_TARGET_TYPE(HttpStatus.BAD_REQUEST.value(), "C011", "지원하지 않는 상품 타입입니다."),
    CARTITEM_ID_REQUIRED(HttpStatus.BAD_REQUEST.value(), "C012", "장바구니 아이템 ID가 필요합니다."),
    CARTITEM_TYPE_REQUIRED(HttpStatus.BAD_REQUEST.value(), "C013", "장바구니 아이템 타입이 필요합니다."),
    EXCEED_REMAINING_AMOUNT(HttpStatus.BAD_REQUEST.value(), "C014","참여 금액은 펀딩 잔액 (%d원)을 초과할 수 없습니다."),
    EXCEED_PRODUCT_PRICE(HttpStatus.BAD_REQUEST.value(), "C015", "참여 금액은 상품 금액(%d원)을 초과할 수 없습니다.");


    private final int statusCode;
    private final String code;
    private final String message;

    CartErrorCode(int statusCode, String code, String message) {
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

    @Override
    public String formatMessage(Object... args) {
        return String.format(this.message, args);
    }
}