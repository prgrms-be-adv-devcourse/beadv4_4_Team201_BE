package app.giftify.domain.product.exception;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.HttpStatus;

import app.giftify.shared.api.exception.ErrorCode;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ProductErrorCode implements ErrorCode {

	PRODUCT_NOT_FOUND(NOT_FOUND, "P001", "상품을 찾을 수 없습니다."),
	PRODUCT_OUT_OF_STOCK(BAD_REQUEST, "P002", "상품 재고가 부족합니다."),
	PRODUCT_NOT_APPROVED_YET(BAD_REQUEST, "P003", "관리자의 판매 승인을 먼저 받아야 합니다."),
	PRODUCT_CANNOT_CHANGE_STATUS_TO_DRAFT(BAD_REQUEST, "P004", "'DRAFT'로 상태를 되돌릴 수 없습니다."),
	PRODUCT_CANNOT_CHANGE_TO_SAME_STATUS(BAD_REQUEST, "P005", "동일 상태로 변경할 수 없습니다."),
	PRODUCT_NOT_IN_DRAFT_STATUS(BAD_REQUEST, "P006", "'DRAFT' 상태가 아닙니다."),
	PRODUCT_REJECTED_CANNOT_BE_ACTIVATED(BAD_REQUEST, "P007", "관리자가 판매 승인을 거절한 상품은 판매 대기 상태로 변경할 수 없습니다."),
	PRODUCT_NOT_ACTIVE(BAD_REQUEST, "P008", "판매 중인 상품이 아닙니다."),
	PRODUCT_NOT_IN_INACTIVE_STATUS(BAD_REQUEST, "P009", "'INACTIVE' 상태의 상품이 아닙니다."),

	// 도메인 생성/입력 검증 에러코드
	PRODUCT_SELLER_REQUIRED(BAD_REQUEST, "P010", "판매자 정보는 필수입니다."),
	INVALID_PRODUCT_NAME(BAD_REQUEST, "P011", "상품명이 올바르지 않습니다."),
	INVALID_PRODUCT_DESCRIPTION(BAD_REQUEST, "P012", "상품 설명이 올바르지 않습니다."),
	INVALID_PRODUCT_PRICE(BAD_REQUEST, "P013", "상품 가격이 올바르지 않습니다."),
	INVALID_PRODUCT_SEARCH_PRICE_RANGE(BAD_REQUEST, "P014", "가격 범위가 올바르지 않습니다.");

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
