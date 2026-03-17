package app.giftify.notification.adapter.inbound.event;

import app.giftify.notification.application.CloudEventEnvelope;
import app.giftify.notification.application.outbound.NotificationPushPort;
import app.giftify.notification.application.outbound.NotificationRepository;
import app.giftify.notification.application.support.CloudEventMapper;
import app.giftify.notification.application.support.CloudEventTypeRegistry;
import app.giftify.notification.application.support.NotificationFactory;
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
import app.giftify.shared.domain.event.product.ProductSellerOrderReceivedEvent;
import app.giftify.shared.domain.vo.FundingDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final CloudEventMapper cloudEventMapper;
    private final CloudEventTypeRegistry typeRegistry;
    private final NotificationFactory notificationFactory;
    private final NotificationRepository notificationRepository;
    private final NotificationPushPort pushPort;

    @ApplicationModuleListener
    public void handleFundingFailAccept(FundingFailAcceptEvent event) {
        log.info("[Notification] FundingFailAcceptEvent: fundingId={}, receiverId={}" , event.fundingId() , event.receiverId());
        var meta = typeRegistry.resolve(event.getClass());
        CloudEventEnvelope ce = cloudEventMapper.fromDomainEvent(event, "funding-" + event.fundingId());

        createAndPush(event.receiverId(), meta.notificationType(),
                String.valueOf(event.fundingId()), "FUNDING", ce);
    }

	@ApplicationModuleListener
	public void handleFundingCreated(FundingCreatedEvent event) {
		for (FundingDetail detail : event.getFundings()) {
			log.info("[Notification] FundingCreatedEvent: fundingId={}, receiverId={}", detail.fundingId(), detail.receiverId());
            var meta = typeRegistry.resolve(event.getClass());
            CloudEventEnvelope ce = cloudEventMapper.fromDomainEvent(event, "funding-" + detail.fundingId());
            createAndPush(detail.receiverId(), meta.notificationType(),
                    String.valueOf(detail.fundingId()), "FUNDING", ce);
        }
    }

    @ApplicationModuleListener
    public void handleFundingAchieved(FundingAchievedEvent event) {
        log.info("[Notification] FundingAchievedEvent: fundingId={}", event.getFundingId());
        var meta = typeRegistry.resolve(event.getClass());
        CloudEventEnvelope ce = cloudEventMapper.fromDomainEvent(event, "funding-" + event.getFundingId());

        // 수령자에게 알림 전송
        createAndPush(event.getReceiverId(), meta.notificationType(),
                String.valueOf(event.getFundingId()), "FUNDING", ce);

        // 참여자들에게 알림 전송
        if (event.getParticipantIds() != null && !event.getParticipantIds().isEmpty()) {
            for (Long participantId : event.getParticipantIds()) {
                createAndPush(participantId, meta.notificationType(),
                        String.valueOf(event.getFundingId()), "FUNDING", ce);
            }
        }
    }

    @ApplicationModuleListener
    public void handleFundingExpired(FundingExpiredEvent event) {
        log.info("[Notification] FundingExpiredEvent: fundingId={}", event.getFundingId());
        var meta = typeRegistry.resolve(event.getClass());
        CloudEventEnvelope ce = cloudEventMapper.fromDomainEvent(event, "funding-" + event.getFundingId());

        createAndPush(event.getReceiverId(), meta.notificationType(),
                String.valueOf(event.getFundingId()), "FUNDING", ce);

        if (event.getParticipantIds() != null && !event.getParticipantIds().isEmpty()) {
            for (Long participantId : event.getParticipantIds()) {
                createAndPush(participantId, meta.notificationType(),
                        String.valueOf(event.getFundingId()), "FUNDING", ce);
            }
        }
    }

    @ApplicationModuleListener
    public void handleFundingCanceled(FundingCanceledEvent event) {
        log.info("[Notification] FundingCanceledEvent: fundingId={}", event.getFundingId());
        var meta = typeRegistry.resolve(event.getClass());
        CloudEventEnvelope ce = cloudEventMapper.fromDomainEvent(event, "funding-" + event.getFundingId());

        createAndPush(event.getReceiverId(), meta.notificationType(),
                String.valueOf(event.getFundingId()), "FUNDING", ce);

        if (event.getParticipantIds() != null && !event.getParticipantIds().isEmpty()) {
            for (Long participantId : event.getParticipantIds()) {
                createAndPush(participantId, meta.notificationType(),
                        String.valueOf(event.getFundingId()), "FUNDING", ce);
            }
        }
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

    // -- Product 이벤트 --

    @ApplicationModuleListener
    public void handleProductSellerOrderReceived(ProductSellerOrderReceivedEvent event) {
        log.info("[Notification] ProductSellerOrderReceivedEvent: 항목 수={}", event.getItems().size());
        var meta = typeRegistry.resolve(event.getClass());
        CloudEventEnvelope ce = cloudEventMapper.fromDomainEvent(event, "product-seller-order");

        List<Notification> notifications = event.getItems().stream()
                .map(item -> {
                    String content = "[%s] %d개의 주문이 인입되었습니다".formatted(item.productName(), item.quantity());
                    return notificationFactory.createSingle(
                            item.sellerId(), meta.notificationType(),
                            String.valueOf(item.productId()), "PRODUCT",
                            ce, content);
                })
                .toList();

        List<Notification> saved = notificationRepository.saveAll(notifications);
        saved.forEach(n -> pushPort.send(n.getRecipientId(), n));
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
