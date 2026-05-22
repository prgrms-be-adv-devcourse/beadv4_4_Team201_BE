package app.giftify.notification.domain;

import app.giftify.shared.api.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum NotificationErrorCode implements ErrorCode {
	NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "NOTIF_101", "알림을 찾을 수 없습니다."),
	NOT_NOTIFICATION_RECIPIENT(HttpStatus.FORBIDDEN.value(), "NOTIF_102", "이 알림의 수신자가 아닙니다.");

	private final int statusCode;
	private final String code;
	private final String message;

	NotificationErrorCode(int statusCode, String code, String message) {
		this.statusCode = statusCode;
		this.code = code;
		this.message = message;
	}

	@Override
	public int getStatusCode() { return statusCode; }
	@Override
	public String getCode() { return code; }
	@Override
	public String getMessage() { return message; }

	@Override
	public String formatMessage(Object... args) {
		return String.format(this.message, args);
	}
}
