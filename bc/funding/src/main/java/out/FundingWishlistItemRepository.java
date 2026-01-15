package out;

import domain.funding.FundingWishlistItem;

public interface FundingWishlistItemRepository {

    FundingWishlistItem save(FundingWishlistItem item);

    FundingWishlistItem getReferenceById(Long id);
}

