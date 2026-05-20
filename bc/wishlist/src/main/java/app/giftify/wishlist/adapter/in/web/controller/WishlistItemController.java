package app.giftify.wishlist.adapter.in.web.controller;

import app.giftify.security.common.CurrentMemberId;
import app.giftify.shared.api.response.RsData;
import app.giftify.wishlist.application.port.in.AddWishlistItemUseCase;
import app.giftify.wishlist.application.port.in.GetWishlistItemUseCase;
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
    private final GetWishlistItemUseCase getWishlistItemUseCase;

    @Override
    @PostMapping("/me/items/add")
    public ResponseEntity<RsData<Object>> addProduct(
            @CurrentMemberId Long memberId,
            @RequestParam(name = "productId") Long productId
    ) {
        // command 생성
        AddWishlistItemUseCase.WishlistItemAddCommand command = new AddWishlistItemUseCase.WishlistItemAddCommand(
                productId
        );

        addWishlistItemUseCase.addWishlistItem(memberId, command);

        return ResponseEntity.status(201).body(RsData.success(null, "위시리스트아이템 추가 완료"));
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

    @Override
    @GetMapping("/me/items/check")
    public ResponseEntity<RsData<Boolean>> checkItemExistence(
            @CurrentMemberId Long memberId,
            @RequestParam(name = "productId") Long productId
    ) {
        boolean exists = getWishlistItemUseCase.isItemExists(memberId, productId);
        return ResponseEntity.ok(RsData.success(exists));
    }
}
