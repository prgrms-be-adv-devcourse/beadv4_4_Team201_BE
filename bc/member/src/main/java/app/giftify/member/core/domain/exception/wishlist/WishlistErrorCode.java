package app.giftify.member.core.domain.exception.wishlist;

import app.giftify.shared.api.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WishlistErrorCode implements ErrorCode {
    // [000 ~ 099] 공통 및 입력값 유효성
    INVALID_INPUT_VALUE("W001", "유효하지 않은 입력값입니다."),
    INVALID_WISHLIST_ID("W002", "유효하지 않은 위시리스트 ID입니다."),

    // [100 ~ 199] 조회 및 리소스 존재 여부
    WISHLIST_NOT_FOUND("W101", "위시리스트를 찾을 수 없습니다."),
    WISHLIST_ITEM_NOT_FOUND("W102", "위시리스트 아이템을 찾을 수 없습니다."),

    // [200 ~ 299] 상태 변경 및 비즈니스 흐름 제어
    DUPLICATE_WISHLIST_ITEM("W201", "이미 위시리스트에 존재하는 아이템입니다."),
    WISHLIST_LIMIT_EXCEEDED("W202", "위시리스트 담기 제한을 초과했습니다."),
    NOT_WISHLIST_OWNER("W203", "위시리스트의 소유자가 아닙니다."),

    // [900 ~ 999] 시스템 및 내부 오류
    INTERNAL_SERVER_ERROR("W999", "서버 내부 오류가 발생했습니다.");

    private final String code;
    private final String message;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
