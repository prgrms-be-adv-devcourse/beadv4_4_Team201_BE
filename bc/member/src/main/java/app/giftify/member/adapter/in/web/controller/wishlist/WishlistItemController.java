package app.giftify.member.adapter.in.web.controller.wishlist;

import app.giftify.member.application.port.in.wishlist.AddWishlistItemUseCase;
import app.giftify.member.application.port.in.wishlist.GetWishlistItemUseCase;
import app.giftify.member.application.port.in.wishlist.RemoveWishlistItemUseCase;
import app.giftify.security.common.context.AuthenticatedMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/wishlist/item/")
@RequiredArgsConstructor
@Validated
public class WishlistItemController {
    private final AddWishlistItemUseCase addWishlistItemUseCase;
    private final GetWishlistItemUseCase getWishlistItemUseCase;
    private final RemoveWishlistItemUseCase removeWishlistItemUseCase;

    // 위시리스트에 담긴 모든 상품 조회
    @GetMapping("/me")
    public ResponseEntity<?> getAllProducts(
            @AuthenticatedMember String authSub
    ) {
        return ResponseEntity.ok(getWishlistItemUseCase.getWishlistItems(authSub));
    }

    // 특정 상품이 위시리스트에 포함되어 있는지 확인
    @GetMapping("/me/check")
    public ResponseEntity<?> isExistProduct(
            @AuthenticatedMember String authSub,
            @RequestParam @Valid Long productId
    ) {
        boolean exists = getWishlistItemUseCase.isItemExists(authSub, productId);

        return ResponseEntity.ok(exists);
    }

    // 위시리스트에 새로운 상품 추가
    // TODO: product 정보 조회 - sanpShot으로 구현
//    @PostMapping("/add")
//    public ResponseEntity<Void> addProduct(
//            @AuthenticatedMember String authSub,
//            @RequestParam @Valid Long productId
//    ) {
//
//        // product 정보 조회
//
//        // command 생성
//        AddWishlistItemUseCase.WishlistItemAddCommand command = new AddWishlistItemUseCase.WishlistItemAddCommand(
//            authSub, productId,
//        );
//
//        return ResponseEntity.ok(addWishlistItemUseCase.addWishlistItem(command));
//    }

    // 위시리스트에서 특정 상품 삭제
    @DeleteMapping("/remove")
    public ResponseEntity<Void> removeProduct(
            @AuthenticatedMember String authSub,
            @RequestParam @Valid Long productId
    ) {
        RemoveWishlistItemUseCase.WishlistItemRemoveCommand command = new RemoveWishlistItemUseCase.WishlistItemRemoveCommand(
                authSub,
                productId
        );

        removeWishlistItemUseCase.removeWishlistItem(command);

        return ResponseEntity.noContent().build();
    }
}
