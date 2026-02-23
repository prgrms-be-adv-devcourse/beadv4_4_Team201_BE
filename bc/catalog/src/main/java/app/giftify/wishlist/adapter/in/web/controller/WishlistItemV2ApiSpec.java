package app.giftify.wishlist.adapter.in.web.controller;

import app.giftify.security.common.CurrentMemberId;
import app.giftify.wishlist.adapter.in.web.responseDto.WishlistItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Wishlist Item V2", description = "위시리스트 아이템 관련 API")
public interface WishlistItemV2ApiSpec {

    @Operation(summary = "위시리스트 아이템 추가", description = "위시리스트에 상품을 추가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추가 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (판매 중이 아닌 상품 등)"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    ResponseEntity<WishlistItemResponse> addProduct(
            @Parameter(hidden = true) @CurrentMemberId Long memberId,
            @Parameter(description = "상품 ID", required = true, example = "1")
            @RequestParam(name = "productId") Long productId
    );

    @Operation(summary = "위시리스트 아이템 삭제", description = "위시리스트에서 특정 상품을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    ResponseEntity<Void> removeProduct(
            @Parameter(hidden = true) @CurrentMemberId Long memberId,
            @Parameter(description = "상품 ID", required = true, example = "1")
            @RequestParam(name = "productId") Long productId
    );
}
