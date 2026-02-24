package app.giftify.wishlist.adapter.in.web.controller;

import app.giftify.security.common.CurrentMemberId;
import app.giftify.shared.api.response.RsData;
import app.giftify.wishlist.adapter.in.web.requestDto.UpdateWishlistSettingsRequest;
import app.giftify.wishlist.adapter.in.web.responseDto.WishlistItemResponse;
import app.giftify.wishlist.adapter.in.web.responseDto.WishlistResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Wishlist V2", description = "위시리스트 관련 API")
public interface WishlistV2ApiSpec {

    @Operation(summary = "내 위시리스트 + 아이템 조회", description = "현재 로그인한 사용자의 위시리스트 정보와 페이징된 아이템 목록을 함께 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/me")
    ResponseEntity<WishlistResponse> getMyWishlist(
            @Parameter(hidden = true) @CurrentMemberId Long memberId,
            Pageable pageable
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
