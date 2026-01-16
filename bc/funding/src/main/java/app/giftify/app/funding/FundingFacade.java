package app.giftify.app.funding;

import app.giftify.domain.funding.Funding;
import app.giftify.domain.funding.FundingWishlistItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class FundingFacade {
    private final FundingCreateUseCase fundingCreateUseCase;
    private final FundingSyncItemUseCase fundingSyncItemUseCase;


    public FundingWishlistItem syncItem(Long wishlistItemId, Long productId) {
        return fundingSyncItemUseCase.syncItem(wishlistItemId,productId);
    }

    public Funding createFunding(Long itemId, Integer amount) {
        return fundingCreateUseCase.createFunding(itemId, amount);
    }

}
