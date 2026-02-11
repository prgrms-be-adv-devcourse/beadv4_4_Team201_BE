package app.giftify.wishlist.adapter.in.web.controller;

import app.giftify.security.common.CurrentMemberId;
import app.giftify.wishlist.adapter.in.web.requestDto.UpdateWishlistSettingsRequest;
import app.giftify.wishlist.adapter.in.web.responseDto.WishlistResponse;
import app.giftify.wishlist.application.port.in.GetWishlistUseCase;
import app.giftify.wishlist.application.port.in.UpdateWishlistSettingsUseCase;
import app.giftify.wishlist.core.domain.Visibility;
import app.giftify.wishlist.core.domain.Wishlist;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/wishlists")
@RequiredArgsConstructor
@Validated
public class WishlistController implements WishlistV2ApiSpec {

    private final UpdateWishlistSettingsUseCase updateWishlistSettingsUseCase;
    private final GetWishlistUseCase getWishlistUseCase;

    // 내 위시리스트 정보 조회
    @Override
    @GetMapping("/me")
    public ResponseEntity<WishlistResponse> getMyInfo(
            @CurrentMemberId Long memberId
    ) {
        return ResponseEntity.ok(WishlistResponse.from(getWishlistUseCase.getOrCreateWishlistByMemberId(memberId)));
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
}
