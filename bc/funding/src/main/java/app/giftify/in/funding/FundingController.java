package app.giftify.in.funding;

import app.giftify.app.funding.FundingFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fundings")
public class FundingController {

    private final FundingFacade fundingFacade;

    // 펀딩 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<FundingResponseDto> getFunding(@PathVariable Long id) {
        FundingResponseDto funding = fundingFacade.getFunding(id);
        return ResponseEntity.ok(funding);
    }

    // 펀딩 종료
    @PutMapping("/{id}/close")
    public ResponseEntity<FundingCompleteResponseDto> closeFunding(@PathVariable Long id) {
        FundingCompleteResponseDto funding = fundingFacade.closeFunding(id);
        return ResponseEntity.ok(funding);
    }

    // 펀딩 단건 만료 (관리자용)
    @PutMapping("/{id}/expire")
    public ResponseEntity<FundingCompleteResponseDto> expireFunding(@PathVariable Long id) {
        FundingCompleteResponseDto funding = fundingFacade.expireFunding(id);
        return ResponseEntity.ok(funding);
    }
}
