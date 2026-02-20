package app.giftify.funding.application;

import app.giftify.funding.adpater.outbound.jpa.Funding;
import app.giftify.funding.adpater.outbound.repository.FundingRepository;
import app.giftify.funding.domain.FundingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncFundingProductUseCase {
    private final FundingRepository fundingRepository;

    @Transactional
    public void syncFundingProduct(Long productId, Integer productPrice) {
        List<Funding> fundings = fundingRepository.findByProductIdAndStatus(productId, FundingStatus.IN_PROGRESS);

        if (fundings.isEmpty()) { return; }

        for (Funding funding : fundings) {
            funding.updateProductInfo(productPrice);
        }

        log.info("[Funding] 펀딩 목표액 일괄 변경 완료. 상품 ID: {}, 변경 금액: {}원, 변경된 펀딩 수: {}건", 
                productId, productPrice, fundings.size());
    }
}
