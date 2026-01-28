package app.giftify.funding.domain.product.exception;

import app.giftify.shared.api.exception.DomainException;

public class ProductException extends DomainException {
	// todo 에러 종류별로 코드 분리
	public ProductException(ProductErrorCode errorCode) {
		super(errorCode);
	}
}
