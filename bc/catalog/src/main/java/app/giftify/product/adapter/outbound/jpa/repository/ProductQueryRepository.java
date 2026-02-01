package app.giftify.product.adapter.outbound.jpa.repository;

import app.giftify.product.adapter.inbound.web.requestDto.MyProductSearchDto;
import app.giftify.product.adapter.inbound.web.requestDto.ProductSearchDto;
import app.giftify.product.adapter.outbound.jpa.entity.Product;
import org.springframework.data.domain.Page;

public interface ProductQueryRepository {
    Page<Product> searchProducts(ProductSearchDto searchDto);

    Page<Product> searchMyProducts(Long sellerId, MyProductSearchDto searchDto);
}
