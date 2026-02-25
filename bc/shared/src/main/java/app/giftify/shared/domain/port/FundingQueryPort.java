package app.giftify.shared.domain.port;

import app.giftify.shared.domain.vo.FundingInfo;

import java.util.List;
import java.util.Map;

public interface FundingQueryPort {
    Map<Long, FundingInfo> findFundingInfoByWishlistItemIds(List<Long> wishlistItemIds);

    FundingInfo findFundingInfoByWishlistItemId(Long wishlistItemId);
}
