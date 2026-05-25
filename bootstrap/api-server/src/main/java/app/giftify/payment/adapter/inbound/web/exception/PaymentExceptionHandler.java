package app.giftify.payment.adapter.inbound.web.exception;

import app.giftify.payment.domain.PaymentException;
import app.giftify.support.common.api.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = {"app.giftify.payment"})
public class PaymentExceptionHandler {

	@ExceptionHandler(PaymentException.class)
	public ResponseEntity<ErrorResponse> handlePaymentException(PaymentException e) {
		return ResponseEntity
				.status(e.getErrorCode().getStatusCode())
				.body(new ErrorResponse(e.getErrorCode().getCode(), e.getMessage()));
	}
}
