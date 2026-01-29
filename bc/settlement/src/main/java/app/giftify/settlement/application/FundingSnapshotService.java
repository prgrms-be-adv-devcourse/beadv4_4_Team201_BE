package app.giftify.settlement.application;

import app.giftify.settlement.adapter.outbound.jpa.repository.FundingSnapshotRepository;
import app.giftify.settlement.domain.FundingSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FundingSnapshotService {
    private final FundingSnapshotRepository fundingSnapshotRepository;

    @Transactional
    public FundingSnapshot save(FundingSnapshot snapshot) {
        return fundingSnapshotRepository.save(snapshot);
    }
}
