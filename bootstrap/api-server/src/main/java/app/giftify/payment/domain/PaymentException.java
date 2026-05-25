package app.giftify.payment.domain;

import app.giftify.support.common.api.exception.DomainException;

public class PaymentException extends DomainException {

	public PaymentException(PaymentErrorCode errorCode) {
		super(errorCode);
	}

	public PaymentException(PaymentErrorCode errorCode, String message) {
		super(errorCode, message);
	}

	public PaymentException(PaymentErrorCode errorCode, String message, Throwable cause) {
		super(errorCode, message, cause);
	}
}
