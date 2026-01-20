package app.giftify.app.funding;

import app.giftify.domain.funding.Funding;
import app.giftify.domain.funding.FundingErrorCode;
import app.giftify.domain.funding.FundingException;
import app.giftify.domain.funding.FundingWishlistItem;
import app.giftify.out.FundingRepository;
import app.giftify.out.FundingWishlistItemRepository;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.funding.FundingAchievedEvent;
import app.giftify.shared.domain.event.funding.FundingCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class FundingCreateUseCase {

    private final FundingRepository fundingRepository;
    private final FundingWishlistItemRepository fundingWishlistItemRepository;
    private final EventPublisher eventPublisher;

    public Funding createFunding(Long itemId, Integer amount) {
        FundingWishlistItem wishlistItem = fundingWishlistItemRepository.findById(itemId).orElseThrow(() ->
            new FundingException(
                FundingErrorCode.WISHLIST_ITEM_NOT_FOUND,
                "위시리스트 상품이 존재하지 않습니다. ID: " + itemId
            ));

        Funding funding = Funding.startFunding(wishlistItem, amount);
        fundingRepository.save(funding);

        // Member BC에서 수신하여 WishlistItem 상태 변경 (PENDING → IN_PROGRESS)
        eventPublisher.publish(new FundingCreatedEvent(
            funding.getId(),
            wishlistItem.getWishlistId(),
            funding.getTargetAmount(),
            funding.getDeadline()
        ));

        // 첫 결제로 바로 목표 달성한 경우 FundingAchievedEvent 발행
        if (funding.isAchieved()) {
            eventPublisher.publish(new FundingAchievedEvent(
                funding.getId(),
                wishlistItem.getWishlistId(),
                funding.getTargetAmount(),
                wishlistItem.getProductId(),
                wishlistItem.getReceiverId()
            ));
        }

        return funding;
    }
}
