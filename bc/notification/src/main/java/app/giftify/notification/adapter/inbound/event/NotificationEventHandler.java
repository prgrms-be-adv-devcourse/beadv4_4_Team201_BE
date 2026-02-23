package app.giftify.notification.adapter.inbound.event;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import app.giftify.notification.application.CloudEventEnvelope;
import app.giftify.notification.application.support.CloudEventMapper;
import app.giftify.notification.application.support.CloudEventTypeRegistry;
import app.giftify.notification.application.support.NotificationFactory;
import app.giftify.notification.application.outbound.NotificationPushPort;
import app.giftify.notification.application.outbound.NotificationRepository;
import app.giftify.notification.domain.Notification;
import app.giftify.notification.domain.NotificationType;
import app.giftify.shared.domain.event.funding.FundingAchievedEvent;
import app.giftify.shared.domain.event.funding.FundingCanceledEvent;
import app.giftify.shared.domain.event.funding.FundingCreatedEvent;
import app.giftify.shared.domain.event.funding.FundingExpiredEvent;
import app.giftify.shared.domain.event.payment.PaymentCancelFailedEvent;
import app.giftify.shared.domain.event.payment.PaymentCanceledEvent;
import app.giftify.shared.domain.event.payment.PaymentFailedEvent;
import app.giftify.shared.domain.event.payment.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

	private final CloudEventMapper cloudEventMapper;
	private final CloudEventTypeRegistry typeRegistry;
	private final NotificationFactory notificationFactory;
	private final NotificationRepository notificationRepository;
	private final NotificationPushPort pushPort;

	// -- Funding 이벤트 (receiverId 미포함, 이벤트 필드 추가 후 활성화) --

	@ApplicationModuleListener
	public void handleFundingCreated(FundingCreatedEvent event) {
		log.info("[Notification] FundingCreatedEvent: fundingId={}", event.getFundingId());
		// TODO: event에 receiverId 추가 후 활성화
	}

	@ApplicationModuleListener
	public void handleFundingAchieved(FundingAchievedEvent event) {
		log.info("[Notification] FundingAchievedEvent: fundingId={}", event.getFundingId());
		// TODO: event에 receiverId + participantIds 추가 후 활성화
	}

	@ApplicationModuleListener
	public void handleFundingExpired(FundingExpiredEvent event) {
		log.info("[Notification] FundingExpiredEvent: fundingId={}", event.getFundingId());
		// TODO: event에 receiverId + participantIds 추가 후 활성화
	}

	@ApplicationModuleListener
	public void handleFundingCanceled(FundingCanceledEvent event) {
		log.info("[Notification] FundingCanceledEvent: fundingId={}", event.getFundingId());
		// TODO: event에 receiverId + participantIds 추가 후 활성화
	}

	// -- Payment 이벤트 --

	@ApplicationModuleListener
	public void handlePaymentSucceeded(PaymentSucceededEvent event) {
		log.info("[Notification] PaymentSucceededEvent: paymentId={}", event.data().paymentId());
		var meta = typeRegistry.resolve(event.getClass());
		CloudEventEnvelope ce = cloudEventMapper.fromDomainEvent(event, "payment-" + event.data().paymentId());
		createAndPush(event.data().memberId(), meta.notificationType(),
			String.valueOf(event.data().paymentId()), "PAYMENT", ce);
	}

	@ApplicationModuleListener
	public void handlePaymentFailed(PaymentFailedEvent event) {
		log.info("[Notification] PaymentFailedEvent: paymentId={}", event.data().paymentId());
		var meta = typeRegistry.resolve(event.getClass());
		CloudEventEnvelope ce = cloudEventMapper.fromDomainEvent(event, "payment-" + event.data().paymentId());
		createAndPush(event.data().memberId(), meta.notificationType(),
			String.valueOf(event.data().paymentId()), "PAYMENT", ce);
	}

	@ApplicationModuleListener
	public void handlePaymentCanceled(PaymentCanceledEvent event) {
		log.info("[Notification] PaymentCanceledEvent: paymentId={}", event.data().paymentId());
		var meta = typeRegistry.resolve(event.getClass());
		CloudEventEnvelope ce = cloudEventMapper.fromDomainEvent(event, "payment-" + event.data().paymentId());
		createAndPush(event.data().memberId(), meta.notificationType(),
			String.valueOf(event.data().paymentId()), "PAYMENT", ce);
	}

	@ApplicationModuleListener
	public void handlePaymentCancelFailed(PaymentCancelFailedEvent event) {
		log.info("[Notification] PaymentCancelFailedEvent: paymentId={}", event.data().paymentId());
		var meta = typeRegistry.resolve(event.getClass());
		CloudEventEnvelope ce = cloudEventMapper.fromDomainEvent(event, "payment-" + event.data().paymentId());
		createAndPush(event.data().memberId(), meta.notificationType(),
			String.valueOf(event.data().paymentId()), "PAYMENT", ce);
	}

	// -- 헬퍼 --

	private void createAndPush(Long recipientId, NotificationType type,
		String referenceId, String referenceType, CloudEventEnvelope ce) {
		Notification notification = notificationFactory.createSingle(
			recipientId, type, referenceId, referenceType, ce);
		Notification saved = notificationRepository.save(notification);
		pushPort.send(recipientId, saved);
	}
}
