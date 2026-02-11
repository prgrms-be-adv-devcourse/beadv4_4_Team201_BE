package app.giftify.wishlist.adapter.in.web.controller;

import app.giftify.security.common.CurrentMemberId;
import app.giftify.wishlist.adapter.in.web.responseDto.WishlistItemResponse;
import app.giftify.wishlist.application.port.in.AddWishlistItemUseCase;
import app.giftify.wishlist.application.port.in.GetWishlistItemUseCase;
import app.giftify.wishlist.application.port.in.RemoveWishlistItemUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2/wishlists/items")
@RequiredArgsConstructor
@Validated
public class WishlistItemController implements WishlistItemV2ApiSpec {
    private final AddWishlistItemUseCase addWishlistItemUseCase;
    private final GetWishlistItemUseCase getWishlistItemUseCase;
    private final RemoveWishlistItemUseCase removeWishlistItemUseCase;

    @Override
    @GetMapping("/me")
    public ResponseEntity<List<WishlistItemResponse>> getAllProducts(
            @CurrentMemberId Long memberId
    ) {
        List<WishlistItemResponse> items = getWishlistItemUseCase.getWishlistItems(memberId)
                .stream()
                .map(WishlistItemResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(items);
    }

    @Override
    @GetMapping("/me/check")
    public ResponseEntity<?> isExistProduct(
            @CurrentMemberId Long memberId,
            @RequestParam(name = "productId") Long productId
    ) {
        boolean exists = getWishlistItemUseCase.isItemExists(memberId, productId);

        return ResponseEntity.ok(exists);
    }

    @Override
    @PostMapping("/add")
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
    @DeleteMapping("/remove")
    public ResponseEntity<Void> removeProduct(
            @CurrentMemberId Long memberId,
            @RequestParam(name = "productId") Long productId
    ) {
        RemoveWishlistItemUseCase.WishlistItemRemoveCommand command = new RemoveWishlistItemUseCase.WishlistItemRemoveCommand(
                memberId,
                productId
        );

        removeWishlistItemUseCase.removeWishlistItem(command);

        return ResponseEntity.noContent().build();
    }
}
