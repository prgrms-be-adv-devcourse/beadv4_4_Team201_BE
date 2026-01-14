package app.giftify.shared.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 도메인 이벤트의 공통 속성 정의
 */
public abstract class BaseDomainEvent {
    private final String eventId;
    private final LocalDateTime occurredAt;

    protected BaseDomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
    }

    public String getEventId() {
        return eventId;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}