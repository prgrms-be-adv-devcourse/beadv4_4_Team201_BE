package app.giftify.notification.adapter.inbound.web;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import app.giftify.notification.adapter.inbound.web.dto.NotificationResponse;
import app.giftify.notification.adapter.inbound.web.dto.UnreadCountResponse;
import app.giftify.notification.application.inbound.NotificationCommandUseCase;
import app.giftify.notification.application.inbound.NotificationQueryUseCase;
import app.giftify.notification.application.inbound.SseSubscribeUseCase;
import app.giftify.security.common.CurrentMemberId;
import app.giftify.shared.api.response.RsData;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController implements NotificationV2ApiSpec {

	private final NotificationQueryUseCase queryUseCase;
	private final NotificationCommandUseCase commandUseCase;
	private final SseSubscribeUseCase sseSubscribeUseCase;

	@Override
	@GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter subscribe(@CurrentMemberId Long memberId) {
		return sseSubscribeUseCase.subscribe(memberId);
	}

	@Override
	@GetMapping
	public ResponseEntity<RsData<Page<NotificationResponse>>> getNotifications(
		@CurrentMemberId Long memberId,
		@PageableDefault(size = 20) Pageable pageable
	) {
		Page<NotificationResponse> page = queryUseCase.getNotifications(memberId, pageable)
			.map(NotificationResponse::from);
		return ResponseEntity.ok(RsData.success(page));
	}

	@Override
	@GetMapping("/unread")
	public ResponseEntity<RsData<Page<NotificationResponse>>> getUnreadNotifications(
		@CurrentMemberId Long memberId,
		@PageableDefault(size = 20) Pageable pageable
	) {
		Page<NotificationResponse> page = queryUseCase.getUnreadNotifications(memberId, pageable)
			.map(NotificationResponse::from);
		return ResponseEntity.ok(RsData.success(page));
	}

	@Override
	@GetMapping("/unread/count")
	public ResponseEntity<RsData<UnreadCountResponse>> getUnreadCount(
		@CurrentMemberId Long memberId
	) {
		long count = queryUseCase.getUnreadCount(memberId);
		return ResponseEntity.ok(RsData.success(new UnreadCountResponse(count)));
	}

	@Override
	@PatchMapping("/{notificationId}/read")
	public ResponseEntity<RsData<Void>> markAsRead(
		@CurrentMemberId Long memberId,
		@PathVariable Long notificationId
	) {
		commandUseCase.markAsRead(notificationId, memberId);
		return ResponseEntity.ok(RsData.success(null));
	}

	@Override
	@PatchMapping("/read-all")
	public ResponseEntity<RsData<Void>> markAllAsRead(
		@CurrentMemberId Long memberId
	) {
		commandUseCase.markAllAsRead(memberId);
		return ResponseEntity.ok(RsData.success(null));
	}
}
