package app.giftify.funding.domain.exception;

import app.giftify.support.common.api.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum FundingErrorCode implements ErrorCode {
    INVALID_AMOUNT (HttpStatus.BAD_REQUEST.value(), "F001", "참여 금액은 1,000원 이상이어야 합니다"),
    EXCEED_REMAINING_AMOUNT (HttpStatus.BAD_REQUEST.value(), "F002", "펀딩 잔여 금액을 초과할 수 없습니다"),
    NOT_IN_PROGRESS (HttpStatus.BAD_REQUEST.value(), "F003", "진행 중인 펀딩이 아닙니다. ID: %d"),
    ALREADY_TERMINATED (HttpStatus.BAD_REQUEST.value(), "F004", "이미 완료된 펀딩입니다. ID: %d"),
    WISHLIST_ITEM_NOT_FOUND (HttpStatus.NOT_FOUND.value(), "F005", "위시리스트 상품이 존재하지 않습니다"),
    FUNDING_NOT_FOUND (HttpStatus.NOT_FOUND.value(), "F006", "펀딩을 찾을 수 없습니다"),
    IS_NOT_EXPIRED (HttpStatus.BAD_REQUEST.value(), "F007", "펀딩 기한이 만료되지 않았습니다. ID: %d"),
    FORBIDDEN (HttpStatus.FORBIDDEN.value(), "F008", "해당 펀딩에 대한 권한이 없습니다"),
    NOT_ACHIEVED (HttpStatus.BAD_REQUEST.value(), "F009", "목표 달성된 펀딩이 아닙니다. ID: %d"),
    DUPLICATED_WISHLIST_ITEM (HttpStatus.CONFLICT.value(), "F010", "중복된 위시리스트 아이템이 포함되어 있습니다."),
    ALREADY_DECIDED (HttpStatus.BAD_REQUEST.value(), "F011", "이미 수락 또는 거절된 펀딩입니다. ID: %d"),
    RECEIVER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "F012", "수령자를 찾을 수 없습니다."),
    INVALID_STATUS_FOR_WITHDRAWAL (HttpStatus.BAD_REQUEST.value(), "F013", "진행 중이거나 달성 상태의 펀딩만 취소 가능합니다."),
    INVALID_STATUS_FOR_ACCEPTANCE_PENDING (HttpStatus.BAD_REQUEST.value(), "F014", "달성 또는 수락 실패 상태의 펀딩만 확정할 수 있습니다. ID: %d, 현재 상태: %s"),
    ALREADY_PENDING (HttpStatus.BAD_REQUEST.value(), "F015", "이미 수락 진행 중입니다. ID: %d"),
    INVALID_STATUS_FOR_RETRY_ACCEPT(HttpStatus.BAD_REQUEST.value(), "F016", "수락 대기 상태인 펀딩만 재시도할 수 있습니다. ID: %d"),
    INVALID_FUNDING_STATUS(HttpStatus.BAD_REQUEST.value(), "F017", "올바른 상태가 아닙니다. 펀딩ID: %d, 현재 상태: %s"),

    EXTERNAL_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "F100", "외부 API 오류입니다."),
    SNAPSHOT_INCONSISTENCY(HttpStatus.INTERNAL_SERVER_ERROR.value(), "F101", "위시리스트 아이템 스냅샷 데이터가 일관되지 않습니다.");

    private final int statusCode;
    private final String code;
    private final String message;

    FundingErrorCode(int statusCode, String code, String message) {
        this.statusCode = statusCode;
        this.code = code;
        this.message = message;
    }

    @Override
    public int getStatusCode() { return statusCode; }

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
