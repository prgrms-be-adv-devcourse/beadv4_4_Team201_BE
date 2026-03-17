package app.giftify.notification.application.support;

import app.giftify.notification.application.CloudEventEnvelope;
import app.giftify.notification.domain.Notification;
import app.giftify.notification.domain.NotificationType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationFactory {

    public Notification createSingle(
            Long recipientId, NotificationType type,
            String referenceId, String referenceType,
            CloudEventEnvelope sourceEvent
    ) {
        return new Notification(
                recipientId, type,
                type.getTitle(), resolveContent(type),
                referenceId, referenceType,
                sourceEvent.id(), sourceEvent.type(), sourceEvent.source()
        );
    }

    public Notification createSingle(
            Long recipientId, NotificationType type,
            String referenceId, String referenceType,
            CloudEventEnvelope sourceEvent, String customContent
    ) {
        return new Notification(
                recipientId, type,
                type.getTitle(), customContent,
                referenceId, referenceType,
                sourceEvent.id(), sourceEvent.type(), sourceEvent.source()
        );
    }

    public List<Notification> createForMultipleRecipients(
            List<Long> recipientIds, NotificationType type,
            String referenceId, String referenceType,
            CloudEventEnvelope sourceEvent
    ) {
        return recipientIds.stream()
                .map(id -> createSingle(id, type, referenceId, referenceType, sourceEvent))
                .toList();
    }

	private String resolveContent(NotificationType type) {
		return switch (type) {
			case FUNDING_CREATED -> "위시리스트 아이템에 새로운 펀딩이 시작되었습니다";
			case FUNDING_ACHIEVED -> "펀딩이 목표 금액을 달성했습니다";
			case FUNDING_EXPIRED -> "펀딩이 기한이 지나 만료되었습니다";
			case FUNDING_CANCELED -> "펀딩이 취소되었습니다";
            case FUNDING_FAIL_ACCEPT -> "펀딩 수락이 실패했습니다. 재수락 해주세요";
			case FRIEND_REQUEST_RECEIVED -> "새로운 친구 요청을 확인해보세요";
			case FRIEND_REQUEST_ACCEPTED -> "친구 요청이 수락되어 친구가 되었습니다";
			case PAYMENT_SUCCEEDED -> "결제가 성공적으로 처리되었습니다";
			case PAYMENT_FAILED -> "결제 처리 중 문제가 발생했습니다";
			case PAYMENT_CANCEL_SUCCEEDED -> "결제 취소가 정상적으로 처리되었습니다";
			case PAYMENT_CANCEL_FAILED -> "결제 취소 처리 중 문제가 발생했습니다";
            case PRODUCT_SELLER_ORDER_RECEIVED -> "판매 중인 상품에 대해 새로운 주문이 인입되었습니다";
		};
	}
}
