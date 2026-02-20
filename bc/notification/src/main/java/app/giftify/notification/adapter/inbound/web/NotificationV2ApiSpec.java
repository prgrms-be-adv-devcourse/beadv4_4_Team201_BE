package app.giftify.notification.adapter.inbound.web;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import app.giftify.notification.adapter.inbound.web.dto.NotificationResponse;
import app.giftify.notification.adapter.inbound.web.dto.UnreadCountResponse;
import app.giftify.shared.api.response.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Notification V2", description = "알림 API (v2)")
public interface NotificationV2ApiSpec {

	@Operation(
		summary = "SSE 알림 구독",
		description = """
			서버에서 실시간 알림을 수신하기 위한 SSE 연결을 생성합니다.

			**연결 흐름**:
			1. 이 엔드포인트로 GET 요청 (Accept: text/event-stream)
			2. 초기 connect 이벤트 수신으로 연결 확인
			3. 이후 알림 발생 시 CloudEvents JSON 형식으로 수신

			**참고**:
			- 타임아웃: 30분 (이후 재연결 필요)
			- 동일 사용자의 기존 연결은 새 연결 시 종료됨
			"""
	)
	@ApiResponse(responseCode = "200", description = "SSE 스트림 연결 성공")
	@ApiResponse(responseCode = "401", description = "인증 토큰 누락 또는 유효하지 않음", content = @Content)
	SseEmitter subscribe(@Parameter(hidden = true) Long memberId);

	@Operation(
		summary = "알림 목록 조회",
		description = "로그인한 사용자의 알림을 페이지네이션으로 조회합니다."
	)
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@ApiResponse(responseCode = "401", description = "인증 토큰 누락 또는 유효하지 않음", content = @Content)
	ResponseEntity<RsData<Page<NotificationResponse>>> getNotifications(
		@Parameter(hidden = true) Long memberId,
		Pageable pageable
	);

	@Operation(
		summary = "읽지 않은 알림 조회",
		description = "읽지 않은 알림만 페이지네이션으로 조회합니다."
	)
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@ApiResponse(responseCode = "401", description = "인증 토큰 누락 또는 유효하지 않음", content = @Content)
	ResponseEntity<RsData<Page<NotificationResponse>>> getUnreadNotifications(
		@Parameter(hidden = true) Long memberId,
		Pageable pageable
	);

	@Operation(
		summary = "읽지 않은 알림 수 조회",
		description = "읽지 않은 알림의 총 개수를 반환합니다."
	)
	@ApiResponse(
		responseCode = "200",
		description = "조회 성공",
		content = @Content(schema = @Schema(implementation = UnreadCountResponse.class))
	)
	@ApiResponse(responseCode = "401", description = "인증 토큰 누락 또는 유효하지 않음", content = @Content)
	ResponseEntity<RsData<UnreadCountResponse>> getUnreadCount(
		@Parameter(hidden = true) Long memberId
	);

	@Operation(
		summary = "알림 읽음 처리",
		description = "특정 알림을 읽음으로 표시합니다."
	)
	@ApiResponse(responseCode = "200", description = "읽음 처리 성공")
	@ApiResponse(responseCode = "401", description = "인증 토큰 누락 또는 유효하지 않음", content = @Content)
	@ApiResponse(responseCode = "403", description = "해당 알림의 수신자가 아님", content = @Content)
	@ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음", content = @Content)
	ResponseEntity<RsData<Void>> markAsRead(
		@Parameter(hidden = true) Long memberId,
		Long notificationId
	);

	@Operation(
		summary = "전체 알림 읽음 처리",
		description = "로그인한 사용자의 모든 알림을 읽음으로 표시합니다."
	)
	@ApiResponse(responseCode = "200", description = "전체 읽음 처리 성공")
	@ApiResponse(responseCode = "401", description = "인증 토큰 누락 또는 유효하지 않음", content = @Content)
	ResponseEntity<RsData<Void>> markAllAsRead(
		@Parameter(hidden = true) Long memberId
	);
}
