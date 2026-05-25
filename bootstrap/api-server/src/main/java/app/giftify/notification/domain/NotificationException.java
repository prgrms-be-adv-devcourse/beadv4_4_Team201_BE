package app.giftify.notification.domain;

import app.giftify.support.common.api.exception.DomainException;

public class NotificationException extends DomainException {
	public NotificationException(NotificationErrorCode errorCode) {
		super(errorCode);
	}
}
