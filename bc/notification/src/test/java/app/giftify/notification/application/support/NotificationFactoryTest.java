package app.giftify.notification.application.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import app.giftify.notification.application.CloudEventEnvelope;
import app.giftify.notification.domain.Notification;
import app.giftify.notification.domain.NotificationType;

class NotificationFactoryTest {

	NotificationFactory factory = new NotificationFactory();

	@Nested
	@DisplayName("createSingle")
	class CreateSingle {

		@Test
		@DisplayName("모든 필드가 올바르게 매핑된 알림을 생성한다")
		void mapsAllFields() {
			Long recipientId = 100L;
			NotificationType type = NotificationType.PAYMENT_SUCCEEDED;
			String referenceId = "pay-1";
			String referenceType = "PAYMENT";
			CloudEventEnvelope ce = CloudEventEnvelope.of(
				"evt-1", "/giftify/payment", "app.giftify.payment.succeeded",
				"payment-1", OffsetDateTime.now()
			);

			Notification notification = factory.createSingle(
				recipientId, type, referenceId, referenceType, ce
			);

			assertThat(notification.getRecipientId()).isEqualTo(recipientId);
			assertThat(notification.getType()).isEqualTo(type);
			assertThat(notification.getTitle()).isEqualTo(type.getTitle());
			assertThat(notification.getReferenceId()).isEqualTo(referenceId);
			assertThat(notification.getReferenceType()).isEqualTo(referenceType);
			assertThat(notification.getSourceEventId()).isEqualTo("evt-1");
			assertThat(notification.getSourceEventType()).isEqualTo("app.giftify.payment.succeeded");
			assertThat(notification.getSourceEventSource()).isEqualTo("/giftify/payment");
			assertThat(notification.isRead()).isFalse();
		}

		@Test
		@DisplayName("PAYMENT_SUCCEEDED 타입의 title과 content가 정확하다")
		void paymentSucceededContent() {
			CloudEventEnvelope ce = CloudEventEnvelope.of(
				"evt-1", "/giftify/payment", "app.giftify.payment.succeeded",
				"payment-1", OffsetDateTime.now()
			);

			Notification notification = factory.createSingle(
				1L, NotificationType.PAYMENT_SUCCEEDED, "1", "PAYMENT", ce
			);

			assertThat(notification.getTitle()).isEqualTo("결제가 완료되었습니다");
			assertThat(notification.getContent()).isEqualTo("결제가 성공적으로 처리되었습니다");
		}

		@Test
		@DisplayName("PAYMENT_FAILED 타입의 content가 정확하다")
		void paymentFailedContent() {
			CloudEventEnvelope ce = CloudEventEnvelope.of(
				"evt-2", "/giftify/payment", "app.giftify.payment.failed",
				"payment-2", OffsetDateTime.now()
			);

			Notification notification = factory.createSingle(
				1L, NotificationType.PAYMENT_FAILED, "2", "PAYMENT", ce
			);

			assertThat(notification.getTitle()).isEqualTo("결제에 실패했습니다");
			assertThat(notification.getContent()).isEqualTo("결제 처리 중 문제가 발생했습니다");
		}

		@Test
		@DisplayName("FUNDING_CREATED 타입의 content가 정확하다")
		void fundingCreatedContent() {
			CloudEventEnvelope ce = CloudEventEnvelope.of(
				"evt-3", "/giftify/funding", "app.giftify.funding.created",
				"funding-1", OffsetDateTime.now()
			);

			Notification notification = factory.createSingle(
				1L, NotificationType.FUNDING_CREATED, "1", "FUNDING", ce
			);

			assertThat(notification.getTitle()).isEqualTo("새로운 펀딩이 시작되었습니다");
			assertThat(notification.getContent()).isEqualTo("위시리스트 아이템에 새로운 펀딩이 시작되었습니다");
		}
	}

	@Nested
	@DisplayName("createForMultipleRecipients")
	class CreateForMultipleRecipients {

		@Test
		@DisplayName("수신자 수만큼 알림을 생성하고 각각 고유한 recipientId를 가진다")
		void createsForEachRecipient() {
			List<Long> recipientIds = List.of(1L, 2L, 3L);
			CloudEventEnvelope ce = CloudEventEnvelope.of(
				"evt-1", "/giftify/funding", "app.giftify.funding.achieved",
				"funding-1", OffsetDateTime.now()
			);

			List<Notification> notifications = factory.createForMultipleRecipients(
				recipientIds, NotificationType.FUNDING_ACHIEVED,
				"1", "FUNDING", ce
			);

			assertThat(notifications).hasSize(3);
			assertThat(notifications)
				.extracting(Notification::getRecipientId)
				.containsExactly(1L, 2L, 3L);
			assertThat(notifications)
				.allSatisfy(n -> {
					assertThat(n.getType()).isEqualTo(NotificationType.FUNDING_ACHIEVED);
					assertThat(n.getReferenceId()).isEqualTo("1");
				});
		}
	}
}
