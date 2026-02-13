package app.giftify.cart.adapter.inbound;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import app.giftify.security.common.CurrentMemberId;
import app.giftify.shared.api.response.RsData;
import app.giftify.shared.domain.type.TargetType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Cart V2", description = "장바구니 관련 API")
public interface CartV2ApiSpec {

	@Operation(summary = "장바구니에 펀딩 추가", description = "장바구니에 펀딩을 장바구니에 추가합니다.")
	ResponseEntity<RsData<Void>> addItemToMyCart(
			@CurrentMemberId Long memberId,
			@RequestBody CartItemRequest request
	);

	@Operation(summary = "내 장바구니에 복수 펀딩 추가", description = "내 장바구니에 여러 펀딩을 장바구니에 추가합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "추가 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 targetType 또는 targetId)"),
		@ApiResponse(responseCode = "401", description = "인증 실패")
	})
	ResponseEntity<RsData<Void>> addItemsToMyCart(
		@Parameter(hidden = true) @CurrentMemberId Long memberId,
		@RequestBody List<CartItemRequest> requests
	);

	@Operation(summary = "장바구니 조회", description = "특정 회원의 장바구니를 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패")
	})
	ResponseEntity<RsData<CartResponse>> getCart(
		@PathVariable("cartId") Long cartId,
		@Parameter(hidden = true) @CurrentMemberId Long memberId
	);

	@Operation(summary = "내 장바구니 조회", description = "현재 로그인한 회원의 장바구니를 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "404", description = "장바구니 없음")
	})
	ResponseEntity<RsData<CartResponse>> getMyCart(@Parameter(hidden = true) @CurrentMemberId Long memberId);

	@Operation(summary = "장바구니 아이템 복수 금액 수정")
	@ApiResponse(responseCode = "200", description = "수정 성공")
	ResponseEntity<RsData<Void>> updateItemsAmount(
		@Parameter(hidden = true) @CurrentMemberId Long memberId,
		@RequestBody List<CartItemRequest> requests
	);

	@Operation(summary = "장바구니 아이템 복수 삭제")
	@ApiResponse(responseCode = "200", description = "삭제 성공")
	ResponseEntity<RsData<Void>> removeItems(
		@Parameter(hidden = true) @CurrentMemberId Long memberId,
		@Parameter(description = "타겟 타입", example = "FUNDING_PENDING")
		@PathVariable(value = "targetType") TargetType targetType,
		@Parameter(description = "타겟 ID 목록", example = "1,2,3")
		@RequestParam(value = "targetIds") List<Long> targetIds
	);

	@Operation(summary = "장바구니 비우기")
	@ApiResponse(responseCode = "200", description = "비우기 성공")
	ResponseEntity<RsData<Void>> clearCart(
		@Parameter(hidden = true) @CurrentMemberId Long memberId
	);
}
