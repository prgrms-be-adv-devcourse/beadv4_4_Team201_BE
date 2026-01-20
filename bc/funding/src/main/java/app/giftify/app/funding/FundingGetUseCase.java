package app.giftify.app.funding;

import app.giftify.domain.funding.Funding;
import app.giftify.domain.funding.FundingErrorCode;
import app.giftify.domain.funding.FundingException;
import app.giftify.domain.funding.FundingStatus;
import app.giftify.in.funding.FundingResponseDto;
import app.giftify.out.FundingRepository;
import app.giftify.shared.api.paging.PageResponse;
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

    /**
     * 전체 공개 단일 펀딩 조회
     */
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

    /**
     * 전체 공개 펀딩 리스트 조회
     */
    public PageResponse<FundingResponseDto> getFundings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        List<FundingStatus> statuses = List.of(FundingStatus.IN_PROGRESS, FundingStatus.ACHIEVED);
        Page<Funding> fundingPage = fundingRepository.findAllByStatusIn(statuses, pageable);

        List<FundingResponseDto> content = fundingPage.getContent().stream()
            .map(FundingResponseDto::fromEntity)
            .collect(Collectors.toList());

        return PageResponse.of(content, page, size, fundingPage.getTotalElements());
    }
}
