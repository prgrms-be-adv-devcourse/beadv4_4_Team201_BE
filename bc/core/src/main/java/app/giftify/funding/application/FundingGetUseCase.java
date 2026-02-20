package app.giftify.funding.application;

import app.giftify.funding.adpater.inbound.dto.ContributeFundingResponseDto;
import app.giftify.funding.adpater.inbound.dto.FundingResponseDto;
import app.giftify.funding.adpater.inbound.dto.MyFundingResponseDto;
import app.giftify.funding.adpater.inbound.dto.MyFundingSummaryDto;
import app.giftify.funding.adpater.outbound.jpa.Funding;
import app.giftify.funding.adpater.outbound.jpa.FundingParticipantMember;
import app.giftify.funding.adpater.outbound.repository.FundingParticipantMemberRepository;
import app.giftify.funding.adpater.outbound.repository.FundingRepository;
import app.giftify.funding.domain.FundingStatus;
import app.giftify.funding.domain.exception.FundingErrorCode;
import app.giftify.funding.domain.exception.FundingException;
import app.giftify.replica.MemberReplica;
import app.giftify.replica.MemberReplicaRepository;
import app.giftify.shared.api.paging.PageResponse;
import app.giftify.shared.domain.port.FriendshipVerificationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FundingGetUseCase {

    private final FundingRepository fundingRepository;
    private final FundingParticipantMemberRepository participantMemberRepository;
    private final MemberReplicaRepository memberReplicaRepository;
    private final FriendshipVerificationPort friendshipVerificationPort;

    /**
     * 전체 공개 단일 펀딩 조회
     */
    public FundingResponseDto getFunding(Long id) {
        Funding funding = fundingRepository.findById(id).orElseThrow(() ->
                new FundingException(FundingErrorCode.FUNDING_NOT_FOUND, id));

        MemberReplica receiver = memberReplicaRepository.findById(funding.getReceiverId()).orElseThrow(()->
                new FundingException(FundingErrorCode.RECEIVER_NOT_FOUND));

        // 진행 중이거나 목표 달성한 펀딩만 조회 가능
        if (funding.getStatus() != FundingStatus.IN_PROGRESS
            && funding.getStatus() != FundingStatus.ACHIEVED) {
            throw new FundingException(
                FundingErrorCode.NOT_IN_PROGRESS, "진행 중이거나 목표 달성한 펀딩만 조회할 수 있습니다");
        }

        return FundingResponseDto.fromEntity(funding, receiver.getNickname());
    }

    /**
     * 전체 공개 펀딩 리스트 조회
     */
    public PageResponse<FundingResponseDto> getFundings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        List<FundingStatus> statuses = List.of(FundingStatus.IN_PROGRESS, FundingStatus.ACHIEVED);
        Page<Funding> fundingPage = fundingRepository.findAllByStatusIn(statuses, pageable);
        List<Funding> fundings = fundingPage.getContent();

        Set<Long> receiverIds = fundings.stream()
                .map(Funding::getReceiverId)
                .collect(Collectors.toSet());

        Map<Long, MemberReplica> receivers = memberReplicaRepository.findAllById(receiverIds).stream()
                .collect(Collectors.toMap(MemberReplica::getId, r -> r));

        List<FundingResponseDto> content = fundings.stream()
            .map(funding -> {
                String receiverNickname = Optional.ofNullable(receivers.get(funding.getReceiverId()))
                        .map(MemberReplica::getNickname)
                        .orElse("알 수 없음");
                return FundingResponseDto.fromEntity(funding, receiverNickname);
            })
            .collect(Collectors.toList());

        return PageResponse.of(content, page, size, fundingPage.getTotalElements());
    }

    /**
     * 내가 참여한 펀딩 단건 조회
     */
    public ContributeFundingResponseDto getParticipatedFunding(Long fundingId, Long memberId) {
        Funding funding = fundingRepository.findById(fundingId).orElseThrow(() ->
                new FundingException(FundingErrorCode.FUNDING_NOT_FOUND, + fundingId)
        );

        MemberReplica receiver = memberReplicaRepository.findById(funding.getReceiverId()).orElseThrow(()->
                new FundingException(FundingErrorCode.RECEIVER_NOT_FOUND));

        // 참여 여부 확인
        boolean isParticipated = participantMemberRepository.existsByFundingIdAndParticipantId(fundingId, memberId);
        if (!isParticipated) { throw new FundingException(FundingErrorCode.FORBIDDEN); }

        // 참여 금액 조회
        Integer myContribution = participantMemberRepository.findTotalAmountByFundingIdAndParticipantId(fundingId, memberId)
                .orElse(0);

        return ContributeFundingResponseDto.fromEntity(funding, myContribution, receiver.getNickname());
    }

    /**
     * 내가 참여한 펀딩 목록 조회
     */
    public PageResponse<ContributeFundingResponseDto> getParticipatedFundings(int page, int size, Long memberId, FundingStatus status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<MyFundingInfo> myFundingInfoPage = participantMemberRepository.findAllMyFundingInfos(memberId, status, pageable);
        List<MyFundingInfo> myFundingInfos = myFundingInfoPage.getContent();

        Set<Long> receiverIds = myFundingInfos.stream()
                .map(info -> info.funding().getReceiverId())
                .collect(Collectors.toSet());

        Map<Long, MemberReplica> receivers = memberReplicaRepository.findAllById(receiverIds).stream()
                .collect(Collectors.toMap(MemberReplica::getId, r-> r));

        List<ContributeFundingResponseDto> contents =
                myFundingInfos.stream()
                        .map(info -> {
                            Funding funding = info.funding();
                            String receiverNickname = Optional.ofNullable(receivers.get(funding.getReceiverId()))
                                    .map(MemberReplica::getNickname)
                                    .orElse("알 수 없음");

                            return ContributeFundingResponseDto.fromEntity(funding, info.myContribution(), receiverNickname);
                        })
                        .collect(Collectors.toList());

        return PageResponse.of(contents, page, size, myFundingInfoPage.getTotalElements());
    }

    /**
     * 나의 펀딩 단건 조회
     */
    public MyFundingResponseDto getMyFunding(Long id, FundingStatus status, Long memberId) {
        Funding funding = fundingRepository.findByIdAndStatus(id, status).orElseThrow(() ->
                new FundingException(FundingErrorCode.FUNDING_NOT_FOUND, + id));

        funding.validateReceiver(memberId);

        if (funding.isAchieved()) {
            List<FundingParticipantMember> participants = participantMemberRepository.findByFundingId(id);

            return MyFundingResponseDto.fromAchievedFunding(funding, participants);
        }

        return MyFundingResponseDto.fromEntity(funding);
    }

    /**
     * 나의 펀딩 리스트 조회
     */
    public PageResponse<MyFundingSummaryDto> getMyFundings(int page, int size, FundingStatus status, Long memberId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Funding> fundingPage = fundingRepository.findAllByReceiverIdAndStatus(memberId, status, pageable);
        List<MyFundingSummaryDto> contents = fundingPage.getContent().stream()
                .map(MyFundingSummaryDto::fromEntity)
                .collect(Collectors.toList());

        return PageResponse.of(contents, page, size, fundingPage.getTotalElements());
    }

    /**
     * productId로 진행 중/수락 대기 중인 펀딩 존재여부 조회
     */
    @Transactional(readOnly = true)
    public boolean checkFundingExistsByProductId(Long productId) {
        return fundingRepository.existsByProductIdAndStatusIn(productId, List.of(FundingStatus.IN_PROGRESS, FundingStatus.ACHIEVED));
    }

    /**
     * 특정 친구의 진행 중인 펀딩 리스트 조회
     */
    public PageResponse<FundingResponseDto> getFriendFundings(int page, int size, Long memberId, Long friendId) {
        if (!friendshipVerificationPort.areFriends(memberId, friendId)) {
            throw new FundingException(FundingErrorCode.FORBIDDEN, "친구 관계가 아닙니다.");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Funding> fundingPage = fundingRepository.findAllByReceiverIdAndStatusIn(friendId, List.of(FundingStatus.IN_PROGRESS), pageable);
        List<Funding> fundings = fundingPage.getContent();

        MemberReplica friend = memberReplicaRepository.findById(friendId).orElseThrow(() ->
                new FundingException(FundingErrorCode.RECEIVER_NOT_FOUND));

        List<FundingResponseDto> content = fundings.stream()
                .map(funding -> FundingResponseDto.fromEntity(funding, friend.getNickname()))
                .collect(Collectors.toList());

        return PageResponse.of(content, page, size, fundingPage.getTotalElements());
    }

    /**
     * 특정 친구의 진행 중인 단건 펀딩 조회
     */
    public FundingResponseDto getFriendFunding(Long friendId, Long id, Long memberId) {
        if (!friendshipVerificationPort.areFriends(memberId, friendId)) {
            throw new FundingException(FundingErrorCode.FORBIDDEN, "친구 관계가 아닙니다.");
        }

        Funding funding = fundingRepository.findByIdAndReceiverIdAndStatus(id, friendId, FundingStatus.IN_PROGRESS)
                .orElseThrow(()-> new FundingException(FundingErrorCode.FUNDING_NOT_FOUND));

        MemberReplica friend = memberReplicaRepository.findById(friendId).orElseThrow(()->
                new FundingException(FundingErrorCode.RECEIVER_NOT_FOUND));

        return FundingResponseDto.fromEntity(funding, friend.getNickname());
    }

    /**
     * 내 친구들의 진행 중인 펀딩 리스트 조회
     */
    public PageResponse<FundingResponseDto> getFriendsFundings(int page, int size, Long memberId) {
        List<Long> friendIds = friendshipVerificationPort.getFriendIds(memberId);
        if (friendIds.isEmpty()) {
            return PageResponse.of(Collections.emptyList(), page, size, 0);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Funding> fundingPage = fundingRepository.findAllByReceiverIdInAndStatus(friendIds, FundingStatus.IN_PROGRESS, pageable);
        List<Funding> fundings = fundingPage.getContent();

        Set<Long> receiverIds = fundings.stream()
                .map(Funding::getReceiverId)
                .collect(Collectors.toSet());

        Map<Long, MemberReplica> receivers = memberReplicaRepository.findAllById(receiverIds).stream()
                .collect(Collectors.toMap(MemberReplica::getId, r -> r));

        List<FundingResponseDto> content = fundings.stream()
                .map(funding -> {
                    String receiverNickname = Optional.ofNullable(receivers.get(funding.getReceiverId()))
                            .map(MemberReplica::getNickname)
                            .orElse("알 수 없음");
                    return FundingResponseDto.fromEntity(funding, receiverNickname);
                })
                .collect(Collectors.toList());

        return PageResponse.of(content, page, size, fundingPage.getTotalElements());
    }
}
