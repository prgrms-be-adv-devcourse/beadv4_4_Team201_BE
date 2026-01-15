package app.giftify.out;

import org.springframework.data.jpa.repository.JpaRepository;

import app.giftify.domain.funding.FundingWishlistItem;

public interface FundingWishlistItemRepository extends JpaRepository<FundingWishlistItem, Long> {

	FundingWishlistItem save(FundingWishlistItem item);

	FundingWishlistItem getReferenceById(Long id);
}

