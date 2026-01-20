package app.giftify.out.funding;

import app.giftify.domain.funding.FundingWishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FundingWishlistItemRepository extends JpaRepository<FundingWishlistItem, Long> {

    FundingWishlistItem save(FundingWishlistItem item);

    FundingWishlistItem getReferenceById(Long id);
}

