package app.giftify.shared.domain.event.product;

import app.giftify.shared.domain.event.BaseDomainEvent;

/**
 * 상품 이름, 가격, 이미지키 정보 변동 시 이벤트 발행
 */
public class ProductUpdatedEvent extends BaseDomainEvent {
    private final Long productId;
    private final int productPrice;
    private final String productName;
    private final String imageKey;

    public ProductUpdatedEvent(Long productId, int productPrice, String productName, String imageKey) {
        super();
        this.productId = productId;
        this.productPrice = productPrice;
        this.productName = productName;
        this.imageKey = imageKey;
    }

    @Override
    public String toString() {
        return "ProductUpdatedEvent{" +
                "productId=" + productId + "}" +
                ", productPrice=" + productPrice + "}" +
                ", productName=" + productName + "}" +
                ", imageKey=" + imageKey + "}";
    }

    public Long getProductId() {
        return productId;
    }

    public int getProductPrice() {
        return productPrice;
    }

    public String getProductName() {
        return productName;
    }

    public String getImageKey() {
        return imageKey;
    }
}