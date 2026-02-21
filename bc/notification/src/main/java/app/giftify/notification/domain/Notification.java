package app.giftify.notification.domain;

import app.giftify.support.jpa.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseJpaEntity {

	@Column(name = "recipient_id", nullable = false)
	private Long recipientId;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 50)
	private NotificationType type;

	@Column(name = "title", nullable = false, length = 200)
	private String title;

	@Column(name = "content", nullable = false, length = 500)
	private String content;

	@Column(name = "is_read", nullable = false)
	private boolean isRead;

	@Column(name = "read_at")
	private LocalDateTime readAt;

	@Column(name = "reference_id", length = 50)
	private String referenceId;

	@Column(name = "reference_type", length = 50)
	private String referenceType;

	@Column(name = "source_event_id", nullable = false, length = 100)
	private String sourceEventId;

	@Column(name = "source_event_type", nullable = false, length = 100)
	private String sourceEventType;

	@Column(name = "source_event_source", nullable = false, length = 100)
	private String sourceEventSource;

	public Notification(
		Long recipientId, NotificationType type,
		String title, String content,
		String referenceId, String referenceType,
		String sourceEventId, String sourceEventType, String sourceEventSource
	) {
		this.recipientId = recipientId;
		this.type = type;
		this.title = title;
		this.content = content;
		this.referenceId = referenceId;
		this.referenceType = referenceType;
		this.sourceEventId = sourceEventId;
		this.sourceEventType = sourceEventType;
		this.sourceEventSource = sourceEventSource;
	}

	public void markAsRead() {
		if (this.isRead) return;
		this.isRead = true;
		this.readAt = LocalDateTime.now();
	}

	public boolean isOwnedBy(Long memberId) {
		return this.recipientId.equals(memberId);
	}
}
