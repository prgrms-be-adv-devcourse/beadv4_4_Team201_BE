package app.giftify.funding.application;

import app.giftify.funding.adpater.inbound.dto.FundingContributeRequest;
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

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FundingContributeUseCase {
    private final FundingRepository fundingRepository;
    private final FundingParticipantMemberRepository fundingParticipantMemberRepository;
    private final EventPublisher eventPublisher;

    public List<Funding> contribute(Long participantId, List<FundingContributeRequest> requests) {
        List<Funding> result = new ArrayList<>();

        for (FundingContributeRequest request : requests) {
            Funding funding = fundingRepository.findById(request.fundingId())
                    .orElseThrow(() -> new FundingException(FundingErrorCode.FUNDING_NOT_FOUND));

            // 참여자 존재여부 확인 및 추가
            FundingParticipantMember member = fundingParticipantMemberRepository.findByFundingAndParticipantId(funding, participantId);

            if (member == null) {
                member = new FundingParticipantMember(funding, participantId, request.amount());
                fundingParticipantMemberRepository.save(member);
            } else {
                member.addAmount(request.amount());
            }

            funding.contribute(request.amount());

            // 달성된 경우 이벤트 발행
            if (funding.isAchieved()) {
                eventPublisher.publish(new FundingAchievedEvent(
                        funding.getId(),
                        funding.getWishlistItemId()
                ));
            }
            result.add(funding);
        }
        return result;
    }
}
