package app.giftify.app.funding;

import app.giftify.domain.funding.Funding;
import app.giftify.domain.funding.FundingWishlistItem;
import app.giftify.in.funding.FundingCompleteResponseDto;
import app.giftify.in.funding.FundingResponseDto;
import app.giftify.in.funding.WishlistItemDto;
import app.giftify.shared.api.paging.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class FundingFacade {
    private final FundingCreateUseCase fundingCreateUseCase;
    private final FundingSyncItemUseCase fundingSyncItemUseCase;
    private final FundingGetUseCase fundingGetUseCase;
    private final FundingCloseUseCase fundingCloseUseCase;
    private final FundingExpireUseCase fundingExpireUseCase;

    @Transactional
    public Funding startFunding(WishlistItemDto wishlistItemDto, Integer amount) {
        // 1. WishlistItem 복제 (recipientMemberId 포함)
        FundingWishlistItem syncedItem = fundingSyncItemUseCase.syncItem(wishlistItemDto);

        // 2. Funding 생성 (첫 결제 금액으로, 수령자 정보는 WishlistItem에서 자동으로 가져옴)
        return fundingCreateUseCase.createFunding(syncedItem.getId(), amount);
    }

    @Transactional
    public FundingWishlistItem syncItem(WishlistItemDto dto) {
        return fundingSyncItemUseCase.syncItem(dto);
    }

    @Transactional(readOnly = true)
    public FundingResponseDto getFunding(Long id) {
        return fundingGetUseCase.getFunding(id);
    }

    @Transactional
    public FundingCompleteResponseDto closeFunding(Long id) {
        return fundingCloseUseCase.closeFunding(id);
    }

    @Transactional
    public FundingCompleteResponseDto expireFunding(Long id) {
        return fundingExpireUseCase.expireFunding(id);
    }

    @Transactional
    public List<FundingCompleteResponseDto> expireExpiredFundings() {
        return fundingExpireUseCase.expireExpiredFundings();
    }

    @Transactional(readOnly = true)
    public PageResponse<FundingResponseDto> getFundings(int page, int size) {
        return fundingGetUseCase.getFundings(page, size);
    }
}
