package app.giftify.domain.product;

import static app.giftify.domain.product.ProductStatus.*;
import static app.giftify.domain.product.exception.ProductErrorCode.*;

import app.giftify.domain.FundingMember;
import app.giftify.domain.product.exception.ProductException;
import app.giftify.in.product.ProductDto;
import app.giftify.shared.domain.event.product.ProductSnapshot;
import app.giftify.shared.domain.event.product.ProductSoldoutEvent;
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
	private FundingMember seller;
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
	// response 응답용
	public ProductDto toDto() {
		return new ProductDto(
			getId(), getSeller().getNickname(), getName(),
			getDescription(), getPrice(), getCreatedAt()
		);
	}

	// sync 이벤트 등에 사용
	public ProductSnapshot toSnapshot() {
		return new ProductSnapshot(
			getId(), getSeller().getNickname(), getName(),
			getDescription(), getPrice(), getStock(),
			getCreatedAt(), getUpdatedAt()
		);
	}

	/**
	 * 상품 상태 변경
	 */
	// 상품 등록 승인
	public void approve() {
		validateTransition(INACTIVE); // 판매 대기 상태로
		// todo 판매 상태 변경 이벤트
	}

	// 상품 등록 거절
	public void reject() {
		validateTransition(REJECTED);
		// todo 판매 상태 변경 이벤트
	}

	// 상품 판매 시작
	public void active() {
		validateTransition(ACTIVE);
		// todo 판매 상태 변경 이벤트
	}

	// 상품 판매 중지
	public void inActive() {
		validateTransition(INACTIVE);
		// todo 판매 상태 변경 이벤트 (미완료된 펀딩 환불)
	}

	// 상품 상태 변경 가능 여부 검증
	private void validateTransition(ProductStatus toStatus) {
		switch (toStatus) {
			case DRAFT -> throw new ProductException(PRODUCT_CANNOT_CHANGE_STATUS_TO_DRAFT);
			case REJECTED -> {
				if (this.status != DRAFT)
					throw new ProductException(PRODUCT_NOT_IN_DRAFT_STATUS);
				this.status = REJECTED;
			}
			case ACTIVE -> {
				if (this.status != INACTIVE)
					throw new ProductException(PRODUCT_NOT_IN_INACTIVE_STATUS);
				this.status = ACTIVE;
			}
			case INACTIVE -> {
				if (this.status == REJECTED)
					throw new ProductException(PRODUCT_REJECTED_CANNOT_BE_ACTIVATED);
				this.status = INACTIVE;
			}
		}
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
		// todo 재고이력관리
		int beforeStock = this.stock;
		this.stock = newStock;

		if (beforeStock > 0 && newStock == 0) {
			registerEvent(new ProductSoldoutEvent(this.getId())); // 품절 이벤트 발생
			// eventPublisher(new ProductSoldoutEvent(this.getId()));
		}
	}

	// 상품 재고 감소 todo 재고이력관리
	public void decreaseStock(int quantity) {
		if (this.stock < quantity)
			throw new ProductException(PRODUCT_OUT_OF_STOCK);
		this.stock -= quantity;
		if (this.stock == 0)
			registerEvent(new ProductSoldoutEvent(this.getId())); // 품절 이벤트 발생
		// eventPublisher(new ProductSoldoutEvent(this.getId()));
	}

	// 상품 재고 추가 todo 재고이력관리
	public void increaseStock(int quantity) {
		this.stock += quantity;
	}
}
