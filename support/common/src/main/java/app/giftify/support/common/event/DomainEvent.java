package app.giftify.support.common.event;

import java.time.LocalDateTime;

public interface DomainEvent extends org.jmolecules.event.types.DomainEvent {
    String eventId();

    LocalDateTime occurredAt();
}

