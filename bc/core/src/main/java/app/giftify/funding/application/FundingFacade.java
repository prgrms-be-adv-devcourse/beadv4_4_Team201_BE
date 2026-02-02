package app.giftify.funding.application;

import app.giftify.funding.adpater.outbound.jpa.Funding;
import app.giftify.funding.adpater.outbound.jpa.FundingWishlistItem;
import app.giftify.funding.adpater.inbound.dto.FundingCompleteResponseDto;
import app.giftify.funding.adpater.inbound.dto.FundingResponseDto;
import app.giftify.funding.adpater.inbound.dto.MyFundingResponseDto;
import app.giftify.funding.adpater.inbound.dto.WishlistItemDto;
import app.giftify.funding.adpater.outbound.repository.FundingRepository;
import app.giftify.shared.api.paging.PageResponse;
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


    @Transactional
    public FundingResponseDto startFunding(Long wishlistItemId, Long participantId, Integer productPrice, Integer amount) {
        // Funding 생성 (기여금 0원의 빈 펀딩)
        Funding funding = fundingCreateUseCase.createFunding(wishlistItemId, productPrice);

        // 펀딩 기여(기여금, 기여자 추가)
        fundingContributeUseCase.contribute(wishlistItemId, participantId, amount);

        return FundingResponseDto.fromEntity(funding);
    }

    @Transactional
    public void contributeFunding(Long fundingId, Long participantId, Integer amount) {
        fundingContributeUseCase.contribute(fundingId, participantId, amount);
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
    public MyFundingResponseDto getParticipatedFunding(Long fundingId, Long memberId) {
        return fundingGetUseCase.getParticipatedFunding(fundingId, memberId);
    }

    @Transactional(readOnly = true)
    public PageResponse<MyFundingResponseDto> getParticipatedFundings(int page, int size, Long memberId) {
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

//    @Transactional
//    public List<FundingCompleteResponseDto> closeAchievedFundings() {
//        return fundingCloseUseCase.closeUnacceptedAchievedFundings();
//    }
}
