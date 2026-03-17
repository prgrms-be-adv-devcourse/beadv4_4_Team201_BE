package app.giftify.notification.application.support;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import tools.jackson.core.JsonProcessingException;
import tools.jackson.databind.ObjectMapper;

import app.giftify.notification.application.CloudEventEnvelope;
import app.giftify.notification.domain.Notification;
import app.giftify.shared.domain.event.BaseDomainEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CloudEventMapper {

	private final CloudEventTypeRegistry registry;
	private final ObjectMapper objectMapper;

	public CloudEventEnvelope fromDomainEvent(Object event, String subject) {
		var meta = registry.resolve(event.getClass());
		String eventId;
		OffsetDateTime time;

		if (event instanceof BaseDomainEvent bde) {
			eventId = bde.getEventId();
			time = bde.getOccurredAt().atOffset(ZoneOffset.UTC);
		} else {
			eventId = UUID.randomUUID().toString();
			time = OffsetDateTime.now(ZoneOffset.UTC);
		}

		return CloudEventEnvelope.of(
			eventId, meta.ceSource().toString(), meta.ceType(), subject, time
		);
	}

	public CloudEventEnvelope toNotificationCloudEvent(Notification notification) {
		String ceType = CloudEventTypeRegistry.toNotificationCeType(notification.getType());

		Map<String, Object> data = new LinkedHashMap<>();
		data.put("notificationId", notification.getId());
		data.put("title", notification.getTitle());
		data.put("content", notification.getContent());
		data.put("isRead", notification.isRead());
		data.put("referenceId", notification.getReferenceId());
		data.put("referenceType", notification.getReferenceType());

		OffsetDateTime time = notification.getCreatedAt() != null
			? notification.getCreatedAt().atOffset(ZoneOffset.UTC)
			: OffsetDateTime.now(ZoneOffset.UTC);

		return CloudEventEnvelope.withData(
			UUID.randomUUID().toString(),
			CloudEventTypeRegistry.SOURCE_NOTIFICATION.toString(),
			ceType,
			"notification-" + notification.getId(),
			time,
			data
		);
	}

	public String toJson(CloudEventEnvelope envelope) {
		try {
			return objectMapper.writeValueAsString(envelope);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to serialize CloudEvent", e);
		}
	}
}
