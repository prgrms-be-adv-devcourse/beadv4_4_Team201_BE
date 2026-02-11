package app.giftify.funding.adpater.inbound.web;

import app.giftify.funding.application.FundingGetUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal/funding")
@RequiredArgsConstructor
public class InternalFundingController {

    private final FundingGetUseCase fundingGetUseCase;

    @GetMapping("/{productId}/exists")
    public Boolean checkFundingExistsByProductId(@PathVariable Long productId) {
        return fundingGetUseCase.checkFundingExistsByProductId(productId);
    }
}
