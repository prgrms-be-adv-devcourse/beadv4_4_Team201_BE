package app.giftify.shared.domain.event.product;

import app.giftify.shared.domain.event.BaseDomainEvent;

/**
 * 상품 가격 변동 시 이벤트 발행
 * - 진행 중인 펀딩의 목표 금액 또한 변동이 적용되어야 합니다. (달성된 펀딩은 영향 X)
 */
public class ProductPriceUpdatedEvent extends BaseDomainEvent {
    private final Long productId;
    private final int productPrice;

    public ProductPriceUpdatedEvent(Long productId, int productPrice) {
        super();
        this.productId = productId;
        this.productPrice = productPrice;
    }

    public Long getProductId() {
        return productId;
    }

    public int getProductPrice() {
        return productPrice;
    }
}