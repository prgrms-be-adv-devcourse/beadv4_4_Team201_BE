package app.giftify.product.application;

import app.giftify.catalogmember.adapter.outbound.jpa.CatalogMemberRepository;
import app.giftify.catalogmember.core.domain.CatalogMember;
import app.giftify.product.adapter.inbound.MyProductSearchDto;
import app.giftify.product.adapter.inbound.ProductDto;
import app.giftify.product.adapter.inbound.ProductSearchDto;
import app.giftify.product.adapter.outbound.ProductRepository;
import app.giftify.product.domain.Product;
import app.giftify.shared.api.paging.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductSearchUseCase {

    private final ProductRepository productRepository;
    private final CatalogMemberRepository catalogMemberRepository;

    // 일반 상품 검색
    public PageResponse<ProductDto> searchProducts(ProductSearchDto searchDto) {
        Page<Product> result = productRepository.searchProducts(searchDto);

        List<ProductDto> content = toProductDtos(result.getContent());

        return PageResponse.of(
                content,
                searchDto.getPage(),
                searchDto.getSize(),
                result.getTotalElements()
        );
    }

    // 내 상품 검색 (판매자)
    public PageResponse<ProductDto> searchMyProducts(Long sellerId, MyProductSearchDto searchDto) {
        Page<Product> result = productRepository.searchMyProducts(sellerId, searchDto);

        List<ProductDto> content = toProductDtos(result.getContent());

        return PageResponse.of(
                content,
                searchDto.getPage(),
                searchDto.getSize(),
                result.getTotalElements()
        );
    }

    // 공통 로직 추출
    private List<ProductDto> toProductDtos(List<Product> products) {
        Map<Long, String> sellerNicknameMap = getSellerNicknameMap(products);

        return products.stream()
                .map(product -> ProductDto.from(product, sellerNicknameMap.get(product.getSellerId())))
                .toList();
    }

    private Map<Long, String> getSellerNicknameMap(List<Product> products) {
        List<Long> sellerIds = products.stream()
                .map(Product::getSellerId)
                .distinct()
                .toList();

        return catalogMemberRepository.findAllById(sellerIds).stream()
                .collect(Collectors.toMap(
                        CatalogMember::getId,
                        CatalogMember::getNickname
                ));
    }
}
