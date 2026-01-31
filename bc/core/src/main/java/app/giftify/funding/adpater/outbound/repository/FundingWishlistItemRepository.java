package app.giftify.funding.adpater.outbound.repository;

import app.giftify.funding.adpater.outbound.jpa.FundingWishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FundingWishlistItemRepository extends JpaRepository<FundingWishlistItem, Long> {

    FundingWishlistItem save(FundingWishlistItem item);

    FundingWishlistItem getReferenceById(Long id);
}
