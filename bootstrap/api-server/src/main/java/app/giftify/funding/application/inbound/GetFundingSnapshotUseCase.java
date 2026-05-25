package app.giftify.funding.application.inbound;

import app.giftify.funding.domain.type.FundingStatus;
import app.giftify.funding.domain.vo.FundingSnapshot;

import java.util.Optional;

public interface GetFundingSnapshotUseCase {
    Optional<FundingSnapshot> getSnapshot(Long fundingId, FundingStatus status);
}
