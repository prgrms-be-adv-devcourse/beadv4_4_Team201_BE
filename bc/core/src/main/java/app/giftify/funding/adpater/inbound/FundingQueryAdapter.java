package app.giftify.funding.adpater.inbound;

import app.giftify.funding.adpater.outbound.jpa.Funding;
import app.giftify.funding.adpater.outbound.repository.FundingRepository;
import app.giftify.funding.domain.exception.FundingErrorCode;
import app.giftify.funding.domain.exception.FundingException;
import app.giftify.shared.domain.port.FundingQueryPort;
import app.giftify.shared.domain.vo.FundingInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FundingQueryAdapter implements FundingQueryPort {
    private final FundingRepository fundingRepository;

    @Override
    public Map<Long, FundingInfo> findFundingInfoByWishlistItemIds(List<Long> wishlistItemIds) {
        return fundingRepository.findAllByWishlistItemIdIn(wishlistItemIds)
                .stream()
                .collect(Collectors.toMap(
                        Funding::getWishlistItemId,
                        funding -> new FundingInfo(
                                funding.getWishlistItemId(),
                                funding.getCurrentAmount(),
                                funding.getTargetAmount() - funding.getCurrentAmount()
//                                ,funding.getTargetAmount(),
//                                (double) funding.getCurrentAmount() / funding.getTargetAmount(),
//                                funding.getDeadline()
                        )
                ));
    }

    @Override
    public FundingInfo findFundingInfoByWishlistItemId(Long wishlistItemId) {
        Funding funding = fundingRepository.findActiveByWishlistItemId(wishlistItemId)
                .orElseThrow(() -> new FundingException(FundingErrorCode.NOT_IN_PROGRESS));

        return new FundingInfo(
                funding.getWishlistItemId(),
                funding.getCurrentAmount(),
                funding.getTargetAmount() - funding.getCurrentAmount());
    }
}
