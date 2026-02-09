package app.giftify.product.domain;

import app.giftify.product.domain.event.ProductStockUpdatedEvent;
import app.giftify.product.domain.exception.ProductException;
import app.giftify.shared.domain.base.BaseDomainModel;
import app.giftify.shared.domain.event.product.ProductPriceUpdatedEvent;
import app.giftify.shared.domain.event.product.ProductSaleDisabledEvent;
import app.giftify.shared.domain.event.product.ProductSaleEnabledEvent;
import lombok.Builder;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

import static app.giftify.product.domain.ProductStatus.*;
import static app.giftify.product.domain.exception.ProductErrorCode.*;

@Getter
public class Product extends BaseDomainModel {
    private final Long sellerId;
    private String name;
    private String description;
    private int price;
    private int stock;
    private ProductStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    @Builder
    public Product(Long id, Long sellerId, String name, String description, int price, int stock, ProductStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id);
        validateCreation(sellerId, name, description, price, stock);
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.status = (status == null) ? DRAFT : status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    private static void validateCreation(Long sellerId, String name, String description, int price, int stock) {
        if (sellerId == null)
            throw new ProductException(PRODUCT_SELLER_REQUIRED);
        if (!StringUtils.hasText(name))
            throw new ProductException(INVALID_PRODUCT_NAME);
        if (!StringUtils.hasText(description))
            throw new ProductException(INVALID_PRODUCT_DESCRIPTION);
        if (price <= 0)
            throw new ProductException(INVALID_PRODUCT_PRICE);
        if (stock < 0)
            throw new ProductException(INVALID_PRODUCT_STOCK);
    }

    /**
     * 상품 상태 변경
     * todo 상품 상태 변경 이력 컬렉션
     */
    // 상품 등록 승인
    public void approve() {
        transitionTo(INACTIVE); // 판매 대기 상태로
    }

    // 상품 등록 거절
    public void reject() {
        transitionTo(REJECTED);
    }

    // 상품 판매 시작
    public void active() {
        transitionTo(ACTIVE);
        registerEvent(new ProductSaleEnabledEvent(this.getId()));
    }

    // 상품 판매 중지
    public void inActive() {
        transitionTo(INACTIVE);
        registerEvent(new ProductSaleDisabledEvent(this.getId()));
    }

    // 상품 상태 검증 todo Map<from,to> 상태 머신
    private void validateTransition(ProductStatus toStatus) {
        if (toStatus == DRAFT) {
            throw new ProductException(PRODUCT_CANNOT_CHANGE_STATUS_TO_DRAFT);
        }
        switch (toStatus) {
            case REJECTED:
                if (this.status != DRAFT)
                    throw new ProductException(PRODUCT_NOT_IN_DRAFT_STATUS);
                break;
            case ACTIVE:
                if (this.status != INACTIVE)
                    throw new ProductException(PRODUCT_NOT_IN_INACTIVE_STATUS);
                break;
            case INACTIVE:
                if (this.status != DRAFT && this.status != ACTIVE)
                    throw new ProductException(PRODUCT_REJECTED_CANNOT_BE_ACTIVATED);
                break;
        }
    }

    // 상태 변경
    private void transitionTo(ProductStatus toStatus) {
        validateTransition(toStatus);
        this.status = toStatus;
    }

    /**
     * 상품 정보 수정
     */
    public void updateName(String newName) {
        this.name = newName;
    }

    public void updateDescription(String newDescription) {
        this.description = newDescription;
    }

    public void updatePrice(int newPrice) {
        this.price = newPrice;

        registerEvent(new ProductPriceUpdatedEvent(this.getId(), newPrice));
    }

    // 상품 재고 수정 (판매자 수동)
    public void updateStock(int newStock) {
        int beforeStock = this.stock;
        this.stock = newStock;

        if (beforeStock > 0 && newStock == 0 && this.status == ACTIVE) {
            registerEvent(new ProductSaleDisabledEvent(this.getId()));
        }
        if (beforeStock == 0 && newStock > 0 && this.status == ACTIVE) {
            registerEvent(new ProductSaleEnabledEvent(this.getId()));
        }

        registerStockUpdatedEvent(this.sellerId, this.getId(), beforeStock, this.stock, newStock - beforeStock, StockChangeType.MANUAL_SELLER);
    }

    // 펀딩에 의한 상품 재고 감소 todo 동시성 제어
    public void decreaseStockByFunding() {
        validateStockForFunding();

        int beforeStock = this.stock;
        --this.stock;

        if (this.stock == 0 && this.status == ACTIVE)
            registerEvent(new ProductSaleDisabledEvent(this.getId()));

        registerStockUpdatedEvent(this.sellerId, this.getId(), beforeStock, this.stock, this.stock - beforeStock, StockChangeType.ORDER_COMPLETED);
    }

    // 펀딩에 의한 상품 재고 추가 (반품 등등)
    public void increaseStockByFunding() {
        int beforeStock = this.stock;
        ++this.stock;

        if (beforeStock == 0 && this.status == ACTIVE)
            registerEvent(new ProductSaleEnabledEvent(this.getId()));

        registerStockUpdatedEvent(this.sellerId, this.getId(), beforeStock, this.stock, this.stock - beforeStock, StockChangeType.ORDER_REFUNDED);
    }

    // 일반 주문에 의한 상품 재고 감소 todo 동시성 제어
    public void decreaseStock(int quantity) {
        validateStockForOrder(quantity);

        int beforeStock = this.stock;
        this.stock -= quantity;

        if (this.stock == 0 && this.status == ACTIVE)
            registerEvent(new ProductSaleDisabledEvent(this.getId()));

        registerStockUpdatedEvent(this.sellerId, this.getId(), beforeStock, this.stock, this.stock - beforeStock, StockChangeType.ORDER_COMPLETED);
    }

    // 일반 주문에 의한 상품 재고 추가 (반품 등등)
    public void increaseStock(int quantity) {
        int beforeStock = this.stock;
        this.stock += quantity;

        if (beforeStock == 0 && this.status == ACTIVE)
            registerEvent(new ProductSaleEnabledEvent(this.getId()));

        registerStockUpdatedEvent(this.sellerId, this.getId(), beforeStock, this.stock, this.stock - beforeStock, StockChangeType.ORDER_REFUNDED);
    }

    // 펀딩 재고 검증: 펀딩은 항상 1개 차감이므로 재고가 1 미만이면 불가
    private void validateStockForFunding() {
        if (this.stock < 1) {
            throw new ProductException(PRODUCT_OUT_OF_STOCK);
        }
    }

    // 일반 주문 재고 검증: 주문 수량만큼 재고가 있어야 함
    private void validateStockForOrder(int quantity) {
        if (this.stock < quantity) {
            throw new ProductException(PRODUCT_OUT_OF_STOCK);
        }
    }

    private void registerStockUpdatedEvent(
            Long sellerId,
            Long productId,
            int beforeStock,
            int afterStock,
            int delta,
            StockChangeType changeType
    ) {
        StockChangeResult result = new StockChangeResult(sellerId, productId, beforeStock, afterStock, delta, changeType);
        registerEvent(new ProductStockUpdatedEvent(result));
    }
}
