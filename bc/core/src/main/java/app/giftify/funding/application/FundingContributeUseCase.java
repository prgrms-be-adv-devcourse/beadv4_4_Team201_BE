package app.giftify.funding.application;

import app.giftify.funding.adpater.outbound.jpa.Funding;
import app.giftify.funding.adpater.outbound.jpa.FundingParticipantMember;
import app.giftify.funding.adpater.outbound.repository.FundingParticipantMemberRepository;
import app.giftify.funding.domain.exception.FundingErrorCode;
import app.giftify.funding.domain.exception.FundingException;
import app.giftify.funding.adpater.outbound.repository.FundingRepository;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.funding.FundingAchievedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FundingContributeUseCase {
    private final FundingRepository fundingRepository;
    private final FundingParticipantMemberRepository fundingParticipantMemberRepository;
    private final EventPublisher eventPublisher;

    public void contribute(Long fundingId, Long participantId, Integer amount) {
        Funding funding = fundingRepository.findById(fundingId)
            .orElseThrow(() -> new FundingException(FundingErrorCode.FUNDING_NOT_FOUND));

        // 참여자 존재여부 확인 및 추가
        FundingParticipantMember member = fundingParticipantMemberRepository.findByFundingAndParticipantId(funding, participantId);

        if (member == null) {
            member = new FundingParticipantMember(funding, participantId, amount);
            fundingParticipantMemberRepository.save(member);
        } else {
            member.addAmount(amount);
        }

        funding.contribute(amount);

        // 달성된 경우 이벤트 발행
        if (funding.isAchieved()) {
            eventPublisher.publish(new FundingAchievedEvent(
                    funding.getId(),
                    funding.getWishlistItemId()
            ));
        }
    }
}
