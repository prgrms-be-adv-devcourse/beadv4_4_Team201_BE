package app.giftify.wishlist.core.domain.exception;

import app.giftify.shared.api.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum WishlistErrorCode implements ErrorCode {
    // [000 ~ 099] 공통 및 입력값 유효성
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST.value(), "W001", "유효하지 않은 입력값입니다."),
    INVALID_WISHLIST_ID(HttpStatus.BAD_REQUEST.value(), "W002", "유효하지 않은 위시리스트 ID입니다."),
    INVALID_WISHLIST_VALUE(HttpStatus.BAD_REQUEST.value(), "W003", "유효하지 않은 위시리스트 상태입니다."),

    // [100 ~ 199] 조회 및 리소스 존재 여부
    WISHLIST_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "W101", "위시리스트를 찾을 수 없습니다."),
    WISHLIST_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "W102", "위시리스트 아이템을 찾을 수 없습니다."),
    INVALID_WISHLIST_ITEM_STAUTS(HttpStatus.BAD_REQUEST.value(), "W103", "담을 수 없는 상품입니다."),
    WISHLIST_NOT_ACCESSIBLE(HttpStatus.BAD_REQUEST.value(), "W104", "위시리스트 조회 권한이 없습니다."),

    // [200 ~ 299] 상태 변경 및 비즈니스 흐름 제어
    DUPLICATE_WISHLIST_ITEM(HttpStatus.BAD_REQUEST.value(), "W201", "이미 위시리스트에 존재하는 아이템입니다."),
    WISHLIST_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST.value(), "W202", "위시리스트 담기 제한을 초과했습니다."),
    NOT_WISHLIST_OWNER(HttpStatus.FORBIDDEN.value(), "W203", "위시리스트의 소유자가 아닙니다."),
    WISHLIST_ITEM_NOT_REMOVABLE(HttpStatus.BAD_REQUEST.value(), "W204", "위시리스트 아이템을 삭제할 수 있는 상태가 아닙니다."),
    WISHLIST_ITEM_INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST.value(), "W207", "위시리스트 아이템 상태 전이가 허용되지 않습니다."),

    // [900 ~ 999] 시스템 및 내부 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "W999", "서버 내부 오류가 발생했습니다.");

    private final int statusCode;
    private final String code;
    private final String message;

    WishlistErrorCode(int statusCode, String code, String message) {
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
