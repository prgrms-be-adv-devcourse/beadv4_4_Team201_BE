package app.giftify.funding.adpater.inbound;

import app.giftify.funding.application.FundingService;
import app.giftify.shared.domain.vo.FundingSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/funding")
public class InternalFundingController {
    private final FundingService fundingService;

    @GetMapping("/wishlist-items/{wishlistItemId}/snapshot")
    public ResponseEntity<FundingSnapshot> getFundingSnapshotByWishlistItem(@PathVariable Long wishlistItemId) {
        return fundingService.getSnapshot(wishlistItemId)
                .map(ResponseEntity::ok)                        // IN_PROGRESS, ACHIEVED 펀딩 있음 -> 200 OK
                .orElse(ResponseEntity.notFound().build());     // 없거나 다른 상태 -> 404 Not Found
    }
}
