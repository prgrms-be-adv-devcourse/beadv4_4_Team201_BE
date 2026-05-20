package app.giftify.funding.application;

import app.giftify.funding.adapter.inbound.FundingCreateResult;
import app.giftify.funding.adapter.inbound.dto.*;
import app.giftify.order.domain.OrderSnapshot;
import app.giftify.shared.api.paging.PageResponse;
import app.giftify.shared.domain.type.FundingStatus;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.FundingSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


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
    private final FundingRetryAcceptUseCase fundingRetryAcceptUseCase;

    // todo : 쓰이는 곳에서 아래 Map으로 바뀌면 제거 예정
    @Transactional(readOnly = true)
    public Optional<FundingSnapshot> getSnapshot(Long wishlistItemId) {
        return getFundingSnapshotUseCase.getSnapshot(wishlistItemId);
    }

    @Transactional(readOnly = true)
    public Map<Long, Long> getFundingIdMapByWishlistItemIds(List<Long> wishlistItemIds) {
        return getFundingSnapshotUseCase.getFundingSnapshotsByWishlistItemIds(wishlistItemIds);
    }

    /**
     * OrderSnapshot을 받아 펀딩 생성&기여
     */
    public void processFundingActions(OrderSnapshot orderSnapshot) {

        List<FundingContributeRequest> allContributeRequests = new ArrayList<>();

        // 1. 펀딩 생성 + 초기 기여 요청 수집
        if (orderSnapshot.orderItemSnapshots().stream()
                .anyMatch(item -> item.targetType() == TargetType.FUNDING_PENDING)) {

            List<FundingCreateResult> createdFundings = fundingCreateUseCase.createFunding(orderSnapshot);

            // 생성된 펀딩의 초기 기여 요청 추가
            allContributeRequests.addAll(
                    createdFundings.stream()
                            .map(result -> new FundingContributeRequest(
                                    result.funding().getId(),
                                    result.orderItemSnapshot().amount().amount().intValueExact()
                            ))
                            .toList()
            );
        }

        // 2. 기존 펀딩 기여 요청 추가
        allContributeRequests.addAll(
                orderSnapshot.orderItemSnapshots().stream()
                        .filter(item -> item.targetType() == TargetType.FUNDING)
                        .map(item -> new FundingContributeRequest(
                                item.targetId(),
                                item.amount().amount().intValueExact()
                        ))
                        .toList()
        );

        // 3. 모든 기여를 한 번에 처리
        if (!allContributeRequests.isEmpty()) {
            fundingContributeUseCase.contribute(allContributeRequests, orderSnapshot.buyerId());
        }
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
    public PageResponse<ContributeFundingResponseDto> getParticipatedFundings(int page, int size, Long memberId, FundingStatus status) {
        return fundingGetUseCase.getParticipatedFundings(page, size, memberId, status);
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
    public FundingCompleteResponseDto requestFundingAcceptance(Long id, Long memberId) {
        return fundingAcceptUseCase.requestFundingAcceptance(id, memberId);
    }

    @Transactional(readOnly = true)
    public MyFundingResponseDto getMyFunding(Long id, FundingStatus status, Long memberId) {
        return fundingGetUseCase.getMyFunding(id, status, memberId);
    }

    @Transactional(readOnly = true)
    public PageResponse<MyFundingSummaryDto> getMyFundings(int page, int size, FundingStatus status, Long memberId) {
        return fundingGetUseCase.getMyFundings(page, size, status, memberId);
    }

    @Transactional(readOnly = true)
    public PageResponse<FundingResponseDto> getFriendFundings(int page, int size, Long memberId, Long friendId) {
        return fundingGetUseCase.getFriendFundings(page, size, memberId, friendId);
    }

    @Transactional(readOnly = true)
    public FundingResponseDto getFriendFunding(Long friendId, Long id, Long memberId) {
        return fundingGetUseCase.getFriendFunding(friendId, id, memberId);
    }

    @Transactional
    public List<FundingCompleteResponseDto> closeAchievedFundings() {
        return fundingCloseUseCase.closeUnacceptedAchievedFundings();
    }

    @Transactional(readOnly = true)
    public PageResponse<FundingResponseDto> getFriendsFundings(int page, int size, Long memberId) {
        return fundingGetUseCase.getFriendsFundings(page, size, memberId);
    }

    @Transactional
    public FundingCompleteResponseDto retryAcceptFunding(Long id, Long memberId) {
        return fundingRetryAcceptUseCase.retryAccept(id, memberId);
    }
}
