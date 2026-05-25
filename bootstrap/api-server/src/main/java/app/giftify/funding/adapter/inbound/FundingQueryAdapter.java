package app.giftify.funding.adapter.inbound;

import app.giftify.funding.adapter.outbound.jpa.Funding;
import app.giftify.funding.adapter.outbound.repository.FundingRepository;
import app.giftify.funding.domain.port.FundingQueryPort;
import app.giftify.funding.domain.vo.FundingInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
                                funding.getId(),
                                funding.getStatus(),
                                funding.getCurrentAmount(),
                                funding.getTargetAmount() - funding.getCurrentAmount()
//                                ,funding.getTargetAmount(),
//                                (double) funding.getCurrentAmount() / funding.getTargetAmount(),
//                                funding.getDeadline()
                        )
                ));
    }

    @Override
    public Optional<FundingInfo> findFundingInfoByWishlistItemId(Long wishlistItemId) {
       return fundingRepository.findByWishlistItemId(wishlistItemId)
               .map(funding -> new FundingInfo(
                funding.getWishlistItemId(),
                funding.getStatus(),
                funding.getCurrentAmount(),
                funding.getTargetAmount() - funding.getCurrentAmount()
               ));
    }

    @Override
    public Optional<Long> findFundingIdByWishlistItemId(Long wishlistItemId) {
        return fundingRepository.findByWishlistItemId(wishlistItemId)
                .map(Funding::getId);
    }
}
