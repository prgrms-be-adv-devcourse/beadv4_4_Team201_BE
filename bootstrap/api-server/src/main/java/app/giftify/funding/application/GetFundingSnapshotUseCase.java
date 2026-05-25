package app.giftify.funding.application;

import app.giftify.funding.adapter.outbound.repository.FundingRepository;
import app.giftify.funding.domain.type.FundingStatus;
import app.giftify.funding.domain.vo.FundingSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetFundingSnapshotUseCase {

    private final FundingRepository fundingRepository;

    public Optional<FundingSnapshot> getSnapshot(Long wishlistItemId) {
        return fundingRepository.findByWishlistItemIdAndStatus(wishlistItemId, FundingStatus.IN_PROGRESS)
                .map(funding -> new FundingSnapshot(funding.getId(), funding.getWishlistItemId()));
    }

    public Map<Long, Long> getFundingSnapshotsByWishlistItemIds(List<Long> wishlistItemIds) {
        return fundingRepository.findAllByWishlistItemIdIn(wishlistItemIds).stream()
                .filter(funding -> funding.getStatus() == FundingStatus.IN_PROGRESS)
                .collect(Collectors.toMap(
                        funding -> funding.getWishlistItemId(),
                        funding -> funding.getId()
                ));
    }
}
