package app.giftify.product.domain.exception;

import app.giftify.support.common.api.exception.DomainException;

public class ProductException extends DomainException {
    // todo 에러 종류별로 코드 분리
    public ProductException(ProductErrorCode errorCode) {
        super(errorCode);
    }

    public ProductException(ProductErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
