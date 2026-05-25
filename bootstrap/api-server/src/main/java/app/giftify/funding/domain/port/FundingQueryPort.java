package app.giftify.funding.domain.port;

import app.giftify.funding.domain.vo.FundingInfo;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface FundingQueryPort {
    Map<Long, FundingInfo> findFundingInfoByWishlistItemIds(List<Long> wishlistItemIds);

    Optional<FundingInfo> findFundingInfoByWishlistItemId(Long wishlistItemId);

    Optional<Long> findFundingIdByWishlistItemId(Long wishlistItemId);
}

