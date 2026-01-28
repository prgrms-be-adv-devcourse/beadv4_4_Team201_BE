package app.giftify.funding.out.product;

import org.springframework.data.domain.Page;

import app.giftify.funding.domain.product.Product;
import app.giftify.funding.in.product.MyProductSearchDto;
import app.giftify.funding.in.product.ProductSearchDto;

public interface ProductQueryRepository {
	Page<Product> searchProducts(ProductSearchDto searchDto);

	Page<Product> searchMyProducts(Long sellerId, MyProductSearchDto searchDto);
}
