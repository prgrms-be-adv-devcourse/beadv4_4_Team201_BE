package app.giftify.wishlist.adapter.in.web.controller;

import app.giftify.security.common.CurrentMemberId;
import app.giftify.wishlist.adapter.in.web.requestDto.UpdateWishlistSettingsRequest;
import app.giftify.wishlist.adapter.in.web.responseDto.WishlistResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Wishlist V2", description = "위시리스트 관련 API")
public interface WishlistV2ApiSpec {

    @Operation(summary = "내 위시리스트 조회", description = "현재 로그인한 사용자의 위시리스트 기본 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    ResponseEntity<WishlistResponse> getMyInfo(
            @Parameter(hidden = true) @CurrentMemberId Long memberId
    );

    @Operation(summary = "위시리스트 설정 변경", description = "위시리스트 공개 설정을 변경합니다. (PUBLIC / PRIVATE / FRIENDS_ONLY)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "설정 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    ResponseEntity<WishlistResponse> updateSettings(
            @Parameter(hidden = true) @CurrentMemberId Long memberId,
            @RequestBody @Valid UpdateWishlistSettingsRequest request
    );
}
