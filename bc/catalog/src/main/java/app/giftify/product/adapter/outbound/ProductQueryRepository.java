package app.giftify.product.adapter.outbound;

import org.springframework.data.domain.Page;

import app.giftify.product.domain.Product;
import app.giftify.product.adapter.inbound.MyProductSearchDto;
import app.giftify.product.adapter.inbound.ProductSearchDto;

public interface ProductQueryRepository {
	Page<Product> searchProducts(ProductSearchDto searchDto);

	Page<Product> searchMyProducts(Long sellerId, MyProductSearchDto searchDto);
}
