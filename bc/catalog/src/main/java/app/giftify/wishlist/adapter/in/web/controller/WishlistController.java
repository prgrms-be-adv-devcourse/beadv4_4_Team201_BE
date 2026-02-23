package app.giftify.wishlist.adapter.in.web.controller;

import app.giftify.replica.member.MemberRepository;
import app.giftify.security.common.CurrentMemberId;
import app.giftify.security.common.util.SecurityUtil;
import app.giftify.shared.api.response.RsData;
import app.giftify.wishlist.adapter.in.web.requestDto.UpdateWishlistSettingsRequest;
import app.giftify.wishlist.adapter.in.web.responseDto.WishlistItemResponse;
import app.giftify.wishlist.adapter.in.web.responseDto.WishlistResponse;
import app.giftify.wishlist.application.port.in.GetWishlistItemUseCase;
import app.giftify.wishlist.application.port.in.GetWishlistUseCase;
import app.giftify.wishlist.application.port.in.UpdateWishlistSettingsUseCase;
import app.giftify.wishlist.core.domain.Visibility;
import app.giftify.wishlist.core.domain.Wishlist;
import app.giftify.wishlist.core.domain.WishlistItemDetail;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/wishlists")
@RequiredArgsConstructor
@Validated
public class WishlistController implements WishlistV2ApiSpec {

    private final UpdateWishlistSettingsUseCase updateWishlistSettingsUseCase;
    private final GetWishlistUseCase getWishlistUseCase;
    private final GetWishlistItemUseCase getWishlistItemUseCase;
    private final MemberRepository memberRepository;

    // 내 위시리스트 기본 정보 + 아이템 목록 조회
    @Override
    @GetMapping("/me")
    public ResponseEntity<WishlistResponse> getMyWishlist(
            @CurrentMemberId Long memberId
    ) {
        Wishlist wishlist = getWishlistUseCase.getOrCreateWishlistByMemberId(memberId);

        String nickname = memberRepository.findById(memberId)
                .map(m -> m.getNickname())
                .orElse(null);

        List<WishlistItemResponse> items = getWishlistItemUseCase.getWishlistItems(memberId)
                .stream()
                .map(WishlistItemResponse::from)
                .toList();

        return ResponseEntity.ok(WishlistResponse.from(wishlist, nickname, items));
    }

    // 위시리스트 공개 범위 설정
    @Override
    @PatchMapping("/me/settings")
    public ResponseEntity<WishlistResponse> updateSettings(
            @CurrentMemberId Long memberId,
            @RequestBody @Valid UpdateWishlistSettingsRequest request
    ) {
        Visibility visibility = Visibility.from(request.visibility());

        UpdateWishlistSettingsUseCase.UpdateSettingsCommand command = new UpdateWishlistSettingsUseCase.UpdateSettingsCommand(
                memberId,
                visibility
        );

        Wishlist updatedWishlist = updateWishlistSettingsUseCase.updateSettings(command);

        return ResponseEntity.ok(WishlistResponse.from(updatedWishlist));
    }

    /**
     * 타인의 위시리스트 아이템 목록 조회
     * 로그인 상태 : 친구 관계라면 PUBLIC + FRIENDS_ONLY / 친구가 아니면 PUBLIC
     * 비로그인 상태 : PUBLIC
     */
    @Override
    @GetMapping("/{memberId}")
    public ResponseEntity<RsData<List<WishlistItemResponse>>> getWishlistItems(
            @PathVariable("memberId") Long targetMemberId
    ) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId().orElse(null);

        List<WishlistItemDetail> details = getWishlistUseCase.getWishlistItemDetails(targetMemberId, currentMemberId);

        List<WishlistItemResponse> response = details.stream()
                .map(WishlistItemResponse::from)
                .toList();

        return ResponseEntity.ok(RsData.success(response));
    }
}
