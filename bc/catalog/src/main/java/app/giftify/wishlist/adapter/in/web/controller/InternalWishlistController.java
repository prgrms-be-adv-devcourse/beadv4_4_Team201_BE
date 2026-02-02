package app.giftify.wishlist.adapter.in.web.controller;

import app.giftify.shared.domain.vo.WishlistItemSnapshot;
import app.giftify.wishlist.application.port.in.GetWishlistItemSnapshotUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/wishlist")
@RequiredArgsConstructor
public class InternalWishlistController {

    private final GetWishlistItemSnapshotUseCase getWishlistItemSnapshotUseCase;

    // 위시리스트 아이템 스냅샷 조회
    @GetMapping("/items/{wishlistItemId}/snapshot")
    public ResponseEntity<?> getSnapshot(
            @PathVariable("wishlistItemId") Long wishlistItemId
    ) {
        WishlistItemSnapshot snapshot = getWishlistItemSnapshotUseCase.getSnapshot(wishlistItemId);
        return ResponseEntity.ok(snapshot);
    }
}
