package app.giftify.product.adapter.inbound.event;

import app.giftify.product.application.port.out.ProductEsPort;
import app.giftify.product.application.support.ProductSupport;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.event.ProductAcceptedEvent;
import app.giftify.shared.domain.event.member.SellerNicknameChangedEvent;
import app.giftify.shared.domain.event.product.ProductDeletedEvent;
import app.giftify.shared.domain.event.product.ProductSaleDisabledEvent;
import app.giftify.shared.domain.event.product.ProductSaleEnabledEvent;
import app.giftify.shared.domain.event.product.ProductUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProductEsEventListener {

    private final ProductSupport productSupport;
    private final ProductEsPort productEsPort;

    // ES 도큐먼트 생성
    @ApplicationModuleListener
    public void handleAccepted(ProductAcceptedEvent event) {
        syncToEs(event.getProductId());
        log.info("ES 도큐먼트 생성 완료: {}", event);
    }

    // ES 도큐먼트 상태 ACTIVE 변경
    @ApplicationModuleListener
    public void handleSaleEnabled(ProductSaleEnabledEvent event) {
        syncToEs(event.getProductId());
        log.info("ES 도큐먼트 ACTIVE 변경 완료: productId={}", event.getProductId());
    }

    // ES 도큐먼트 상태 INACTIVE 변경
    @ApplicationModuleListener
    public void handleSaleDisabled(ProductSaleDisabledEvent event) {
        syncToEs(event.getProductId());
        log.info("ES 도큐먼트 INACTIVE 변경 완료: productId={}", event.getProductId());
    }

    // ES 도큐먼트 업데이트 (sync)
    @ApplicationModuleListener
    public void handleUpdated(ProductUpdatedEvent event) {
        syncToEs(event.getProductId());
        log.info("ES 도큐먼트 업데이트 완료: {}", event);
    }

    // ES 도큐먼트 삭제 (sync)
    @ApplicationModuleListener
    public void handleDeleted(ProductDeletedEvent event) {
        productEsPort.deleteById(event.getProductId());
        log.info("ES 도큐먼트 삭제 완료: {}", event.getProductId());
    }

    // 판매자 닉네임 변경 시 ES 도큐먼트 일괄 업데이트
    @ApplicationModuleListener
    public void handleSellerNicknameChanged(SellerNicknameChangedEvent event) {
        productEsPort.updateSellerNickname(event.getSellerId(), event.getNickname());
    }

    private void syncToEs(Long productId) {
        Product product = productSupport.findById(productId);
        productEsPort.save(product);
    }
}
