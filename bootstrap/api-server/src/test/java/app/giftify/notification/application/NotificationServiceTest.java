package app.giftify.notification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import app.giftify.notification.application.outbound.NotificationRepository;
import app.giftify.notification.domain.Notification;
import app.giftify.notification.domain.NotificationErrorCode;
import app.giftify.notification.domain.NotificationException;
import app.giftify.notification.domain.NotificationType;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

	@Mock
	NotificationRepository notificationRepository;

	@InjectMocks
	NotificationService notificationService;

	@Nested
	@DisplayName("markAsRead")
	class MarkAsRead {

		@Test
		@DisplayName("알림 소유자가 읽음 처리하면 정상 저장된다")
		void success() {
			Long notificationId = 1L;
			Long memberId = 100L;
			Notification notification = createNotification(memberId);

			given(notificationRepository.findById(notificationId))
				.willReturn(Optional.of(notification));

			notificationService.markAsRead(notificationId, memberId);

			assertThat(notification.isRead()).isTrue();
			then(notificationRepository).should().save(notification);
		}

		@Test
		@DisplayName("존재하지 않는 알림이면 NOTIFICATION_NOT_FOUND 예외가 발생한다")
		void notFound() {
			Long notificationId = 999L;
			Long memberId = 100L;

			given(notificationRepository.findById(notificationId))
				.willReturn(Optional.empty());

			assertThatThrownBy(() -> notificationService.markAsRead(notificationId, memberId))
				.isInstanceOf(NotificationException.class)
				.satisfies(ex -> {
					NotificationException nex = (NotificationException) ex;
					assertThat(nex.getErrorCode()).isEqualTo(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
				});
		}

		@Test
		@DisplayName("알림 소유자가 아니면 NOT_NOTIFICATION_RECIPIENT 예외가 발생한다")
		void notOwner() {
			Long notificationId = 1L;
			Long ownerId = 100L;
			Long otherMemberId = 999L;
			Notification notification = createNotification(ownerId);

			given(notificationRepository.findById(notificationId))
				.willReturn(Optional.of(notification));

			assertThatThrownBy(() -> notificationService.markAsRead(notificationId, otherMemberId))
				.isInstanceOf(NotificationException.class)
				.satisfies(ex -> {
					NotificationException nex = (NotificationException) ex;
					assertThat(nex.getErrorCode()).isEqualTo(NotificationErrorCode.NOT_NOTIFICATION_RECIPIENT);
				});
		}
	}

	@Nested
	@DisplayName("markAllAsRead")
	class MarkAllAsRead {

		@Test
		@DisplayName("memberId를 전달하여 repository 일괄 읽음 처리를 위임한다")
		void delegatesToRepository() {
			Long memberId = 100L;

			notificationService.markAllAsRead(memberId);

			then(notificationRepository).should().markAllAsReadByRecipientId(memberId);
		}
	}

	@Nested
	@DisplayName("getNotifications")
	class GetNotifications {

		@Test
		@DisplayName("memberId와 pageable로 repository 조회를 위임한다")
		void delegatesToRepository() {
			Long memberId = 100L;
			Pageable pageable = PageRequest.of(0, 10);
			Page<Notification> expected = new PageImpl<>(List.of(createNotification(memberId)));

			given(notificationRepository.findByRecipientId(memberId, pageable))
				.willReturn(expected);

			Page<Notification> result = notificationService.getNotifications(memberId, pageable);

			assertThat(result).isEqualTo(expected);
		}
	}

	@Nested
	@DisplayName("getUnreadCount")
	class GetUnreadCount {

		@Test
		@DisplayName("읽지 않은 알림 개수를 반환한다")
		void returnsCount() {
			Long memberId = 100L;

			given(notificationRepository.countByRecipientIdAndIsReadFalse(memberId))
				.willReturn(5L);

			long count = notificationService.getUnreadCount(memberId);

			assertThat(count).isEqualTo(5L);
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
