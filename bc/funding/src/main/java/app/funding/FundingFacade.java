package app.funding;

import domain.funding.Funding;
import domain.funding.FundingWishlistItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FundingFacade {
    private final FundingCreateUseCase fundingCreateUseCase;
    private final FundingSyncItemUseCase fundingSyncItemUseCase;

    @Transactional
    public FundingWishlistItem syncItem(Long wishlistItemId, Long productId) {
        return fundingSyncItemUseCase.syncItem(wishlistItemId,productId);
    }

    @Transactional
    public Funding createFunding(Long participantId, Long itemId, Integer amount) {
        return fundingCreateUseCase.createFunding(participantId, itemId, amount);
    }

}
