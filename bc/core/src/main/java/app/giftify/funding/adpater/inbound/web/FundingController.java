package app.giftify.funding.adpater.inbound.web;

import app.giftify.funding.adpater.inbound.dto.*;
import app.giftify.funding.application.FundingFacade;
import app.giftify.security.common.CurrentMemberId;
import app.giftify.shared.api.paging.PageResponse;
import app.giftify.shared.api.response.RsData;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/fundings")
public class FundingController implements FundingV2ApiSpec {

    private final FundingFacade fundingFacade;

    // 전체공개 펀딩 단건 조희
    @Override
    @GetMapping("/{id}")
    public ResponseEntity<RsData<FundingResponseDto>> getFunding(@PathVariable("id") Long id) {
        FundingResponseDto funding = fundingFacade.getFunding(id);
        return ResponseEntity.ok(RsData.success(funding));
    }

    // 전체공개 펀딩 목록 조회
    @Override
    @GetMapping("/list")
    public ResponseEntity<RsData<PageResponse<FundingResponseDto>>> getFundings(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        PageResponse<FundingResponseDto> fundings = fundingFacade.getFundings(page, size);
        return ResponseEntity.ok(RsData.success(fundings));
    }

    // 펀딩 종료 처리
    @Override
    @PutMapping("/{id}/close")
    public ResponseEntity<RsData<FundingCompleteResponseDto>> closeFunding(@PathVariable("id") Long id) {
        // TODO: 관리자 권한 체크 필요
        FundingCompleteResponseDto funding = fundingFacade.closeFunding(id);
        return ResponseEntity.ok(RsData.success(funding));
    }

    // 펀딩 만료 처리
    @Override
    @PutMapping("/{id}/expire")
    public ResponseEntity<RsData<FundingCompleteResponseDto>> expireFunding(@PathVariable("id") Long id) {
        // TODO: 관리자 권한 체크 필요
        FundingCompleteResponseDto funding = fundingFacade.expireFunding(id);
        return ResponseEntity.ok(RsData.success(funding));
    }

    // 참여한 펀딩 단건 조회
    @Override
    @GetMapping("/participated/{id}")
    public ResponseEntity<RsData<ContributeFundingResponseDto>> getParticipatedFunding(
            @PathVariable("id") Long id,
            @Parameter(hidden = true) @CurrentMemberId Long memberId
    ) {
        ContributeFundingResponseDto funding = fundingFacade.getParticipatedFunding(id, memberId);
        return ResponseEntity.ok(RsData.success(funding));
    }

    // 참여한 펀딩 목록 조회
    @Override
    @GetMapping("/participated/list")
    public ResponseEntity<RsData<PageResponse<ContributeFundingResponseDto>>> getParticipatedFundings(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(hidden = true) @CurrentMemberId Long memberId
    ) {
        PageResponse<ContributeFundingResponseDto> fundings = fundingFacade.getParticipatedFundings(page, size, memberId);
        return ResponseEntity.ok(RsData.success(fundings));
    }

    // 펀딩 거절
    @Override
    @PostMapping("/{id}/refuse")
    public ResponseEntity<RsData<FundingCompleteResponseDto>> refuseFunding(
            @PathVariable("id") Long id,
            @Parameter(hidden = true) @CurrentMemberId Long memberId) {
        FundingCompleteResponseDto funding = fundingFacade.refuseFunding(id, memberId);
        return ResponseEntity.ok(RsData.success(funding));
    }

    // 펀딩 수락
    @Override
    @PostMapping("/{id}/accept")
    public ResponseEntity<RsData<FundingCompleteResponseDto>> acceptFunding(
            @PathVariable("id") Long id,
            @Parameter(hidden = true) @CurrentMemberId Long memberId) {
        FundingCompleteResponseDto funding = fundingFacade.acceptFunding(id, memberId);
        return ResponseEntity.ok(RsData.success(funding));
    }

    // 나의 펀딩 단건 조회
    @Override
    @GetMapping("/my/{id}")
    public ResponseEntity<RsData<MyFundingResponseDto>> getMyFunding(
            @PathVariable("id") Long id,
            @Parameter(hidden = true) @CurrentMemberId Long memberId) {
        MyFundingResponseDto funding = fundingFacade.getMyFunding(id, memberId);
        return ResponseEntity.ok(RsData.success(funding));
    }

    // 나의 펀딩 목록 조회
    @Override
    @GetMapping("/my/list")
    public ResponseEntity<RsData<PageResponse<MyFundingSummaryDto>>> getMyFundings(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(hidden = true) @CurrentMemberId Long memberId) {
        PageResponse<MyFundingSummaryDto> fundings = fundingFacade.getMyFundings(page, size, memberId);
        return ResponseEntity.ok(RsData.success(fundings));
    }
}
