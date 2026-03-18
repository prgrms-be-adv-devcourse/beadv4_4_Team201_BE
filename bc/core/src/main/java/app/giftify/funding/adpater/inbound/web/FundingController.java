package app.giftify.funding.adpater.inbound.web;

import app.giftify.funding.adpater.inbound.dto.*;
import app.giftify.funding.application.FundingFacade;
import app.giftify.security.common.CurrentMemberId;
import app.giftify.shared.api.paging.PageResponse;
import app.giftify.shared.api.response.RsData;
import app.giftify.shared.domain.type.FundingStatus;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

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

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/close")
    public ResponseEntity<RsData<FundingCompleteResponseDto>> closeFunding(@PathVariable("id") Long id) {
        FundingCompleteResponseDto funding = fundingFacade.closeFunding(id);
        return ResponseEntity.ok(RsData.success(funding));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/expire")
    public ResponseEntity<RsData<FundingCompleteResponseDto>> expireFunding(@PathVariable("id") Long id) {
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
            @RequestParam(value = "status", required = false) FundingStatus status,
            @Parameter(hidden = true) @CurrentMemberId Long memberId
    ) {
        PageResponse<ContributeFundingResponseDto> fundings = fundingFacade.getParticipatedFundings(page, size, memberId, status);
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
    public ResponseEntity<RsData<FundingCompleteResponseDto>> requestFundingAcceptance(
            @PathVariable("id") Long id,
            @Parameter(hidden = true) @CurrentMemberId Long memberId) {
        FundingCompleteResponseDto funding = fundingFacade.requestFundingAcceptance(id, memberId);
        return ResponseEntity.ok(RsData.success(funding));
    }

    // 나의 펀딩 단건 조회
    @Override
    @GetMapping("/my/{id}")
    public ResponseEntity<RsData<MyFundingResponseDto>> getMyFunding(
            @PathVariable("id") Long id,
            @RequestParam(value = "status", required = false) FundingStatus status,
            @Parameter(hidden = true) @CurrentMemberId Long memberId) {
        MyFundingResponseDto funding = fundingFacade.getMyFunding(id, status, memberId);
        return ResponseEntity.ok(RsData.success(funding));
    }

    // 나의 펀딩 목록 조회
    @Override
    @GetMapping("/my/list")
    public ResponseEntity<RsData<PageResponse<MyFundingSummaryDto>>> getMyFundings(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) FundingStatus status,
            @Parameter(hidden = true) @CurrentMemberId Long memberId) {
        PageResponse<MyFundingSummaryDto> fundings = fundingFacade.getMyFundings(page, size, status, memberId);
        return ResponseEntity.ok(RsData.success(fundings));
    }

    // 특정 친구의 진행 중인 펀딩 리스트 조회
    @Override
    @GetMapping("/friend/{friendId}/list")
    public ResponseEntity<RsData<PageResponse<FundingResponseDto>>> getFriendFundings(
            @PathVariable("friendId") Long friendId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(hidden = true) @CurrentMemberId Long memberId) {
        PageResponse<FundingResponseDto> fundings = fundingFacade.getFriendFundings(page, size, memberId, friendId);
        return ResponseEntity.ok(RsData.success(fundings));
    }

    // 특정 친구의 특정 펀딩 조회
    @Override
    @GetMapping("/friend/{friendId}/{id}")
    public ResponseEntity<RsData<FundingResponseDto>> getFriendFunding(
            @PathVariable("friendId") Long friendId,
            @PathVariable("id") Long id,
            @Parameter(hidden = true) @CurrentMemberId Long memberId) {
        FundingResponseDto funding = fundingFacade.getFriendFunding(friendId, id, memberId);
        return ResponseEntity.ok(RsData.success(funding));
    }

    // 내 친구들의 펀딩 리스트 조회
    @Override
    @GetMapping("/friends/list")
    public ResponseEntity<RsData<PageResponse<FundingResponseDto>>> getFriendsFundings(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(hidden = true) @CurrentMemberId Long memberId) {
        PageResponse<FundingResponseDto> fundings = fundingFacade.getFriendsFundings(page, size, memberId);
        return ResponseEntity.ok(RsData.success(fundings));
    }

    @Override
    @PostMapping("/retryAccept/{id}")
    public ResponseEntity<RsData<FundingCompleteResponseDto>> retryAcceptFunding(
            @PathVariable("id") Long id,
            @Parameter(hidden = true) @CurrentMemberId Long memberId) {
        FundingCompleteResponseDto funding = fundingFacade.retryAcceptFunding(id, memberId);
        return ResponseEntity.ok(RsData.success(funding));
    }
}
