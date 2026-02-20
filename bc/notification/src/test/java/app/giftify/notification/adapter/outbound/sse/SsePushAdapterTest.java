package app.giftify.notification.adapter.outbound.sse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import java.time.OffsetDateTime;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import app.giftify.notification.application.CloudEventEnvelope;
import app.giftify.notification.application.support.CloudEventMapper;
import app.giftify.notification.domain.Notification;
import app.giftify.notification.domain.NotificationType;

@ExtendWith(MockitoExtension.class)
class SsePushAdapterTest {

	@Mock
	SseEmitterRegistry registry;

	@Mock
	CloudEventMapper cloudEventMapper;

	@InjectMocks
	SsePushAdapter ssePushAdapter;

	@Nested
	@DisplayName("send")
	class Send {

		@Test
		@DisplayName("활성 emitter가 있으면 CloudEvent로 변환하여 SSE 전송한다")
		void sendsWhenEmitterExists() throws Exception {
			Long recipientId = 100L;
			Notification notification = createNotification(recipientId);
			SseEmitter emitter = mock(SseEmitter.class);

			CloudEventEnvelope envelope = CloudEventEnvelope.withData(
				"ce-1", "/giftify/notification",
				"app.giftify.notification.payment-succeeded",
				"notification-1", OffsetDateTime.now(),
				Map.of("notificationId", 1L, "title", "test")
			);

			given(registry.get(recipientId)).willReturn(emitter);
			given(cloudEventMapper.toNotificationCloudEvent(notification)).willReturn(envelope);
			given(cloudEventMapper.toJson(envelope)).willReturn("{\"id\":\"ce-1\"}");

			ssePushAdapter.send(recipientId, notification);

			then(cloudEventMapper).should().toNotificationCloudEvent(notification);
			then(cloudEventMapper).should().toJson(envelope);
			then(emitter).should().send(any(SseEmitter.SseEventBuilder.class));
		}

		@Test
		@DisplayName("활성 emitter가 없으면 조기 반환하고 CloudEventMapper를 호출하지 않는다")
		void returnsEarlyWhenNoEmitter() {
			Long recipientId = 100L;
			Notification notification = createNotification(recipientId);

			given(registry.get(recipientId)).willReturn(null);

			ssePushAdapter.send(recipientId, notification);

			then(cloudEventMapper).should(never()).toNotificationCloudEvent(any());
		}

		@Test
		@DisplayName("전송 실패 시 registry에서 emitter를 제거한다")
		void removesEmitterOnFailure() throws Exception {
			Long recipientId = 100L;
			Notification notification = createNotification(recipientId);
			SseEmitter emitter = mock(SseEmitter.class);

			CloudEventEnvelope envelope = CloudEventEnvelope.withData(
				"ce-1", "/giftify/notification",
				"app.giftify.notification.payment-succeeded",
				"notification-1", OffsetDateTime.now(),
				Map.of("notificationId", 1L)
			);

			given(registry.get(recipientId)).willReturn(emitter);
			given(cloudEventMapper.toNotificationCloudEvent(notification)).willReturn(envelope);
			given(cloudEventMapper.toJson(envelope)).willReturn("{\"id\":\"ce-1\"}");
			doThrow(new java.io.IOException("connection reset"))
				.when(emitter).send(any(SseEmitter.SseEventBuilder.class));

			ssePushAdapter.send(recipientId, notification);

			then(registry).should().remove(recipientId);
		}
	}

	private Notification createNotification(Long recipientId) {
		return new Notification(
			recipientId, NotificationType.PAYMENT_SUCCEEDED,
			"결제가 완료되었습니다", "결제가 성공적으로 처리되었습니다",
			"1", "PAYMENT",
			"evt-001", "app.giftify.payment.succeeded", "/giftify/payment"
		);
	}
}
