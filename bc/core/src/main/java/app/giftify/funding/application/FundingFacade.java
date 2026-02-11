package app.giftify.funding.application;

import app.giftify.funding.adpater.inbound.dto.*;
import app.giftify.funding.adpater.outbound.jpa.Funding;
import app.giftify.orderDemo.domain.OrderSnapshot;
import app.giftify.shared.api.paging.PageResponse;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.FundingSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class FundingFacade {
    private final FundingCreateUseCase fundingCreateUseCase;
    private final FundingGetUseCase fundingGetUseCase;
    private final FundingCloseUseCase fundingCloseUseCase;
    private final FundingExpireUseCase fundingExpireUseCase;
    private final FundingContributeUseCase fundingContributeUseCase;
    private final FundingRefuseUseCase fundingRefuseUseCase;
    private final FundingAcceptUseCase fundingAcceptUseCase;
    private final GetFundingSnapshotUseCase getFundingSnapshotUseCase;

    // todo : 쓰이는 곳에서 아래 Map으로 바뀌면 제거 예정
    @Transactional(readOnly = true)
    public Optional<FundingSnapshot> getSnapshot(Long wishlistItemId) {
        return getFundingSnapshotUseCase.getSnapshot(wishlistItemId);
    }

    @Transactional(readOnly = true)
    public Map<Long, Long> getFundingIdMapByWishlistItemIds(List<Long> wishlistItemIds) {
        return getFundingSnapshotUseCase.getFundingSnapshotsByWishlistItemIds(wishlistItemIds);
    }

    @Transactional
    public void processFundingActions(OrderSnapshot orderSnapshot) {
        // 펀딩 생성 처리 -> OrderSnapshot에 포함된 주문 아이템들 중 'FUNDING_PENDING' 타입이 하나라도 있는지 확인
        if (orderSnapshot.orderItemSnapshots().stream().anyMatch(item -> item.targetType() == TargetType.FUNDING_PENDING)) {
            fundingCreateUseCase.createFunding(orderSnapshot);
        }

        // 펀딩 기여 처리
        List<FundingContributeRequest> contributeRequests = orderSnapshot.orderItemSnapshots().stream()
                .filter(item -> item.targetType() == TargetType.FUNDING)
                .map(item -> new FundingContributeRequest(item.targetId(), item.amount().amount().intValueExact()))
                .collect(Collectors.toList());

        // 'FUNDING' 타입의 아이템이 하나 이상 있어서 기여 요청 리스트가 비어있지 않다면 펀딩 기여 메서드 호출
        if (!contributeRequests.isEmpty()) {
            fundingContributeUseCase.contribute(contributeRequests, orderSnapshot.buyerId());
        }
    }

    @Transactional
    public List<Funding> contributeFunding(List<FundingContributeRequest> requests, Long participantId) {
        return fundingContributeUseCase.contribute(requests, participantId);
    }

    @Transactional(readOnly = true)
    public FundingResponseDto getFunding(Long id) {
        return fundingGetUseCase.getFunding(id);
    }

    @Transactional(readOnly = true)
    public PageResponse<FundingResponseDto> getFundings(int page, int size) {
        return fundingGetUseCase.getFundings(page, size);
    }

    @Transactional(readOnly = true)
    public ContributeFundingResponseDto getParticipatedFunding(Long fundingId, Long memberId) {
        return fundingGetUseCase.getParticipatedFunding(fundingId, memberId);
    }

    @Transactional(readOnly = true)
    public PageResponse<ContributeFundingResponseDto> getParticipatedFundings(int page, int size, Long memberId) {
        return fundingGetUseCase.getParticipatedFundings(page, size, memberId);
    }

    @Transactional
    public FundingCompleteResponseDto closeFunding(Long id) {
        return fundingCloseUseCase.closeFunding(id);
    }

    @Transactional
    public FundingCompleteResponseDto expireFunding(Long id) {
        return fundingExpireUseCase.expireFunding(id);
    }

    @Transactional
    public List<FundingCompleteResponseDto> expireExpiredFundings(LocalDateTime now ) {
        return fundingExpireUseCase.expireExpiredFundings(now);
    }

    @Transactional
    public FundingCompleteResponseDto refuseFunding(Long id, Long memberId) {
        return fundingRefuseUseCase.refuseFunding(id, memberId);
    }

    @Transactional
    public FundingCompleteResponseDto acceptFunding(Long id, Long memberId) {
        return fundingAcceptUseCase.acceptFunding(id, memberId);
    }

    @Transactional(readOnly = true)
    public MyFundingResponseDto getMyFunding(Long id, Long memberId) {
        return fundingGetUseCase.getMyFunding(id, memberId);
    }

    @Transactional(readOnly = true)
    public PageResponse<MyFundingSummaryDto> getMyFundings(int page, int size, Long memberId) {
        return fundingGetUseCase.getMyFundings(page, size, memberId);
    }

//    @Transactional
//    public List<FundingCompleteResponseDto> closeAchievedFundings() {
//        return fundingCloseUseCase.closeUnacceptedAchievedFundings();
//    }
}
