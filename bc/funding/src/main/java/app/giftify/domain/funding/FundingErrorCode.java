package app.giftify.domain.funding;

import app.giftify.shared.api.exception.ErrorCode;

public enum FundingErrorCode implements ErrorCode {
    INVALID_AMOUNT("F001", "참여 금액은 1,000원 이상이여야 합니다"),
    EXCEED_REMAINING_AMOUNT("F002", "펀딩 잔여 금액을 초과할 수 없습니다"),
    NOT_IN_PROGRESS("F003", "진행 중인 펀딩만 참여할 수 있습니다"),
    ALREADY_CLOSED("F004", "이미 완료된 펀딩은 만료 처리할 수 없습니다"),
    WISHLIST_ITEM_NOT_FOUND("F005", "위시리스트 상품이 존재하지 않습니다");

    private final String code;
    private final String message;

    FundingErrorCode(String code, String message) {
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

