package app.giftify.funding.application;

import app.giftify.funding.adpater.outbound.jpa.Funding;
import app.giftify.funding.adpater.outbound.repository.FundingParticipantMemberRepository;
import app.giftify.funding.adpater.outbound.repository.FundingRepository;
import app.giftify.funding.domain.exception.FundingErrorCode;
import app.giftify.funding.domain.exception.FundingException;
import app.giftify.shared.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WithdrawFundingUseCase {
    private final FundingRepository fundingRepository;
    private final FundingParticipantMemberRepository fundingParticipantMemberRepository;

    @Transactional
    public void withdrawFunding(Long fundingId, Long participantId, Money amount) {
        // 진행중이거나 달성 상태일 때만 가능 -> 도메인 내 메서드에서 처리
        Funding funding = fundingRepository.findById(fundingId).
                orElseThrow(() -> new FundingException(FundingErrorCode.FUNDING_NOT_FOUND, fundingId));

        funding.withdraw(amount.toBigDecimalValue().intValue());
        fundingParticipantMemberRepository.deleteByFundingIdAndParticipantId(fundingId, participantId);
    }
}
