package app.giftify.app.funding;

import app.giftify.domain.funding.Funding;
import app.giftify.domain.funding.FundingWishlistItem;
import app.giftify.in.funding.WishlistItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class FundingFacade {
    private final FundingCreateUseCase fundingCreateUseCase;
    private final FundingSyncItemUseCase fundingSyncItemUseCase;


    public Funding startFunding(WishlistItemDto wishlistItemDto, Integer amount) {
        // 1. WishlistItem 복제
        FundingWishlistItem syncedItem = fundingSyncItemUseCase.syncItem(wishlistItemDto);
        
        // 2. Funding 생성 (첫 결제 금액으로)
        return fundingCreateUseCase.createFunding(syncedItem.getId(), amount);
    }

    public FundingWishlistItem syncItem(WishlistItemDto dto) {
        return fundingSyncItemUseCase.syncItem(dto);
    }

}
