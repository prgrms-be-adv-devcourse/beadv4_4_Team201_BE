package out;

import domain.funding.Funding;

public interface FundingRepository {

    Funding save(Funding funding);

    boolean existsByWishlistItemIdAndStatusInProgress(Long wishlistItemId);
}
