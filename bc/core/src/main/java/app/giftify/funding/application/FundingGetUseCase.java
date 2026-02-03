package app.giftify.funding.application;

import app.giftify.funding.adpater.outbound.jpa.Funding;
import app.giftify.funding.application.outbound.WishlistItemSnapshotPort;
import app.giftify.funding.domain.exception.FundingErrorCode;
import app.giftify.funding.domain.exception.FundingException;
import app.giftify.shared.domain.type.FundingStatus;
import app.giftify.funding.adpater.inbound.dto.FundingResponseDto;
import app.giftify.funding.adpater.inbound.dto.MyFundingResponseDto;
import app.giftify.funding.adpater.outbound.repository.FundingParticipantMemberRepository;
import app.giftify.funding.adpater.outbound.repository.FundingRepository;
import app.giftify.shared.api.paging.PageResponse;
import app.giftify.shared.domain.vo.WishlistItemSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FundingGetUseCase {

    private final FundingRepository fundingRepository;
    private final FundingParticipantMemberRepository participantMemberRepository;
    private final WishlistItemSnapshotPort wishlistItemSnapshotPort;

    /**
     * 전체 공개 단일 펀딩 조회
     */
    public FundingResponseDto getFunding(Long id) {
        Funding funding = fundingRepository.findById(id).orElseThrow(() ->
                new FundingException(FundingErrorCode.FUNDING_NOT_FOUND, id));

        WishlistItemSnapshot snapshot = wishlistItemSnapshotPort.getSnapshot(funding.getWishlistItemId());


        // 진행 중이거나 목표 달성한 펀딩만 조회 가능
        if (funding.getStatus() != FundingStatus.IN_PROGRESS
            && funding.getStatus() != FundingStatus.ACHIEVED) {
            throw new FundingException(
                FundingErrorCode.NOT_IN_PROGRESS, "진행 중이거나 목표 달성한 펀딩만 조회할 수 있습니다");
        }

        return FundingResponseDto.fromEntity(funding, snapshot);
    }

    /**
     * 전체 공개 펀딩 리스트 조회
     */
    public PageResponse<FundingResponseDto> getFundings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        List<FundingStatus> statuses = List.of(FundingStatus.IN_PROGRESS, FundingStatus.ACHIEVED);
        Page<Funding> fundingPage = fundingRepository.findAllByStatusIn(statuses, pageable);

        List<FundingResponseDto> content = fundingPage.getContent().stream()
            .map(funding -> {
                WishlistItemSnapshot snapshot = wishlistItemSnapshotPort.getSnapshot(funding.getWishlistItemId());
                return FundingResponseDto.fromEntity(funding, snapshot);
            })
            .collect(Collectors.toList());

        return PageResponse.of(content, page, size, fundingPage.getTotalElements());
    }

    /**
     * 내가 참여한 펀딩 단건 조회
     */
    public MyFundingResponseDto getParticipatedFunding(Long fundingId, Long memberId) {
        Funding funding = fundingRepository.findById(fundingId).orElseThrow(() ->
                new FundingException(FundingErrorCode.FUNDING_NOT_FOUND, + fundingId)
        );

        WishlistItemSnapshot snapshot = wishlistItemSnapshotPort.getSnapshot(funding.getWishlistItemId());

        // 참여 여부 확인
        boolean isParticipated = participantMemberRepository.existsByFundingIdAndParticipantId(fundingId, memberId);
        if (!isParticipated) {
            throw new FundingException(FundingErrorCode.FORBIDDEN);
        }

        // 참여 금액 조회
        Integer myContribution = participantMemberRepository.findTotalAmountByFundingIdAndParticipantId(fundingId, memberId)
                .orElse(0);

        return MyFundingResponseDto.fromEntity(funding, myContribution, snapshot);
    }

    /**
     * 내가 참여한 펀딩 목록 조회
     */
    public PageResponse<MyFundingResponseDto> getParticipatedFundings(int page, int size, Long memberId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<MyFundingInfo> myFundingInfoPage = participantMemberRepository.findAllMyFundingInfos(memberId, pageable);

        List<MyFundingResponseDto> contents =
                myFundingInfoPage.getContent().stream()
                        .map(info -> {
                            Funding funding = info.funding();

                            WishlistItemSnapshot snapshot = wishlistItemSnapshotPort.getSnapshot(funding.getWishlistItemId());

                            return MyFundingResponseDto.fromEntity(funding, info.myContribution(), snapshot);
                        })
                        .collect(Collectors.toList());

        return PageResponse.of(contents, page, size, myFundingInfoPage.getTotalElements());
    }
}
