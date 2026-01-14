package domain.product;

import static jakarta.persistence.GenerationType.*;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import domain.FundingMember;
import in.product.ProductDto;
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
	// private ProductStatus status;

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
	}

	public ProductDto toDto() {
		return new ProductDto(getId(), getSeller().getNickname(), getName(), getDescription(), getPrice(), getStock());
	}
}
