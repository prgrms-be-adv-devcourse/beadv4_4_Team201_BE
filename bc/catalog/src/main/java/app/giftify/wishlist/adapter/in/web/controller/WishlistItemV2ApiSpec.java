package app.giftify.wishlist.adapter.in.web.controller;

import app.giftify.security.common.CurrentMemberId;
import app.giftify.shared.api.response.RsData;
import app.giftify.wishlist.adapter.in.web.responseDto.WishlistItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Wishlist Item V2", description = "위시리스트 아이템 관련 API")
public interface WishlistItemV2ApiSpec {

    @Operation(summary = "내 위시리스트 아이템 전체 조회", description = "현재 로그인한 사용자의 위시리스트에 담긴 모든 상품을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    ResponseEntity<List<WishlistItemResponse>> getAllProducts(
            @Parameter(hidden = true) @CurrentMemberId Long memberId
    );

    @Operation(summary = "위시리스트 아이템 존재 여부 확인", description = "특정 상품이 위시리스트에 담겨있는지 확인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "확인 성공")
    })
    ResponseEntity<?> isExistProduct(
            @Parameter(hidden = true) @CurrentMemberId Long memberId,
            @Parameter(description = "상품 ID", required = true, example = "1")
            @RequestParam(name = "productId") Long productId
    );

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

    @Operation(summary = "타인의 위시리스트 아이템 목록 조회",
            description = "특정 사용자의 위시리스트 아이템 목록을 조회합니다. \n\n" +
                    "로그인 시, 타겟 멤버와 친구인 경우 PUBLIC 또는 FRIENDS_ONLY 위시리스트의 아이템을 반환합니다.\n\n" +
                    "로그인 시, 타겟 멤버와 친구가 아닌 경우 PUBLIC 위시리스트의 아이템만 반환합니다.\n\n" +
                    "비로그인 시, PUBLIC 위시리스트의 아이템만 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "조회 실패 - 위시리스트 조회 권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "조회 실패 - MemberId로 등록된 위시리스트 없음", content = @Content)
    })
    ResponseEntity<RsData<List<WishlistItemResponse>>> getWishlistItems(
            @Parameter(description = "조회 대상 회원 ID", required = true, example = "1")
            @PathVariable("memberId") Long memberId
    );
}
