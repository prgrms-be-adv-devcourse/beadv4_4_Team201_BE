package app.giftify.wishlist.adapter.in.web.controller;

import app.giftify.wishlist.domain.vo.WishlistItemSnapshot;
import app.giftify.wishlist.application.port.in.GetWishlistItemSnapshotUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/internal/wishlist")
@RequiredArgsConstructor
public class InternalWishlistController {

    private final GetWishlistItemSnapshotUseCase getWishlistItemSnapshotUseCase;

    @PostMapping("/items/snapshots")
    public Map<Long, WishlistItemSnapshot> getSnapshotList(
            @RequestBody List<Long> wishlistItemIds
    ) {
        return getWishlistItemSnapshotUseCase.getSnapshotList(wishlistItemIds);
    }
}
