package app.giftify.notification.domain;

import app.giftify.support.jpa.BaseJpaEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
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

	protected Notification() {
	}

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

	public Long getRecipientId() {
		return recipientId;
	}

	public NotificationType getType() {
		return type;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}

	public boolean isRead() {
		return isRead;
	}

	public LocalDateTime getReadAt() {
		return readAt;
	}

	public String getReferenceId() {
		return referenceId;
	}

	public String getReferenceType() {
		return referenceType;
	}

	public String getSourceEventId() {
		return sourceEventId;
	}

	public String getSourceEventType() {
		return sourceEventType;
	}

	public String getSourceEventSource() {
		return sourceEventSource;
	}
}
