package app.giftify.shared.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 도메인 이벤트의 공통 속성 정의
 */
public abstract class BaseDomainEvent {
    private final String eventId;
    private final LocalDateTime occurredAt;
    // TODO :: sourceType 을 여기다 포함하는게 맞지 않을까?

    protected BaseDomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
    }

	/**
     * 이벤트의 발생 시간을 외부 주입해주기 위한 생성자
	 */
    protected BaseDomainEvent(LocalDateTime occurredAt){
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = occurredAt;
    }

    public String getEventId() {
        return eventId;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
