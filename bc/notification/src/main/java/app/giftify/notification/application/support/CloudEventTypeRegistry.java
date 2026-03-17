package app.giftify.notification.application.support;

import java.net.URI;
import java.util.Map;

import app.giftify.shared.domain.event.funding.*;
import org.springframework.stereotype.Component;

import app.giftify.notification.domain.NotificationType;
import app.giftify.shared.domain.event.payment.PaymentCancelFailedEvent;
import app.giftify.shared.domain.event.payment.PaymentCanceledEvent;
import app.giftify.shared.domain.event.payment.PaymentFailedEvent;
import app.giftify.shared.domain.event.payment.PaymentSucceededEvent;

@Component
public class CloudEventTypeRegistry {

	private static final URI SOURCE_FUNDING = URI.create("/giftify/funding");
	private static final URI SOURCE_PAYMENT = URI.create("/giftify/payment");
	private static final URI SOURCE_MEMBER = URI.create("/giftify/member");
	public static final URI SOURCE_NOTIFICATION = URI.create("/giftify/notification");

	public record CloudEventMeta(String ceType, URI ceSource, NotificationType notificationType) {
	}

	private static final Map<Class<?>, CloudEventMeta> REGISTRY = Map.of(
		FundingCreatedEvent.class, new CloudEventMeta("app.giftify.funding.created", SOURCE_FUNDING, NotificationType.FUNDING_CREATED),
		FundingAchievedEvent.class, new CloudEventMeta("app.giftify.funding.achieved", SOURCE_FUNDING, NotificationType.FUNDING_ACHIEVED),
		FundingExpiredEvent.class, new CloudEventMeta("app.giftify.funding.expired", SOURCE_FUNDING, NotificationType.FUNDING_EXPIRED),
		FundingCanceledEvent.class, new CloudEventMeta("app.giftify.funding.canceled", SOURCE_FUNDING, NotificationType.FUNDING_CANCELED),
        FundingFailAcceptEvent.class, new CloudEventMeta("app.giftify.funding.fail-accept", SOURCE_FUNDING, NotificationType.FUNDING_FAIL_ACCEPT),
		PaymentSucceededEvent.class, new CloudEventMeta("app.giftify.payment.succeeded", SOURCE_PAYMENT, NotificationType.PAYMENT_SUCCEEDED),
		PaymentFailedEvent.class, new CloudEventMeta("app.giftify.payment.failed", SOURCE_PAYMENT, NotificationType.PAYMENT_FAILED),
		PaymentCanceledEvent.class, new CloudEventMeta("app.giftify.payment.canceled", SOURCE_PAYMENT, NotificationType.PAYMENT_CANCEL_SUCCEEDED),
		PaymentCancelFailedEvent.class, new CloudEventMeta("app.giftify.payment.cancel-failed", SOURCE_PAYMENT, NotificationType.PAYMENT_CANCEL_FAILED)
	);

	public CloudEventMeta resolve(Class<?> eventClass) {
		CloudEventMeta meta = REGISTRY.get(eventClass);
		if (meta == null) {
			throw new IllegalArgumentException("Unmapped event class: " + eventClass.getName());
		}
		return meta;
	}

	public boolean supports(Class<?> eventClass) {
		return REGISTRY.containsKey(eventClass);
	}

	public static String toNotificationCeType(NotificationType type) {
		return "app.giftify.notification." + type.name().toLowerCase().replace('_', '-');
	}
}
