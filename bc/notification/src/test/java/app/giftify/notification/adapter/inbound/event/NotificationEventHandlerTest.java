package app.giftify.notification.adapter.inbound.event;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.notification.application.CloudEventEnvelope;
import app.giftify.notification.application.outbound.NotificationPushPort;
import app.giftify.notification.application.outbound.NotificationRepository;
import app.giftify.notification.application.support.CloudEventMapper;
import app.giftify.notification.application.support.CloudEventTypeRegistry;
import app.giftify.notification.application.support.CloudEventTypeRegistry.CloudEventMeta;
import app.giftify.notification.application.support.NotificationFactory;
import app.giftify.notification.domain.Notification;
import app.giftify.notification.domain.NotificationType;
import app.giftify.shared.domain.event.payment.PaymentCancelFailedEvent;
import app.giftify.shared.domain.event.payment.PaymentCanceledEvent;
import app.giftify.shared.domain.event.payment.PaymentEventData;
import app.giftify.shared.domain.event.payment.PaymentFailedEvent;
import app.giftify.shared.domain.event.payment.PaymentSucceededEvent;
import app.giftify.shared.domain.type.CancelType;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

@ExtendWith(MockitoExtension.class)
class NotificationEventHandlerTest {

	@Mock
	CloudEventMapper cloudEventMapper;

	@Mock
	CloudEventTypeRegistry typeRegistry;

	@Mock
	NotificationFactory notificationFactory;

	@Mock
	NotificationRepository notificationRepository;

	@Mock
	NotificationPushPort pushPort;

	@InjectMocks
	NotificationEventHandler eventHandler;

	@Nested
	@DisplayName("handlePaymentSucceeded")
	class HandlePaymentSucceeded {

		@Test
		@DisplayName("결제 성공 이벤트를 수신하면 알림을 생성하고 푸시한다")
		void createsAndPushes() {
			Long paymentId = 1L;
			Long memberId = 100L;
			PaymentSucceededEvent event = PaymentSucceededEvent.create(
				PaymentEventData.forSuccess(paymentId, 10L, memberId, "ORD-001",
					Money.of(10000), PaymentMethod.CARD, PaymentType.FUNDING, "pk", "txn"));

			CloudEventMeta meta = new CloudEventMeta(
				"app.giftify.payment.succeeded", java.net.URI.create("/giftify/payment"),
				NotificationType.PAYMENT_SUCCEEDED);
			CloudEventEnvelope ce = CloudEventEnvelope.of(
				"evt-1", "/giftify/payment", "app.giftify.payment.succeeded",
				"payment-1", OffsetDateTime.now());
			Notification notification = createNotification(memberId);
			Notification saved = createNotification(memberId);

			given(typeRegistry.resolve(PaymentSucceededEvent.class)).willReturn(meta);
			given(cloudEventMapper.fromDomainEvent(eq(event), eq("payment-" + paymentId))).willReturn(ce);
			given(notificationFactory.createSingle(memberId, NotificationType.PAYMENT_SUCCEEDED,
				String.valueOf(paymentId), "PAYMENT", ce)).willReturn(notification);
			given(notificationRepository.save(notification)).willReturn(saved);

			eventHandler.handlePaymentSucceeded(event);

			then(notificationRepository).should().save(notification);
			then(pushPort).should().send(memberId, saved);
		}
	}

	@Nested
	@DisplayName("handlePaymentFailed")
	class HandlePaymentFailed {

		@Test
		@DisplayName("결제 실패 이벤트를 수신하면 알림을 생성하고 푸시한다")
		void createsAndPushes() {
			Long paymentId = 2L;
			Long memberId = 200L;
			PaymentFailedEvent event = PaymentFailedEvent.create(
				PaymentEventData.forFailure(paymentId, 10L, memberId, "ORD-002",
					Money.of(5000), PaymentMethod.CARD, PaymentType.FUNDING));

			CloudEventMeta meta = new CloudEventMeta(
				"app.giftify.payment.failed", java.net.URI.create("/giftify/payment"),
				NotificationType.PAYMENT_FAILED);
			CloudEventEnvelope ce = CloudEventEnvelope.of(
				"evt-2", "/giftify/payment", "app.giftify.payment.failed",
				"payment-2", OffsetDateTime.now());
			Notification notification = createNotification(memberId);
			Notification saved = createNotification(memberId);

			given(typeRegistry.resolve(PaymentFailedEvent.class)).willReturn(meta);
			given(cloudEventMapper.fromDomainEvent(eq(event), eq("payment-" + paymentId))).willReturn(ce);
			given(notificationFactory.createSingle(memberId, NotificationType.PAYMENT_FAILED,
				String.valueOf(paymentId), "PAYMENT", ce)).willReturn(notification);
			given(notificationRepository.save(notification)).willReturn(saved);

			eventHandler.handlePaymentFailed(event);

			then(notificationRepository).should().save(notification);
			then(pushPort).should().send(memberId, saved);
		}
	}

