package app.giftify.wishlist.adapter.in.web.controller;

import app.giftify.security.common.CurrentMemberId;
import app.giftify.wishlist.adapter.in.web.responseDto.WishlistItemResponse;
import app.giftify.wishlist.application.port.in.AddWishlistItemUseCase;
import app.giftify.wishlist.application.port.in.RemoveWishlistItemUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/wishlists")
@RequiredArgsConstructor
@Validated
public class WishlistItemController implements WishlistItemV2ApiSpec {
    private final AddWishlistItemUseCase addWishlistItemUseCase;
    private final RemoveWishlistItemUseCase removeWishlistItemUseCase;

    @Override
    @PostMapping("/me/items/add")
    public ResponseEntity<WishlistItemResponse> addProduct(
            @CurrentMemberId Long memberId,
            @RequestParam(name = "productId") Long productId
    ) {
        // command 생성
        AddWishlistItemUseCase.WishlistItemAddCommand command = new AddWishlistItemUseCase.WishlistItemAddCommand(
                productId
        );

        WishlistItemResponse wishlistItemResponse = WishlistItemResponse.from(
                addWishlistItemUseCase.addWishlistItem(memberId, command));

        // 비즈니스 로직(중복 체크, 판매 상태 검증 등)은 서비스 계층에서 수행
        return ResponseEntity.ok(wishlistItemResponse);
    }

    @Override
    @DeleteMapping("/items/{wishlistItemId}")
    public ResponseEntity<Void> removeProduct(
            @CurrentMemberId Long memberId,
            @PathVariable(name = "wishlistItemId") Long wishlistItemId
    ) {
        RemoveWishlistItemUseCase.WishlistItemRemoveCommand command = new RemoveWishlistItemUseCase.WishlistItemRemoveCommand(
                memberId,
                wishlistItemId
        );

        removeWishlistItemUseCase.removeWishlistItem(command);

        return ResponseEntity.noContent().build();
    }
}
