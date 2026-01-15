package out.product;

import org.springframework.data.domain.Page;

import domain.product.Product;
import in.product.ProductSearchDto;

public interface ProductQueryRepository {
	Page<Product> search(ProductSearchDto searchDto);
}
