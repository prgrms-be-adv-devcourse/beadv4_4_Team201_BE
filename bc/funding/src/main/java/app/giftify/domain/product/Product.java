package app.giftify.domain.product;

import static app.giftify.domain.product.ProductStatus.*;
import static app.giftify.domain.product.exception.ProductErrorCode.*;
import static app.giftify.shared.domain.event.product.ProductSaleDisableReason.*;

import app.giftify.domain.FundingMember;
import app.giftify.domain.product.exception.ProductException;
import app.giftify.in.product.ProductDto;
import app.giftify.shared.domain.event.product.ProductSaleDisabledEvent;
import app.giftify.shared.domain.event.product.ProductSaleEnabledEvent;
import app.giftify.shared.domain.event.product.ProductVerifiedEvent;
import app.giftify.support.jpa.BaseJpaEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PRODUCT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseJpaEntity {
	@ManyToOne
	@JoinColumn(name = "member_id")
	private FundingMember seller; // todo Long sellerId ?
	private String name;
	private String description;
	private int price;
	private int stock;
	private ProductStatus status;

	public Product(FundingMember seller, String name, String description, int price, int stock) {
		validateCreation(seller, name, description, price, stock);

		this.seller = seller;
		this.name = name;
		this.description = description;
		this.price = price;
		this.stock = stock;
		this.status = DRAFT;
	}

	// 상품 생성 검증 (Fail-Fast)
	private void validateCreation(
		FundingMember seller, String name, String description, int price, int stock
	) {
		if (seller == null)
			throw new ProductException(PRODUCT_SELLER_REQUIRED);
		if (name == null || name.isBlank())
			throw new ProductException(INVALID_PRODUCT_NAME);
		if (description == null || description.isBlank())
			throw new ProductException(INVALID_PRODUCT_DESCRIPTION);
		if (price <= 0)
			throw new ProductException(INVALID_PRODUCT_PRICE);
		if (stock < 0)
			throw new ProductException(INVALID_PRODUCT_STOCK);
	}

	/**
	 * DTO 변환
	 */
	// 일반 response 응답용
	public ProductDto toDto() {
		return new ProductDto(
			getId(), getSeller().getNickname(), getName(),
			getDescription(), getPrice(), getCreatedAt()
		);
	}

	/**
	 * 상품 상태 변경
	 * todo 상품 상태 변경 이력 컬렉션
	 */
	// 상품 등록 승인
	public void approve() {
		transitionTo(INACTIVE); // 판매 대기 상태로
		registerEvent(new ProductVerifiedEvent(
			this.getId(),
			this.getName(),
			this.getDescription(),
			this.getPrice(),
			this.getSeller().getNickname()
		));
	}

	// 상품 등록 거절
	public void reject() {
		transitionTo(REJECTED);
	}

	// 상품 판매 시작
	public void active() {
		transitionTo(ACTIVE);
		registerEvent(new ProductSaleEnabledEvent(this.getId())); // 판매 가능 이벤트 발생
	}

	// 상품 판매 중지
	public void inActive() {
		transitionTo(INACTIVE);
		registerEvent(new ProductSaleDisabledEvent(
			this.getId(),
			STOPPED_BY_SELLER
		)); // 판매 불가능 이벤트 발생
	}

	// 상품 상태 검증 todo Map<from,to> 상태 머신
	private void validateTransition(ProductStatus toStatus) {
		if (toStatus == ProductStatus.DRAFT) {
			throw new ProductException(PRODUCT_CANNOT_CHANGE_STATUS_TO_DRAFT);
		}
		switch (toStatus) {
			case REJECTED -> {
				if (this.status != DRAFT)
					throw new ProductException(PRODUCT_NOT_IN_DRAFT_STATUS);
			}
			case ACTIVE -> {
				if (this.status != INACTIVE)
					throw new ProductException(PRODUCT_NOT_IN_INACTIVE_STATUS);
			}
			case INACTIVE -> {
				if (this.status != DRAFT && this.status != ACTIVE)
					throw new ProductException(PRODUCT_REJECTED_CANNOT_BE_ACTIVATED);
			}
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
	}

	public void updateStock(int newStock) {
		// todo 재고이력 컬렉션
		int beforeStock = this.stock;
		this.stock = newStock;

		if (beforeStock > 0 && newStock == 0 && this.status == ACTIVE) {
			registerEvent(new ProductSaleDisabledEvent(this.getId(), SOLD_OUT)); // 품절 이벤트 발생
		}
		if (beforeStock == 0 && newStock > 0 && this.status == ACTIVE) {
			registerEvent(new ProductSaleEnabledEvent(this.getId())); // 판매 가능 이벤트 발생
		}
	}

	// 상품 재고 감소 todo 재고이력 컬렉션
	public void decreaseStock(int quantity) {
		if (this.stock < quantity)
			throw new ProductException(PRODUCT_OUT_OF_STOCK);

		this.stock -= quantity;

		if (this.stock == 0 && this.status == ACTIVE)
			registerEvent(new ProductSaleDisabledEvent(this.getId(), SOLD_OUT)); // 품절 이벤트 발생
	}

	// 상품 재고 추가 todo 재고이력 컬렉션
	public void increaseStock(int quantity) {
		int beforeStock = this.stock;
		this.stock += quantity;

		if (beforeStock == 0 && this.status == ACTIVE)
			registerEvent(new ProductSaleEnabledEvent(this.getId())); // 판매 가능 이벤트 발생
	}
}
