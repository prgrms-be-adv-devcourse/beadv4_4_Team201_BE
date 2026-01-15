package app.giftify.out.product;

import org.springframework.data.domain.Page;

import app.giftify.domain.product.Product;
import app.giftify.in.product.ProductSearchDto;

public interface ProductQueryRepository {
	Page<Product> search(ProductSearchDto searchDto);
}
