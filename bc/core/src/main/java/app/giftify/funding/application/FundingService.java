package app.giftify.funding.application;

import app.giftify.funding.adpater.outbound.repository.FundingRepository;
import app.giftify.funding.application.inbound.GetFundingSnapshotUseCase;
import app.giftify.shared.domain.vo.FundingSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FundingService implements GetFundingSnapshotUseCase {
    private final FundingRepository fundingRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<FundingSnapshot> getSnapshot(Long wishlistItemId) {
        return fundingRepository.findActiveByWishlistItemId(wishlistItemId)
                .map(funding -> new FundingSnapshot(funding.getId()));
    }
}
