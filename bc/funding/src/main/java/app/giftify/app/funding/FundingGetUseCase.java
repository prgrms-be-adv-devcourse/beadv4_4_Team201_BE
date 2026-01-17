package app.giftify.app.funding;

import app.giftify.domain.funding.Funding;
import app.giftify.domain.funding.FundingErrorCode;
import app.giftify.domain.funding.FundingException;
import app.giftify.domain.funding.FundingStatus;
import app.giftify.in.funding.FundingResponseDto;
import app.giftify.out.FundingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FundingGetUseCase {

    private final FundingRepository fundingRepository;

    public FundingResponseDto getFunding(Long id) {
        // 있는 펀딩인지 조회
        Funding funding = fundingRepository.findById(id).orElseThrow(() ->
                new FundingException(FundingErrorCode.FUNDING_NOT_FOUND, "펀딩을 찾을 수 없습니다. ID: " + id)
        );

        // 진행 중인 펀딩인지 조회
        if (funding.getStatus() != FundingStatus.IN_PROGRESS) {
            throw new FundingException(FundingErrorCode.NOT_IN_PROGRESS, "진행 중인 펀딩만 조회할 수 있습니다.");
        }

        return FundingResponseDto.fromEntity(funding);
    }
}
