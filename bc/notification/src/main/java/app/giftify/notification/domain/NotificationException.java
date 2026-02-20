package app.giftify.notification.domain;

import app.giftify.shared.api.exception.DomainException;

public class NotificationException extends DomainException {
	public NotificationException(NotificationErrorCode errorCode) {
		super(errorCode);
	}
}
