package app.giftify.notification.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.notification.application.inbound.NotificationCommandUseCase;
import app.giftify.notification.application.inbound.NotificationQueryUseCase;
import app.giftify.notification.application.outbound.NotificationRepository;
import app.giftify.notification.domain.Notification;
import app.giftify.notification.domain.NotificationErrorCode;
import app.giftify.notification.domain.NotificationException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService implements NotificationQueryUseCase, NotificationCommandUseCase {

	private final NotificationRepository notificationRepository;

	@Override
	public Page<Notification> getNotifications(Long memberId, Pageable pageable) {
		return notificationRepository.findByRecipientId(memberId, pageable);
	}

	@Override
	public Page<Notification> getUnreadNotifications(Long memberId, Pageable pageable) {
		return notificationRepository.findByRecipientIdAndIsReadFalse(memberId, pageable);
	}

	@Override
	public long getUnreadCount(Long memberId) {
		return notificationRepository.countByRecipientIdAndIsReadFalse(memberId);
	}

	@Override
	@Transactional
	public void markAsRead(Long notificationId, Long memberId) {
		Notification notification = notificationRepository.findById(notificationId)
			.orElseThrow(() -> new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));
		if (!notification.isOwnedBy(memberId)) {
			throw new NotificationException(NotificationErrorCode.NOT_NOTIFICATION_RECIPIENT);
		}
		notification.markAsRead();
		notificationRepository.save(notification);
	}

	@Override
	@Transactional
	public void markAllAsRead(Long memberId) {
		notificationRepository.markAllAsReadByRecipientId(memberId);
	}
}
