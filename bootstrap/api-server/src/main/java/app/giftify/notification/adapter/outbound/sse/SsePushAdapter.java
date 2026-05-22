package app.giftify.notification.adapter.outbound.sse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import app.giftify.notification.application.CloudEventEnvelope;
import app.giftify.notification.application.outbound.NotificationPushPort;
import app.giftify.notification.application.support.CloudEventMapper;
import app.giftify.notification.domain.Notification;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SsePushAdapter implements NotificationPushPort {

	private static final Logger log = LoggerFactory.getLogger(SsePushAdapter.class);

	private final SseEmitterRegistry registry;
	private final CloudEventMapper cloudEventMapper;

	@Override
	public void send(Long recipientId, Notification notification) {
		SseEmitter emitter = registry.get(recipientId);
		if (emitter == null) {
			log.debug("No active SSE connection for memberId={}", recipientId);
			return;
		}

		try {
			CloudEventEnvelope envelope = cloudEventMapper.toNotificationCloudEvent(notification);
			String json = cloudEventMapper.toJson(envelope);

			emitter.send(SseEmitter.event()
				.id(envelope.id())
				.name(envelope.type())
				.data(json));

			log.debug("SSE event sent: memberId={}, type={}", recipientId, envelope.type());
		} catch (Exception e) {
			log.warn("Failed to send SSE event: memberId={}, error={}", recipientId, e.getMessage());
			registry.remove(recipientId);
		}
	}
}
