package app.giftify.funding.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.giftify.funding.adapter.outbound.jpa.Funding;
import app.giftify.funding.adapter.outbound.repository.FundingRepository;
import app.giftify.funding.domain.exception.FundingErrorCode;
import app.giftify.funding.domain.exception.FundingException;
import app.giftify.support.common.event.EventPublisher;
import app.giftify.funding.domain.event.FundingFailAcceptEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FundingFailAcceptUseCase {
	private static final Logger log = LoggerFactory.getLogger(FundingFailAcceptUseCase.class);

    private final FundingRepository fundingRepository;
    private final EventPublisher eventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void execute(Long fundingId) {
        Funding funding = fundingRepository.findById(fundingId)
                .orElseThrow(() -> new FundingException(FundingErrorCode.FUNDING_NOT_FOUND, fundingId));
        funding.markAcceptFailed();

        eventPublisher.publish(new FundingFailAcceptEvent(funding.getId(), funding.getReceiverId()));

        log.info("[Funding] 펀딩 수락 실패 처리 완료. fundingId={}", fundingId);
    }
}
