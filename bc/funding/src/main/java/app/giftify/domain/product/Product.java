package app.giftify.domain.product;

import static app.giftify.domain.product.ProductStatus.*;
import static app.giftify.in.product.web.ProductErrorCode.*;
import static jakarta.persistence.GenerationType.*;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import app.giftify.domain.FundingMember;
import app.giftify.in.product.ProductDto;
import app.giftify.in.product.web.ProductException;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PRODUCT")
@NoArgsConstructor
@Getter
public class Product { //todo validation
	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;
	@ManyToOne
	@JoinColumn(name = "member_id")
	private FundingMember seller;
	private String name;
	private String description;
	private int price;
	private int stock;
	private ProductStatus status;

	@CreatedDate
	private LocalDateTime createdAt;
	@LastModifiedDate
	private LocalDateTime modifiedAt;

	public Product(FundingMember seller, String name, String description, int price, int stock) {
		this.seller = seller;
		this.name = name;
		this.description = description;
		this.price = price;
		this.stock = stock;
		this.status = DRAFT;
	}

	public ProductDto toDto() {
		return new ProductDto(getId(), getSeller().getNickname(), getName(), getDescription(), getPrice(), getStock(),
			getCreatedAt());
	}

	// 상품 등록 승인
	public void approve() {
		updateProductStatus(INACTIVE); // 판매 대기 상태로
	}

	// 상품 등록 거절
	public void reject() {
		updateProductStatus(REJECTED);
	}

	// 상품 판매 시작
	public void active() {
		updateProductStatus(ACTIVE);
	}

	// 상품 판매 중지
	public void inActive() {
		updateProductStatus(INACTIVE);
	}

	// 상품 상태 변경
	public void updateProductStatus(ProductStatus status) {
		switch (status) {
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
}
