package app.giftify.orderDemo.domain.errorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OrderErrorCode implements ErrorCode {

    INVALID_TARGET_ID("ORDER-100", "유효하지 않은 상품 아이디입니다.", false),
    INVALID_SELLER_ID("ORDER-101", "유효하지 않은 판매자 ID입니다.", false),
    AMOUNT_EXCEEDS_PRICE("ORDER-102", "주문 금액이 상품 금액을 초과했습니다.", false),
    ORDER_NOT_FOUND("ORDER-103", "주문을 찾을 수 없습니다.", false),
    ORDER_ITEM_NOT_FOUND("ORDER-104", "주문 아이템을 찾을 수 없습니다.", false),
    INVALID_TARGET_TYPE("ORDER-105", "유효하지 않은 타겟 타입입니다.", false),

    INVALID_RECEIVER_ID("ORDER-106", "유효하지 않은 수령자 ID입니다.", false),
    INVALID_PRICE("ORDER-107", "유효하지 않은 상품 가격입니다.", false),

    INVALID_AMOUNT("ORDER-108", "유효하지 않은 주문 가격입니다.", false),
    INVALID_BUYER_ID("ORDER-109", "유효하지 않은 구매자 ID입니다.", false),
    INVALID_ORDER_ITEM("ORDER-110", "유효하지 않은 주문 아이템입니다.", false),
    INVALID_PAYMENT_METHOD("ORDER-111", "유효하지 않은 결제 수단입니다.", false),

    INVALID_TOTAL_AMOUNT("ORDER_112", "총 금액은 1000원 이상이어야 합니다.", false);

    private final String code;
    private final String message;
    private final boolean retryable;
}
