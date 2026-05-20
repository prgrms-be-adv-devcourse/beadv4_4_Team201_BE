package app.giftify.product.adapter.outbound.jpa.repository;

import app.giftify.product.adapter.inbound.web.requestDto.MyProductSearchDto;
import app.giftify.product.adapter.inbound.web.requestDto.ProductSearchDto;
import app.giftify.product.adapter.outbound.jpa.entity.ProductJpa;
import org.springframework.data.domain.Page;

public interface ProductQueryRepository {
    Page<ProductJpa> searchProducts(ProductSearchDto searchDto);

    Page<ProductJpa> searchMyProducts(Long sellerId, MyProductSearchDto searchDto);
}
