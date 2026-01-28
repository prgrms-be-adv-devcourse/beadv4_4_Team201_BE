package app.giftify.funding.in.product;

import app.giftify.funding.domain.product.ProductStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MyProductSearchDto extends ProductSearchDto { // 나의 상품 검색 (판매자)
	private ProductStatus status; // optional
}
