package app.giftify.funding.application;

import app.giftify.funding.adpater.inbound.dto.FundingCompleteResponseDto;
import app.giftify.funding.adpater.inbound.dto.FundingContributeRequest;
import app.giftify.funding.adpater.inbound.dto.FundingResponseDto;
import app.giftify.funding.adpater.inbound.dto.MyFundingResponseDto;
import app.giftify.orderDemo.domain.OrderSnapshot;
import app.giftify.funding.adpater.inbound.FundingCreateResult;
import app.giftify.funding.adpater.inbound.dto.*;
import app.giftify.funding.adpater.outbound.jpa.Funding;
import app.giftify.shared.api.paging.PageResponse;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.FundingSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    @Transactional(readOnly = true)
    public Optional<FundingSnapshot> getSnapshot(Long wishlistItemId) {
        return getFundingSnapshotUseCase.getSnapshot(wishlistItemId);
    }

    @Transactional
    public void processFundingActions(OrderSnapshot orderSnapshot) {
        orderSnapshot.orderItemSnapshots().forEach(item -> {
            if (item.targetType() == TargetType.FUNDING_PENDING) {
                fundingCreateUseCase.createFunding(orderSnapshot);
            } else if (item.targetType() == TargetType.FUNDING) {
                contributeFunding(item.targetId(), orderSnapshot.buyerId(), item.amount().amount().intValueExact());
            }
        });
    }

    @Transactional
    public void contributeFunding(Long fundingId, List<FundingContributeRequest> requests) {
        fundingContributeUseCase.contribute(fundingId, requests);
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
    public List<FundingCompleteResponseDto> expireExpiredFundings() {
        return fundingExpireUseCase.expireExpiredFundings();
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
