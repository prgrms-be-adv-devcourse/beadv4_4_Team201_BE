package app.giftify.notification.application.outbound;

import app.giftify.notification.domain.Notification;

public interface NotificationPushPort {
	void send(Long recipientId, Notification notification);
}
