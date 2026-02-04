package app.giftify.funding.application;

import app.giftify.funding.adpater.outbound.repository.FundingRepository;
import app.giftify.shared.domain.type.FundingStatus;
import app.giftify.shared.domain.vo.FundingSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetFundingSnapshotUseCase {
    private final FundingRepository fundingRepository;

    public Optional<FundingSnapshot> getSnapshot(Long wishlistItemId) {
        return fundingRepository.findByWishlistItemIdAndStatus(wishlistItemId, FundingStatus.IN_PROGRESS)
                .map(funding -> new FundingSnapshot(funding.getId()));
    }
    }