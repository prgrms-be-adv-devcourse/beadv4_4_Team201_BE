package app.giftify.funding.adapter.inbound.web;

import app.giftify.funding.application.FundingGetUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal/funding")
@RequiredArgsConstructor
public class InternalFundingController {

    private final FundingGetUseCase fundingGetUseCase;

    @GetMapping("/{productId}/exists")
    public boolean checkFundingExistsByProductId(@PathVariable Long productId) {
        return fundingGetUseCase.checkFundingExistsByProductId(productId);
    }
}
