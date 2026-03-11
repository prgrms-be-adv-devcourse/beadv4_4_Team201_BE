package app.giftify.product.domain.exception;

import app.giftify.shared.api.exception.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum ProductErrorCode implements ErrorCode {

    // P1xx: 조회/리소스 관련
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "P101", "상품을 찾을 수 없습니다."),
    SELLER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "P102", "판매자를 찾을 수 없습니다."),
    PRODUCTS_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "P103", "일부 상품을 찾을 수 없습니다."),
    PRODUCT_NOT_OWNED(HttpStatus.FORBIDDEN.value(), "P104", "본인의 상품이 아닙니다."),

    // P2xx: 상태/비즈니스 규칙/전이 관련
    PRODUCT_OUT_OF_STOCK(HttpStatus.BAD_REQUEST.value(), "P201", "상품 재고가 부족합니다."),
    PRODUCT_NOT_APPROVED_YET(HttpStatus.BAD_REQUEST.value(), "P202", "관리자의 판매 승인을 먼저 받아야 합니다."),
    PRODUCT_CANNOT_CHANGE_STATUS_TO_DRAFT(HttpStatus.BAD_REQUEST.value(), "P203", "'DRAFT'로 상태를 되돌릴 수 없습니다."),
    PRODUCT_CANNOT_CHANGE_TO_SAME_STATUS(HttpStatus.BAD_REQUEST.value(), "P204", "동일 상태로 변경할 수 없습니다."),
    PRODUCT_NOT_IN_DRAFT_STATUS(HttpStatus.BAD_REQUEST.value(), "P205", "'DRAFT' 상태가 아닙니다."),
    PRODUCT_REJECTED_CANNOT_BE_ACTIVATED(HttpStatus.BAD_REQUEST.value(), "P206", "관리자가 판매 승인을 거절한 상품은 판매 대기 상태로 변경할 수 없습니다."),
    PRODUCT_NOT_ACTIVE(HttpStatus.BAD_REQUEST.value(), "P207", "판매 중인 상품이 아닙니다."),
    PRODUCT_NOT_IN_INACTIVE_STATUS(HttpStatus.BAD_REQUEST.value(), "P208", "'INACTIVE' 상태의 상품이 아닙니다."),
    INVALID_APPROVAL_ACTION(HttpStatus.BAD_REQUEST.value(), "P209", "올바르지 않은 액션값입니다."),
    CANNOT_STOP_SALE_DUE_TO_ACTIVE_FUNDING(HttpStatus.BAD_REQUEST.value(), "P210", "진행 중이거나 수령자 수락 대기 중인 펀딩이 있습니다."),
    PRODUCT_CANNOT_CHANGE_STATUS_TO_REJECTED(HttpStatus.BAD_REQUEST.value(), "P211", "'REJECTED'로 상태를 변경할 수 없습니다."),
    PRODUCT_ALREADY_DELETED(HttpStatus.BAD_REQUEST.value(), "P212", "삭제된 상품입니다."),

    // P3xx: 도메인 생성/입력 검증
    PRODUCT_SELLER_REQUIRED(HttpStatus.BAD_REQUEST.value(), "P301", "판매자 정보는 필수입니다."),
    INVALID_PRODUCT_NAME(HttpStatus.BAD_REQUEST.value(), "P302", "상품명이 올바르지 않습니다."),
    INVALID_PRODUCT_DESCRIPTION(HttpStatus.BAD_REQUEST.value(), "P303", "상품 설명이 올바르지 않습니다."),
    INVALID_PRODUCT_PRICE(HttpStatus.BAD_REQUEST.value(), "P304", "상품 가격이 올바르지 않습니다."),
    INVALID_PRODUCT_SEARCH_PRICE_RANGE(HttpStatus.BAD_REQUEST.value(), "P305", "가격 범위가 올바르지 않습니다."),
    INVALID_PRODUCT_STOCK(HttpStatus.BAD_REQUEST.value(), "P306", "재고량이 올바르지 않습니다."),
    PRODUCT_ID_REQUIRED(HttpStatus.BAD_REQUEST.value(), "P307", "Product ID는 필수입니다."),

    // P4xx: 상품 수정/요청 검증
    PRODUCT_UPDATE_EMPTY_REQUEST(HttpStatus.NO_CONTENT.value(), "P401", "수정할 내용이 없습니다."),
    INVALID_PRODUCT_STATUS(HttpStatus.BAD_REQUEST.value(), "P402", "허용되지 않은 상태값입니다."),
    EXPECTED_STOCK_REQUIRED(HttpStatus.BAD_REQUEST.value(), "P403", "재고 수정 시 기존 재고값(expectedStock)은 필수입니다."),

    // P5xx: 인프라/동시성 관련
    PRODUCT_STOCK_LOCK_TIMEOUT(HttpStatus.CONFLICT.value(), "P501", "일시적인 문제가 발생했습니다. 다시 시도하세요."),
    PRODUCT_STOCK_CHANGED(HttpStatus.CONFLICT.value(), "P502", "재고가 변경되었습니다. 다시 시도하세요."),
    EXTERNAL_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "P503", "외부 API 호출 중 오류가 발생했습니다."),
    EXTERNAL_API_CONNECTION_FAILURE(HttpStatus.SERVICE_UNAVAILABLE.value(), "P504", "외부 서비스에 연결할 수 없습니다."),
    ES_NICKNAME_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "P505", "ES 닉네임 업데이트에 실패했습니다.");

    private final int statusCode;
    private final String code;
    private final String message;

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
