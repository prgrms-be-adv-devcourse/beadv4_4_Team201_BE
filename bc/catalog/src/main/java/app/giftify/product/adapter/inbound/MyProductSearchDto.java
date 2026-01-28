package app.giftify.product.adapter.inbound;

import app.giftify.product.domain.ProductStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MyProductSearchDto extends ProductSearchDto { // 나의 상품 검색 (판매자)
	private ProductStatus status; // optional
}