	@Nested
	@DisplayName("handlePaymentCanceled")
	class HandlePaymentCanceled {

		@Test
		@DisplayName("결제 취소 이벤트를 수신하면 알림을 생성하고 푸시한다")
		void createsAndPushes() {
			Long paymentId = 3L;
			Long memberId = 300L;
			PaymentCanceledEvent event = PaymentCanceledEvent.create(
				PaymentEventData.forCancel(paymentId, 10L, memberId, "ORD-003",
					Money.of(3000), PaymentMethod.CARD, PaymentType.FUNDING,
					CancelType.CANCEL, "사용자 요청"));

			CloudEventMeta meta = new CloudEventMeta(
				"app.giftify.payment.canceled", java.net.URI.create("/giftify/payment"),
				NotificationType.PAYMENT_CANCEL_SUCCEEDED);
			CloudEventEnvelope ce = CloudEventEnvelope.of(
				"evt-3", "/giftify/payment", "app.giftify.payment.canceled",
				"payment-3", OffsetDateTime.now());
			Notification notification = createNotification(memberId);
			Notification saved = createNotification(memberId);

			given(typeRegistry.resolve(PaymentCanceledEvent.class)).willReturn(meta);
			given(cloudEventMapper.fromDomainEvent(eq(event), eq("payment-" + paymentId))).willReturn(ce);
			given(notificationFactory.createSingle(memberId, NotificationType.PAYMENT_CANCEL_SUCCEEDED,
				String.valueOf(paymentId), "PAYMENT", ce)).willReturn(notification);
			given(notificationRepository.save(notification)).willReturn(saved);

			eventHandler.handlePaymentCanceled(event);

			then(notificationRepository).should().save(notification);
			then(pushPort).should().send(memberId, saved);
		}
	}

	@Nested
	@DisplayName("handlePaymentCancelFailed")
	class HandlePaymentCancelFailed {

		@Test
		@DisplayName("결제 취소 실패 이벤트를 수신하면 알림을 생성하고 푸시한다")
		void createsAndPushes() {
			Long paymentId = 4L;
			Long memberId = 400L;
			PaymentCancelFailedEvent event = PaymentCancelFailedEvent.create(
				PaymentEventData.forCancelFailed(paymentId, 10L, memberId, "ORD-004",
					PaymentMethod.CARD, PaymentType.FUNDING, "{\"code\":\"PG_ERROR\"}"));

			CloudEventMeta meta = new CloudEventMeta(
				"app.giftify.payment.cancel-failed", java.net.URI.create("/giftify/payment"),
				NotificationType.PAYMENT_CANCEL_FAILED);
			CloudEventEnvelope ce = CloudEventEnvelope.of(
				"evt-4", "/giftify/payment", "app.giftify.payment.cancel-failed",
				"payment-4", OffsetDateTime.now());
			Notification notification = createNotification(memberId);
			Notification saved = createNotification(memberId);

			given(typeRegistry.resolve(PaymentCancelFailedEvent.class)).willReturn(meta);
			given(cloudEventMapper.fromDomainEvent(eq(event), eq("payment-" + paymentId))).willReturn(ce);
			given(notificationFactory.createSingle(memberId, NotificationType.PAYMENT_CANCEL_FAILED,
				String.valueOf(paymentId), "PAYMENT", ce)).willReturn(notification);
			given(notificationRepository.save(notification)).willReturn(saved);

			eventHandler.handlePaymentCancelFailed(event);

			then(notificationRepository).should().save(notification);
			then(pushPort).should().send(memberId, saved);
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
