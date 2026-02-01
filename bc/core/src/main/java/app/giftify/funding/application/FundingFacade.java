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
    private final FundingRepository fundingRepository;
    private final FundingCreateUseCase fundingCreateUseCase;
    private final FundingSyncItemUseCase fundingSyncItemUseCase;
    private final FundingGetUseCase fundingGetUseCase;
    private final FundingCloseUseCase fundingCloseUseCase;
    private final FundingExpireUseCase fundingExpireUseCase;
    private final FundingContributeUseCase fundingContributeUseCase;
    private final FundingRefuseUseCase fundingRefuseUseCase;
    private final FundingAcceptUseCase fundingAcceptUseCase;

    @Transactional
    public void handleFundingFromOrder(FundingFromOrderCommand command) {
        // 진행중인 펀딩인지 확인
        Optional<Funding> fundingOpt = fundingRepository.findByWishlistItemId(command.wishlistItemId());

        if (fundingOpt.isPresent()) {
            // 진행 중인 펀딩이면 기여
            fundingContributeUseCase.contribute(fundingOpt.get().getId(), command.participantId(), command.amount());
            return;
        }

        // 아직 시작 안됨 -> 펀딩 시작
        FundingWishlistItem syncedItem =
                fundingSyncItemUseCase.syncItem(
                        new WishlistItemDto(
                                command.wishlistItemId(),
                                command.receiverId(),
                                command.amount()
                        )
                );

        fundingCreateUseCase.createFunding(syncedItem.getId(),command.amount());

        // 첫 기여자도 기록 -> creatFunding이랑 중복되는 로직 없는지 확인하기
        fundingContributeUseCase.contribute(fundingOpt.get().getId(), command.participantId(), command.amount());
    }

    @Transactional
    public FundingResponseDto startFunding(WishlistItemDto wishlistItemDto, Integer amount) {
        // 1. WishlistItem 복제
        FundingWishlistItem syncedItem = fundingSyncItemUseCase.syncItem(wishlistItemDto);

        // 2. Funding 생성 (첫 결제 금액으로)
        Funding funding = fundingCreateUseCase.createFunding(syncedItem.getId(), amount);

        return FundingResponseDto.fromEntity(funding);
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
