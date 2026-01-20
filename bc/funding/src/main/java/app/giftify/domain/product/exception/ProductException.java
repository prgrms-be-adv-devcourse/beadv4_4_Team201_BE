package app.giftify.domain.product.exception;

import app.giftify.shared.api.exception.DomainException;

public class ProductException extends DomainException {
	public ProductException(ProductErrorCode errorCode) {
		super(errorCode);
	}
}
