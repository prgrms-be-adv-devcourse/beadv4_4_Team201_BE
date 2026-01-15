package app.giftify.in.product.web;

import app.giftify.shared.api.exception.BusinessException;

public class ProductException extends BusinessException {
	public ProductException(ProductErrorCode errorCode) {
		super(errorCode);
	}
}
