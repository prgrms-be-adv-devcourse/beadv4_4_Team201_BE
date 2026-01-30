package app.giftify.settlement.domain.errorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SettlementErrorCode implements ErrorCode {

    ORDER_ITEM_SNAPSHOT_NOT_FOUND("SETTLEMENT-001", "주문 아이템 스냅샷을 찾을 수 없습니다.", false),
    ORDER_SNAPSHOT_NOT_FOUND("SETTLEMENT-002", "주문 스냅샷을 찾을 수 없습니다.", false),
    PAYMENT_SNAPSHOT_NOT_FOUND("SETTLEMENT-003", "결제 스냅샷을 찾을 수 없습니다.", false),

    INVALID_SELLER_ID("SETTLEMENT-101", "유효하지 않은 판매자 ID입니다.", false),
    INVALID_SETTLEMENT_TYPE("SETTLEMENT-102", "유효하지 않은 정산 타입입니다.", false),
    INVALID_SETTLEMENT_CORE("SETTLEMENT-104", "정산 금액 정보가 올바르지 않습니다.", false),
    INVALID_LIFECYCLE_META("SETTLEMENT-105", "정산 라이프사이클 정보가 올바르지 않습니다.", false),
    INVALID_STATUS_TRANSITION("SETTLEMENT-106", "허용되지 않는 정산 상태 변경입니다.", false),
    INVALID_ORIGIN_ID("SETTLEMENT-107", "정산 원천 ID가 올바르지 않습니다.", false),
    INVALID_ORDER_NUMBER("SETTLEMENT-108", "주문 번호가 올바르지 않습니다.", false),
    DUPLICATE_SETTLEMENT_ITEM("SETTLEMENT-109", "이미 존재하는 정산 아이템입니다", false),

    PAYMENT_NOT_COMPLETED("SETTLEMENT-201", "결제가 완료되지 않아 정산을 생성할 수 없습니다.", false),
    CONFIRMED_AT_REQUIRED("SETTLEMENT-202", "구매 확정 시점은 필수입니다.", false),

    INVALID_TIME_SEQUENCE("SETTLEMENT-301", "주문, 결제, 확정 시점의 시간 순서가 올바르지 않습니다.", false),
    ;

    private final String code;
    private final String message;
    private final boolean retryable;
}
