package app.giftify.wishlist.adapter.in.web.controller;

import app.giftify.shared.domain.vo.WishlistItemSnapshot;
import app.giftify.wishlist.application.port.in.GetWishlistItemSnapshotUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/items/snapshots")
    public ResponseEntity<List<WishlistItemSnapshot>> getSnapshotList(
            @RequestBody List<Long> wishlistItemIds
    ) {
        List<WishlistItemSnapshot> snapshots = getWishlistItemSnapshotUseCase.getSnapshotList(wishlistItemIds);
        return ResponseEntity.ok(snapshots);
    }
}
