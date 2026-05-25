package app.giftify.funding.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.giftify.funding.adapter.outbound.jpa.Funding;
import app.giftify.funding.adapter.outbound.repository.FundingRepository;
import app.giftify.funding.domain.type.FundingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SyncFundingProductUseCase {
	private static final Logger log = LoggerFactory.getLogger(SyncFundingProductUseCase.class);

    private final FundingRepository fundingRepository;

    @Transactional
    public void syncFundingProduct(Long productId, Integer productPrice, String productName, String imageKey) {
        List<Funding> fundings = fundingRepository.findByProductIdAndStatus(productId, FundingStatus.IN_PROGRESS);

        if (fundings.isEmpty()) { return; }

        for (Funding funding : fundings) {
            funding.updateProductInfo(productPrice, productName, imageKey);
        }

        log.info("[Funding] 진행 중 펀딩 상품 정보 일괄 동기화 완료. 상품 ID: {}, 변경 가격: {}원, 변경 이름: {}, 변경 이미지: {}, 적용 펀딩 수: {}건",
                productId, productPrice, productName, imageKey, fundings.size()
        );
    }
}
