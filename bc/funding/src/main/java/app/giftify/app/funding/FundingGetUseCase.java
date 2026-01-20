package app.giftify.app.funding;

import app.giftify.domain.funding.Funding;
import app.giftify.domain.funding.FundingErrorCode;
import app.giftify.domain.funding.FundingException;
import app.giftify.domain.funding.FundingStatus;
import app.giftify.in.funding.FundingResponseDto;
import app.giftify.out.FundingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FundingGetUseCase {

    private final FundingRepository fundingRepository;

    @Transactional(readOnly = true)
    public FundingResponseDto getFunding(Long id) {
        Funding funding = fundingRepository.findById(id).orElseThrow(() ->
                new FundingException(FundingErrorCode.FUNDING_NOT_FOUND, "펀딩을 찾을 수 없습니다. ID: " + id)
        );

        // 진행 중이거나 목표 달성한 펀딩만 조회 가능
        if (funding.getStatus() != FundingStatus.IN_PROGRESS
            && funding.getStatus() != FundingStatus.ACHIEVED) {
            throw new FundingException(
                FundingErrorCode.NOT_IN_PROGRESS, "진행 중이거나 목표 달성한 펀딩만 조회할 수 있습니다");
        }

        return FundingResponseDto.fromEntity(funding);
    }
}
