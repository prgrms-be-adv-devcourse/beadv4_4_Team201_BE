package app.giftify.funding.application;

import app.giftify.funding.adapter.inbound.dto.FundingContributeRequest;
import app.giftify.funding.adapter.outbound.jpa.Funding;
import app.giftify.replica.MemberReplica;
import app.giftify.funding.adapter.outbound.jpa.FundingParticipantMember;
import app.giftify.replica.MemberReplicaRepository;
import app.giftify.funding.adapter.outbound.repository.FundingParticipantMemberRepository;
import app.giftify.funding.domain.exception.FundingErrorCode;
import app.giftify.funding.domain.exception.FundingException;
import app.giftify.funding.adapter.outbound.repository.FundingRepository;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.funding.FundingAchievedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FundingContributeUseCase {
    private final FundingRepository fundingRepository;
    private final FundingParticipantMemberRepository fundingParticipantMemberRepository;
    private final MemberReplicaRepository memberReplicaRepository;
    private final EventPublisher eventPublisher;


    public List<Funding> contribute(List<FundingContributeRequest> requests, Long participantId) {

        List<Funding> result = new ArrayList<>();

        List<Long> fundingIds = requests.stream()
                .map(FundingContributeRequest::fundingId)
                .collect(Collectors.toList());

        Map<Long, Funding> fundingMap = fundingRepository.findAllById(fundingIds).stream()
                .collect(Collectors.toMap(Funding::getId, funding -> funding));

        // 닉네임 조회
        String nickName = memberReplicaRepository.findById(participantId)
                .map(MemberReplica::getNickname)
                .orElse("Unknown");

        for (FundingContributeRequest request : requests) {
            Funding funding = fundingMap.get(request.fundingId());
            if (funding == null) {
                throw new FundingException(FundingErrorCode.FUNDING_NOT_FOUND);
            }

            // 참여자 존재여부 확인 및 추가
            FundingParticipantMember member = fundingParticipantMemberRepository.findByFundingAndParticipantId(funding, participantId);

            if (member == null) {
                member = new FundingParticipantMember(funding, participantId, nickName, request.amount());
                fundingParticipantMemberRepository.save(member);
            } else {
                member.addAmount(request.amount());
            }

            boolean achievedNow = funding.contribute(request.amount());

            // 달성된 경우 이벤트 발행
            if (achievedNow) {
                // 이벤트 발행 시점 스냅샷
                List<Long> participantIds = fundingParticipantMemberRepository.findIdsByFundingId((funding.getId()));

                eventPublisher.publish(new FundingAchievedEvent(
                        funding.getId(),
                        funding.getWishlistItemId(),
                        funding.getReceiverId(),
                        participantIds

                ));
            }
            result.add(funding);
        }
        return result;
    }
}
