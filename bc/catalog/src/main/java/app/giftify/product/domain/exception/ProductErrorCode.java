package app.giftify.product.domain.exception;

import app.giftify.shared.api.exception.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@AllArgsConstructor
public enum ProductErrorCode implements ErrorCode {

    // P1xx: 조회/리소스 관련
    PRODUCT_NOT_FOUND(NOT_FOUND, "P101", "상품을 찾을 수 없습니다."),
    SELLER_NOT_FOUND(NOT_FOUND, "P102", "판매자를 찾을 수 없습니다."),
    PRODUCTS_NOT_FOUND(NOT_FOUND, "P103", "일부 상품을 찾을 수 없습니다."),

    // P2xx: 상태/비즈니스 규칙/전이 관련
    PRODUCT_OUT_OF_STOCK(BAD_REQUEST, "P201", "상품 재고가 부족합니다."),
    PRODUCT_NOT_APPROVED_YET(BAD_REQUEST, "P202", "관리자의 판매 승인을 먼저 받아야 합니다."),
    PRODUCT_CANNOT_CHANGE_STATUS_TO_DRAFT(BAD_REQUEST, "P203", "'DRAFT'로 상태를 되돌릴 수 없습니다."),
    PRODUCT_CANNOT_CHANGE_TO_SAME_STATUS(BAD_REQUEST, "P204", "동일 상태로 변경할 수 없습니다."),
    PRODUCT_NOT_IN_DRAFT_STATUS(BAD_REQUEST, "P205", "'DRAFT' 상태가 아닙니다."),
    PRODUCT_REJECTED_CANNOT_BE_ACTIVATED(BAD_REQUEST, "P206", "관리자가 판매 승인을 거절한 상품은 판매 대기 상태로 변경할 수 없습니다."),
    PRODUCT_NOT_ACTIVE(BAD_REQUEST, "P207", "판매 중인 상품이 아닙니다."),
    PRODUCT_NOT_IN_INACTIVE_STATUS(BAD_REQUEST, "P208", "'INACTIVE' 상태의 상품이 아닙니다."),
    INVALID_APPROVAL_ACTION(BAD_REQUEST, "P209", "올바르지 않은 액션값입니다."),

    // P3xx: 도메인 생성/입력 검증
    PRODUCT_SELLER_REQUIRED(BAD_REQUEST, "P301", "판매자 정보는 필수입니다."),
    INVALID_PRODUCT_NAME(BAD_REQUEST, "P302", "상품명이 올바르지 않습니다."),
    INVALID_PRODUCT_DESCRIPTION(BAD_REQUEST, "P303", "상품 설명이 올바르지 않습니다."),
    INVALID_PRODUCT_PRICE(BAD_REQUEST, "P304", "상품 가격이 올바르지 않습니다."),
    INVALID_PRODUCT_SEARCH_PRICE_RANGE(BAD_REQUEST, "P305", "가격 범위가 올바르지 않습니다."),
    INVALID_PRODUCT_STOCK(BAD_REQUEST, "P306", "재고량이 올바르지 않습니다."),
    PRODUCT_ID_REQUIRED(BAD_REQUEST, "P307", "Product ID는 필수입니다."),

    // P4xx: 상품 수정/요청 검증
    PRODUCT_UPDATE_EMPTY_REQUEST(NO_CONTENT, "P401", "수정할 내용이 없습니다."),
    INVALID_PRODUCT_STATUS(BAD_REQUEST, "P402", "허용되지 않은 상태값입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    public HttpStatus getStatus() {
        return status;
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
