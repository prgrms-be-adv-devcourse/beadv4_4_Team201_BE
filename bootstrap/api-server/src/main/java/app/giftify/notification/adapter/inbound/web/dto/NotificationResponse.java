package app.giftify.notification.adapter.inbound.web.dto;

import java.time.LocalDateTime;

import app.giftify.notification.domain.Notification;

public record NotificationResponse(
	Long id,
	String type,
	String title,
	String content,
	boolean isRead,
	LocalDateTime readAt,
	String referenceId,
	String referenceType,
	LocalDateTime createdAt
) {
	public static NotificationResponse from(Notification n) {
		return new NotificationResponse(
			n.getId(), n.getType().name(),
			n.getTitle(), n.getContent(),
			n.isRead(), n.getReadAt(),
			n.getReferenceId(), n.getReferenceType(),
			n.getCreatedAt()
		);
	}
}
