package app.giftify.app.funding;

import app.giftify.domain.funding.FundingWishlistItem;
import app.giftify.domain.product.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import app.giftify.out.FundingWishlistItemRepository;
import app.giftify.out.product.ProductRepository;

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
