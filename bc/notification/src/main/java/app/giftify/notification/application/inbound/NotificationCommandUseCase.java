package app.giftify.notification.application.inbound;

public interface NotificationCommandUseCase {
	void markAsRead(Long notificationId, Long memberId);

	void markAllAsRead(Long memberId);
}
