package app.giftify.funding.application;

import app.giftify.funding.adpater.outbound.jpa.FundingWishlistItem;
import app.giftify.funding.adpater.inbound.dto.WishlistItemDto;
import app.giftify.funding.adpater.outbound.repository.FundingWishlistItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FundingSyncItemUseCase {

    private final FundingWishlistItemRepository fundingWishlistItemRepository;

    /**
     * Member BC의 WishlistItem을 Funding BC로 복제 (스냅샷 생성)
     *
     * 시점: Payment 완료 시 (결제 시점의 상품 정보를 스냅샷으로 저장)
     * 목적: Funding 생성 시 필요한 상품 정보(ID, 이름, 가격)를 값으로 복제
     */
    public FundingWishlistItem syncItem(WishlistItemDto dto) {
        // DTO 정보를 그대로 FundingWishlistItem에 저장 (스냅샷)
        FundingWishlistItem fundingWishlistItem = new FundingWishlistItem(
            dto.wishlistItemId(),
            dto.receiverId(),
            dto.productId(),
            dto.productName(),
            dto.productPrice(),
            FundingWishlistItem.WishListItemStatus.IN_PROGRESS
        );

        return fundingWishlistItemRepository.save(fundingWishlistItem);
    }
}
