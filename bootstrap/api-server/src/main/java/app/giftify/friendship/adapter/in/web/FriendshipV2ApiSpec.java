package app.giftify.friendship.adapter.in.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import app.giftify.friendship.adapter.in.web.dto.FriendRequestResponse;
import app.giftify.friendship.adapter.in.web.dto.FriendResponse;
import app.giftify.friendship.adapter.in.web.dto.FriendshipResponse;
import app.giftify.friendship.adapter.in.web.dto.SendFriendRequestDto;
import app.giftify.security.common.CurrentMemberId;
import app.giftify.support.common.api.response.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "FriendShip V2", description = "소셜 기능 관련 API")
public interface FriendshipV2ApiSpec {

	@Operation(summary = "친구 요청 전송")
	@ApiResponse(responseCode = "201", description = "친구 요청 생성 성공")
	@ApiResponse(responseCode = "400", description = "자기 자신에게 요청 / 탈퇴 회원에게 요청")
	@ApiResponse(responseCode = "404", description = "대상 회원을 찾을 수 없음")
	@ApiResponse(responseCode = "409", description = "이미 친구 관계가 존재함")
	ResponseEntity<RsData<FriendshipResponse>> sendRequest(
		@Parameter(hidden = true) @CurrentMemberId Long memberId,
		@Valid SendFriendRequestDto request
	);

	@Operation(summary = "친구 요청 수락")
	@ApiResponse(responseCode = "200", description = "수락 성공")
	@ApiResponse(responseCode = "400", description = "수신자만 수락 가능 / PENDING 상태가 아님")
	@ApiResponse(responseCode = "404", description = "친구 관계를 찾을 수 없음")
	ResponseEntity<RsData<FriendshipResponse>> accept(
		@Parameter(hidden = true) @CurrentMemberId Long memberId,
		@PathVariable Long friendshipId
	);

	@Operation(summary = "친구 요청 거절")
	@ApiResponse(responseCode = "200", description = "거절 성공")
	@ApiResponse(responseCode = "400", description = "수신자만 거절 가능 / PENDING 상태가 아님")
	@ApiResponse(responseCode = "404", description = "친구 관계를 찾을 수 없음")
	ResponseEntity<RsData<Void>> reject(
		@Parameter(hidden = true) @CurrentMemberId Long memberId,
		@PathVariable Long friendshipId
	);

	@Operation(summary = "친구 삭제")
	@ApiResponse(responseCode = "200", description = "삭제 성공")
	@ApiResponse(responseCode = "400", description = "당사자만 삭제 가능")
	@ApiResponse(responseCode = "404", description = "친구 관계를 찾을 수 없음")
	ResponseEntity<RsData<Void>> remove(
		@Parameter(hidden = true) @CurrentMemberId Long memberId,
		@PathVariable Long friendshipId
	);

	@Operation(summary = "내 친구 목록 조회")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	ResponseEntity<RsData<List<FriendResponse>>> getFriends(
		@Parameter(hidden = true) @CurrentMemberId Long memberId
	);

	@Operation(summary = "받은 친구 요청 목록 조회")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	ResponseEntity<RsData<List<FriendRequestResponse>>> getReceivedRequests(
		@Parameter(hidden = true) @CurrentMemberId Long memberId
	);
}
