package app.giftify.notification.application.inbound;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import app.giftify.notification.domain.Notification;

public interface NotificationQueryUseCase {
	Page<Notification> getNotifications(Long memberId, Pageable pageable);

	Page<Notification> getUnreadNotifications(Long memberId, Pageable pageable);

	long getUnreadCount(Long memberId);
}
