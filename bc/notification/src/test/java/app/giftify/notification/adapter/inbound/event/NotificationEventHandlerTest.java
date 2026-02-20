package app.giftify.notification.adapter.inbound.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
import app.giftify.shared.domain.event.payment.PaymentCanceledEvent;
import app.giftify.shared.domain.event.payment.PaymentFailedEvent;
import app.giftify.shared.domain.event.payment.PaymentRefundedEvent;
import app.giftify.shared.domain.event.payment.PaymentSucceededEvent;
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

	@Captor
	ArgumentCaptor<Long> recipientIdCaptor;

	@Captor
	ArgumentCaptor<Notification> notificationCaptor;

	@Nested
	@DisplayName("handlePaymentSucceeded")
	class HandlePaymentSucceeded {

		@Test
		@DisplayName("결제 성공 이벤트를 수신하면 알림을 생성하고 푸시한다")
		void createsAndPushes() {
			Long paymentId = 1L;
			Long userId = 100L;
			PaymentSucceededEvent event = new PaymentSucceededEvent(
				paymentId, "FUNDING", userId, Money.of(10000), PaymentType.FUNDING, LocalDateTime.now()
			);

			CloudEventMeta meta = new CloudEventMeta(
				"app.giftify.payment.succeeded", java.net.URI.create("/giftify/payment"),
				NotificationType.PAYMENT_SUCCEEDED
			);
			CloudEventEnvelope ce = CloudEventEnvelope.of(
				"evt-1", "/giftify/payment", "app.giftify.payment.succeeded",
				"payment-1", OffsetDateTime.now()
			);
			Notification notification = createNotification(userId);
			Notification saved = createNotification(userId);

			given(typeRegistry.resolve(PaymentSucceededEvent.class)).willReturn(meta);
			given(cloudEventMapper.fromDomainEvent(eq(event), eq("payment-" + paymentId))).willReturn(ce);
			given(notificationFactory.createSingle(userId, NotificationType.PAYMENT_SUCCEEDED,
				String.valueOf(paymentId), "PAYMENT", ce)).willReturn(notification);
			given(notificationRepository.save(notification)).willReturn(saved);

			eventHandler.handlePaymentSucceeded(event);

			then(notificationRepository).should().save(notification);
			then(pushPort).should().send(userId, saved);
		}
	}

	@Nested
	@DisplayName("handlePaymentFailed")
	class HandlePaymentFailed {

		@Test
		@DisplayName("결제 실패 이벤트를 수신하면 알림을 생성하고 푸시한다")
		void createsAndPushes() {
			Long paymentId = 2L;
			Long userId = 200L;
			PaymentFailedEvent event = new PaymentFailedEvent(
				paymentId, "FUNDING", userId, Money.of(5000), PaymentType.FUNDING,
				"잔액 부족", LocalDateTime.now()
			);

			CloudEventMeta meta = new CloudEventMeta(
				"app.giftify.payment.failed", java.net.URI.create("/giftify/payment"),
				NotificationType.PAYMENT_FAILED
			);
			CloudEventEnvelope ce = CloudEventEnvelope.of(
				"evt-2", "/giftify/payment", "app.giftify.payment.failed",
				"payment-2", OffsetDateTime.now()
			);
			Notification notification = createNotification(userId);
			Notification saved = createNotification(userId);

			given(typeRegistry.resolve(PaymentFailedEvent.class)).willReturn(meta);
			given(cloudEventMapper.fromDomainEvent(eq(event), eq("payment-" + paymentId))).willReturn(ce);
			given(notificationFactory.createSingle(userId, NotificationType.PAYMENT_FAILED,
				String.valueOf(paymentId), "PAYMENT", ce)).willReturn(notification);
			given(notificationRepository.save(notification)).willReturn(saved);

			eventHandler.handlePaymentFailed(event);

			then(notificationRepository).should().save(notification);
			then(pushPort).should().send(userId, saved);
		}
	}

	@Nested
	@DisplayName("handlePaymentCanceled")
	class HandlePaymentCanceled {

		@Test
		@DisplayName("결제 취소 이벤트를 수신하면 알림을 생성하고 푸시한다")
		void createsAndPushes() {
			Long paymentId = 3L;
			Long userId = 300L;
			PaymentCanceledEvent event = new PaymentCanceledEvent(
				paymentId, "FUNDING", userId, Money.of(3000),
				"사용자 요청", PaymentType.FUNDING, LocalDateTime.now(), 1L
			);

			CloudEventMeta meta = new CloudEventMeta(
				"app.giftify.payment.canceled", java.net.URI.create("/giftify/payment"),
				NotificationType.PAYMENT_CANCEL_SUCCEEDED
			);
			CloudEventEnvelope ce = CloudEventEnvelope.of(
				"evt-3", "/giftify/payment", "app.giftify.payment.canceled",
				"payment-3", OffsetDateTime.now()
			);
			Notification notification = createNotification(userId);
			Notification saved = createNotification(userId);

			given(typeRegistry.resolve(PaymentCanceledEvent.class)).willReturn(meta);
			given(cloudEventMapper.fromDomainEvent(eq(event), eq("payment-" + paymentId))).willReturn(ce);
			given(notificationFactory.createSingle(userId, NotificationType.PAYMENT_CANCEL_SUCCEEDED,
				String.valueOf(paymentId), "PAYMENT", ce)).willReturn(notification);
			given(notificationRepository.save(notification)).willReturn(saved);

			eventHandler.handlePaymentCanceled(event);

			then(notificationRepository).should().save(notification);
			then(pushPort).should().send(userId, saved);
		}
	}

	@Nested
	@DisplayName("handlePaymentRefunded")
	class HandlePaymentRefunded {

		@Test
		@DisplayName("결제 환불 이벤트를 수신하면 알림을 생성하고 푸시한다")
		void createsAndPushes() {
			Long paymentId = 4L;
			Long userId = 400L;
			PaymentRefundedEvent event = new PaymentRefundedEvent(
				paymentId, "FUNDING", userId, Money.of(2000), PaymentType.FUNDING,
				"환불 사유", LocalDateTime.now()
			);

			CloudEventMeta meta = new CloudEventMeta(
				"app.giftify.payment.refunded", java.net.URI.create("/giftify/payment"),
				NotificationType.PAYMENT_CANCEL_FAILED
			);
			CloudEventEnvelope ce = CloudEventEnvelope.of(
				"evt-4", "/giftify/payment", "app.giftify.payment.refunded",
				"payment-4", OffsetDateTime.now()
			);
			Notification notification = createNotification(userId);
			Notification saved = createNotification(userId);

			given(typeRegistry.resolve(PaymentRefundedEvent.class)).willReturn(meta);
			given(cloudEventMapper.fromDomainEvent(eq(event), eq("payment-" + paymentId))).willReturn(ce);
			given(notificationFactory.createSingle(userId, NotificationType.PAYMENT_CANCEL_FAILED,
				String.valueOf(paymentId), "PAYMENT", ce)).willReturn(notification);
			given(notificationRepository.save(notification)).willReturn(saved);

			eventHandler.handlePaymentRefunded(event);

			then(notificationRepository).should().save(notification);
			then(pushPort).should().send(userId, saved);
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
