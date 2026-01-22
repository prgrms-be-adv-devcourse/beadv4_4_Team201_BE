package app.giftify.payment.adapter.in.web.exception;

import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import app.giftify.shared.api.response.ErrorResponse;
import domain.payment.PaymentErrorCode;
import domain.payment.PaymentException;

@RestControllerAdvice(basePackages = "app.giftify.payment.adapter.in.web")
public class MoneyGlobalExceptionHandler {

	@ExceptionHandler(PaymentException.class)
	public ResponseEntity<ErrorResponse> handlePaymentException(PaymentException e) {
		var errorCode = e.getErrorCode();
		// TODO :: ErrorCode 인터페이스에 getStatus() 추가 후, 동적으로 상태 코드 결정하도록 수정이 필요합니다.
		// ErrorCode 인터페이스에는 int 를 반환하고,
		// 여기 ExceptionHandler 에서 ResponseEntity.status(int) 로 설정해서 나가게 하면 됩니다.
		// 현재는 모든 PaymentException을 400(Bad Request)으로 처리 중
		return ResponseEntity
			.badRequest()
			.body(new ErrorResponse(errorCode.getCode(), e.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
		String errorMessage = e.getBindingResult().getFieldErrors().stream()
			.map(error -> error.getField() + ": " + error.getDefaultMessage())
			.collect(Collectors.joining(", "));

		return ResponseEntity
			.badRequest()
			.body(new ErrorResponse(PaymentErrorCode.INVALID_INPUT_VALUE.getCode(), errorMessage));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
		return ResponseEntity.badRequest().body(e.getMessage());
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<String> handleIllegalState(IllegalStateException e) {
		return ResponseEntity.badRequest().body(e.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> handleException(Exception e) {
		return ResponseEntity.internalServerError().body("일시적인 서버 오류 입니다. 잠시후 재시도해 주세요: " + e.getMessage());
	}
}
