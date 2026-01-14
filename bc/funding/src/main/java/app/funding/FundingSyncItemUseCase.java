package app.funding;

import domain.Product;
import domain.funding.FundingWishlistItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import out.FundingWishlistItemRepository;
import out.product.ProductRepository;

@Service
@RequiredArgsConstructor
public class FundingSyncItemUseCase {
    private final FundingWishlistItemRepository fundingWishlistItemRepository;
    private final ProductRepository productRepository;

    @Transactional
    public FundingWishlistItem syncItem(Long wishlistItemId, Long productId) {
        Product product = productRepository.getReferenceById(productId);

        FundingWishlistItem fundingWishlistItem = new FundingWishlistItem(
            wishlistItemId,
            product,
            FundingWishlistItem.WishListItemStatus.PENDING
        );

        return fundingWishlistItemRepository.save(fundingWishlistItem);
    }
}
