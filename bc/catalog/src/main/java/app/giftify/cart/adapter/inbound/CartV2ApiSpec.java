package app.giftify.cart.adapter.inbound;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
	ResponseEntity<Void> addItem(
		@Parameter(hidden = true) Long memberId,
		@RequestBody CartItemRequest request
	);
}
