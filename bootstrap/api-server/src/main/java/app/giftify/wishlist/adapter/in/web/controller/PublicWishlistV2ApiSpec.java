package app.giftify.wishlist.adapter.in.web.controller;

import app.giftify.support.common.api.response.RsData;
import app.giftify.wishlist.adapter.in.web.responseDto.MemberWishlistSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Public Wishlist V2", description = "타인의 공개 위시리스트 조회 API ")
public interface PublicWishlistV2ApiSpec {

    @Operation(summary = "공개 위시리스트 사용자 검색",
            description = "PUBLIC 위시리스트를 보유한 사용자를 검색합니다. nickname 미입력 시 전체 목록을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공")
    })
    ResponseEntity<RsData<List<MemberWishlistSummaryResponse>>> search(
            @Parameter(description = "닉네임 검색어 (부분 일치)", required = false, example = "chan")
            @RequestParam(value = "nickname", required = false) String nickname
    );

//	@Operation(summary = "타인의 공개 위시리스트 상세 조회",
//		description = "특정 사용자의 PUBLIC 위시리스트 아이템 목록을 조회합니다. 비공개 위시리스트인 경우 빈 결과를 반환합니다.")
//	@ApiResponses({
//		@ApiResponse(responseCode = "200", description = "조회 성공")
//	})
//	ResponseEntity<RsData<PublicWishlistResponse>> getPublicWishlist(
//		@Parameter(description = "조회 대상 회원 ID", required = true, example = "1")
//		@PathVariable("memberId") Long memberId
//	);
}
