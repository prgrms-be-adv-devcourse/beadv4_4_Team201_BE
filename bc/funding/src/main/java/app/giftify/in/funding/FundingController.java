package app.giftify.in.funding;

import app.giftify.app.funding.FundingFacade;
import app.giftify.shared.api.paging.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fundings")
public class FundingController {

    private final FundingFacade fundingFacade;

    // 전체 펀딩 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<FundingResponseDto> getFunding(@PathVariable Long id) {
        FundingResponseDto funding = fundingFacade.getFunding(id);
        return ResponseEntity.ok(funding);
    }

    // 전체 펀딩 리스트 조회
    @GetMapping("list")
    public ResponseEntity<PageResponse<FundingResponseDto>> getFundings(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<FundingResponseDto> fundings = fundingFacade.getFundings(page, size);
        return ResponseEntity.ok(fundings);
    }

    // 펀딩 종료 (관리자 전용)
    @PutMapping("/{id}/close")
    public ResponseEntity<FundingCompleteResponseDto> closeFunding(@PathVariable Long id) {
        // TODO: 관리자 권한 체크 필요
        FundingCompleteResponseDto funding = fundingFacade.closeFunding(id);
        return ResponseEntity.ok(funding);
    }

    // 펀딩 단건 만료 (관리자용)
    @PutMapping("/{id}/expire")
    public ResponseEntity<FundingCompleteResponseDto> expireFunding(@PathVariable Long id) {
        // TODO: 관리자 권한 체크 필요
        FundingCompleteResponseDto funding = fundingFacade.expireFunding(id);
        return ResponseEntity.ok(funding);
    }

    // 펀딩 수락 -> 펀딩이 achieve 상태로 변경되면 수령자에게 알람 옴
    // 펀딩 거절 -> 펀딩이 achieve 상태로 변경되면 수령자에게 알람 옴
    // 내가 참여한 펀딩 리스트 (본인 기여금이 나와야 함)
    // 내가 참여한 펀딩 단건 (본인의 기여금 나와야 함)
    // 나의 펀딩 (수령자) -> 진행중/완료로 나눠져야 함. 완료된 펀딩은 참여자 목록을 볼 수 있어야 함
    // 펀딩 검색 -> 필요할까?

}
