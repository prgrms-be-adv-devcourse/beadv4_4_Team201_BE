package app.giftify.member.adapter.in.web.controller.wishlist;

import app.giftify.member.adapter.in.web.requestDto.wishlist.UpdateWishlistSettingsRequest;
import app.giftify.member.application.port.in.wishlist.GetWishlistUseCase;
import app.giftify.member.application.port.in.wishlist.UpdateWishlistSettingsUseCase;
import app.giftify.member.core.domain.exception.wishlist.WishlistNotFoundException;
import app.giftify.member.core.domain.wishlist.Visibility;
import app.giftify.member.core.domain.wishlist.Wishlist;
import app.giftify.security.common.context.AuthenticatedMember;
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
