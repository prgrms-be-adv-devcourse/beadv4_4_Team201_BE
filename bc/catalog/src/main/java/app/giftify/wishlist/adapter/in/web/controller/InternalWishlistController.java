package app.giftify.wishlist.adapter.in.web.controller;

import app.giftify.shared.domain.vo.WishlistItemSnapshot;
import app.giftify.wishlist.application.port.in.GetWishlistItemSnapshotUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/internal/wishlist")
@RequiredArgsConstructor
public class InternalWishlistController {

    private final GetWishlistItemSnapshotUseCase getWishlistItemSnapshotUseCase;

    // 위시리스트 아이템 스냅샷 조회
    @PostMapping("/items/snapshots")
    public ResponseEntity<List<WishlistItemSnapshot>> getSnapshot(
            @RequestBody List<Long> wishlistItemIds
    ) {
        List<WishlistItemSnapshot> snapshots = getWishlistItemSnapshotUseCase.getSnapshot(wishlistItemIds);
        return ResponseEntity.ok(snapshots);
    }
}
