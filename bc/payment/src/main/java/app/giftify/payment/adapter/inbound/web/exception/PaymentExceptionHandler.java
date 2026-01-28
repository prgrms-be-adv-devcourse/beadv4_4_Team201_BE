package app.giftify.payment.adapter.inbound.web.exception;

import app.giftify.payment.domain.PaymentException;
import app.giftify.wallet.domain.WalletException;
import app.giftify.shared.api.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = {"app.giftify.payment", "app.giftify.wallet"})
public class PaymentExceptionHandler {

	@ExceptionHandler(PaymentException.class)
	public ResponseEntity<ErrorResponse> handlePaymentException(PaymentException e) {
		return ResponseEntity
			.badRequest()
			.body(new ErrorResponse(e.getErrorCode().getCode(), e.getMessage()));
	}

	@ExceptionHandler(WalletException.class)
	public ResponseEntity<ErrorResponse> handleWalletException(WalletException e) {
		return ResponseEntity
			.badRequest()
			.body(new ErrorResponse(e.getErrorCode().getCode(), e.getMessage()));
	}
}
