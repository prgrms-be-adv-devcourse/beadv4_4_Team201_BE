package app.giftify.wishlist.adapter.in.web.controller;

import app.giftify.security.common.context.AuthenticatedMember;
import app.giftify.wishlist.adapter.in.web.requestDto.UpdateWishlistSettingsRequest;
import app.giftify.wishlist.application.port.in.GetWishlistUseCase;
import app.giftify.wishlist.application.port.in.UpdateWishlistSettingsUseCase;
import app.giftify.wishlist.core.domain.Visibility;
import app.giftify.wishlist.core.domain.Wishlist;
import app.giftify.wishlist.core.domain.exceptioin.WishlistNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("api/wishlist")
@RequiredArgsConstructor
@Validated
public class WishlistController {

    private final UpdateWishlistSettingsUseCase updateWishlistSettingsUseCase;
    private final GetWishlistUseCase getWishlistUseCase;

    // 위시리스트 조회
    // 현재 로그인한 사용자의 위시리스트 기본 정보 조회
    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(
            @AuthenticatedMember String authSub
    ) {
        if (authSub == null) {
            return ResponseEntity.status(401).build();
        }

        return getWishlistUseCase.getWishlistByAuthSub(authSub)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new WishlistNotFoundException(authSub));
    }

    // 위시리스트 설정 변경
    // PUBLIC / PRIVATE / FRIENDS_ONLY
    @PatchMapping("/me/settings")
    public ResponseEntity<?> updateSettings(
            @AuthenticatedMember String authSub,
            @RequestBody @Valid UpdateWishlistSettingsRequest request
    ) {
        if (authSub == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<Wishlist> wishlist = getWishlistUseCase.getWishlistByAuthSub(authSub);
        if (wishlist.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        Visibility visibility = Visibility.from(request.visibility());

        UpdateWishlistSettingsUseCase.UpdateSettingsCommand command = new UpdateWishlistSettingsUseCase.UpdateSettingsCommand(
                authSub,
                visibility
        );

        Wishlist updatedWishlist = updateWishlistSettingsUseCase.updateSettings(command);

        return ResponseEntity.ok(updatedWishlist);
    }
}
