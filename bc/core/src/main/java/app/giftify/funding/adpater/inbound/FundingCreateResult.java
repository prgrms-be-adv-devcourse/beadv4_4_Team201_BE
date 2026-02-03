package app.giftify.funding.adpater.inbound;

import app.giftify.funding.adpater.outbound.jpa.Funding;
import app.giftify.shared.domain.vo.WishlistItemSnapshot;

public record FundingCreateResult(
        Funding funding,
        WishlistItemSnapshot wishlistItemSnapshot
) {
}
