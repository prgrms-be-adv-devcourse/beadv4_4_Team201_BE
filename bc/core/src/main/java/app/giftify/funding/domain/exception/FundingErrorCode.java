package app.giftify.funding.domain.exception;

import app.giftify.shared.api.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum FundingErrorCode implements ErrorCode {
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST,"F001", "참여 금액은 1,000원 이상이여야 합니다"),
    EXCEED_REMAINING_AMOUNT(HttpStatus.BAD_REQUEST,"F002", "펀딩 잔여 금액을 초과할 수 없습니다"),
    NOT_IN_PROGRESS(HttpStatus.BAD_REQUEST,"F003", "진행 중인 펀딩이 아닙니다. ID: %d"),
    ALREADY_TERMINATED(HttpStatus.BAD_REQUEST,"F004", "이미 완료된 펀딩입니다. ID: %d"),
    WISHLIST_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND,"F005", "위시리스트 상품이 존재하지 않습니다. ID: %d"),
    FUNDING_NOT_FOUND(HttpStatus.NOT_FOUND,"F006", "펀딩을 찾을 수 없습니다. ID: %d"),
    IS_NOT_EXPIRED(HttpStatus.BAD_REQUEST,"F007", "펀딩 기한이 만료되지 않았습니다. ID: %d"),
    FORBIDDEN(HttpStatus.FORBIDDEN,"F008", "해당 펀딩에 대한 권한이 없습니다"),
    NOT_ACHIEVED(HttpStatus.FORBIDDEN,"F009", "목표 달성된 펀딩이 아닙니다. ID: %d" ),

    // 외부 에러
    EXTERNAL_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "F100", "외부 API 오류입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    FundingErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    FundingErrorCode(String code, String message) {
        this.httpStatus = HttpStatus.BAD_REQUEST;
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
