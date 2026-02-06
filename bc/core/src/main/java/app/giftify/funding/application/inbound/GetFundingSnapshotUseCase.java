package app.giftify.funding.application.inbound;

import app.giftify.funding.domain.FundingStatus;
import app.giftify.shared.domain.vo.FundingSnapshot;

import java.util.Optional;

public interface GetFundingSnapshotUseCase {
    Optional<FundingSnapshot> getSnapshot(Long fundingId, FundingStatus status);
}
