package app.giftify.cart.adapter.inbound;

import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import app.giftify.security.common.CurrentMemberId;
import app.giftify.shared.api.response.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Cart V2", description = "장바구니 관련 API")
public interface CartV2ApiSpec {

	@Operation(summary = "장바구니에 상품 추가", description = "상품 또는 펀딩을 장바구니에 추가합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "추가 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 targetType 또는 targetId)"),
		@ApiResponse(responseCode = "401", description = "인증 실패")
	})
	ResponseEntity<RsData<Void>> addItem(
		@PathVariable("cartId") Long cartId,
		@RequestBody CartItemRequest request
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
}
