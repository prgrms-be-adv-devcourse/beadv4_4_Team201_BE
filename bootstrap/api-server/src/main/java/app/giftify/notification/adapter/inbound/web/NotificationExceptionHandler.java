package app.giftify.notification.adapter.inbound.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import app.giftify.notification.domain.NotificationException;
import app.giftify.shared.api.response.ErrorResponse;

@RestControllerAdvice(basePackages = "app.giftify.notification")
public class NotificationExceptionHandler {

	@ExceptionHandler(NotificationException.class)
	public ResponseEntity<ErrorResponse> handleNotificationException(NotificationException e) {
		return ResponseEntity
			.status(e.getErrorCode().getStatusCode())
			.body(new ErrorResponse(e.getErrorCode().getCode(), e.getMessage()));
	}
}
