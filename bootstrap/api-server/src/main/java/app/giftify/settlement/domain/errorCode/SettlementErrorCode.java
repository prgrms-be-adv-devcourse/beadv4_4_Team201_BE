package app.giftify.settlement.domain.errorCode;

import app.giftify.shared.api.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
public enum SettlementErrorCode implements ErrorCode {

    // 1. Not Found (404) - 존재해야 할 데이터가 없는 경우
    ORDER_ITEM_SNAPSHOT_NOT_FOUND(404, "SETTLEMENT-001", "주문 아이템 스냅샷을 찾을 수 없습니다.", false),
    ORDER_SNAPSHOT_NOT_FOUND(404, "SETTLEMENT-002", "주문 스냅샷을 찾을 수 없습니다.", false),
    PAYMENT_SNAPSHOT_NOT_FOUND(404, "SETTLEMENT-003", "결제 스냅샷을 찾을 수 없습니다.", false),

    // 2. Bad Request (400) - 입력 값 자체가 올바르지 않은 경우
    INVALID_SELLER_ID(400, "SETTLEMENT-101", "유효하지 않은 판매자 ID입니다.", false),
    INVALID_SETTLEMENT_TYPE(400, "SETTLEMENT-102", "유효하지 않은 정산 타입입니다.", false),
    INVALID_SETTLEMENT_CORE(400, "SETTLEMENT-104", "정산 금액 정보가 올바르지 않습니다.", false),
    INVALID_LIFECYCLE_META(400, "SETTLEMENT-105", "정산 라이프사이클 정보가 올바르지 않습니다.", false),
    INVALID_ORIGIN_ID(400, "SETTLEMENT-107", "정산 원천 ID가 올바르지 않습니다.", false),
    INVALID_ORDER_NUMBER(400, "SETTLEMENT-108", "주문 번호가 올바르지 않습니다.", false),
    INVALID_TIME_SEQUENCE(400, "SETTLEMENT-301", "주문, 결제, 확정 시점의 시간 순서가 올바르지 않습니다.", false),

    // 3. Conflict (409) - 비즈니스 상태가 충돌하거나 중복된 경우
    INVALID_STATUS_TRANSITION(409, "SETTLEMENT-106", "잘못된 상태 변경 요청입니다.", false),
    DUPLICATE_SETTLEMENT_ITEM(409, "SETTLEMENT-109", "이미 존재하는 정산 아이템입니다", false),
    PAYMENT_NOT_COMPLETED(409, "SETTLEMENT-201", "결제가 완료되지 않아 정산을 생성할 수 없습니다.", false),
    CONFIRMED_AT_REQUIRED(409, "SETTLEMENT-202", "구매 확정 시점은 필수입니다.", false),

    MISSING_FIELD(500, "SETTLEMENT-203", "필수 필드 [%s]이/가 누락되었습니다.", false),
    SETTLEMENT_AMOUNT_MISMATCH(500, "SETTLEMENT-203", "정산 금액이 일치하지 않습니다.", false),
    SETTLEMENT_ITEM_NOT_FOUND(404, "SETTLEMENT-204", "정산 아이템이 존재하지 않습니다.", false),
    ;

    private final int statusCode;
    private final String code;
    private final String message;
    @Getter
    private final boolean retryable;

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
