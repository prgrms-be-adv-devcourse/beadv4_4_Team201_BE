package app.giftify.product.application.port.in;

import app.giftify.product.adapter.inbound.web.requestDto.MyProductSearchDto;
import app.giftify.product.adapter.inbound.web.requestDto.ProductSearchDto;
import app.giftify.product.adapter.inbound.web.responseDto.ProductDto;
import app.giftify.product.application.port.out.MyProductSearchCommand;
import app.giftify.product.application.port.out.ProductRepositoryPort;
import app.giftify.product.application.port.out.ProductSearchCommand;
import app.giftify.product.domain.Product;
import app.giftify.replica.member.Member;
import app.giftify.replica.member.MemberRepository;
import app.giftify.shared.api.paging.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductSearchUseCase {

    private final ProductRepositoryPort productRepositoryPort;
    private final MemberRepository memberRepository;

    // 일반 상품 검색
    @Transactional(readOnly = true)
    public PageResponse<ProductDto> searchProducts(ProductSearchDto searchDto) {
        // 1. DTO -> 애플리케이션 계층의 Command 변환
        ProductSearchCommand command = new ProductSearchCommand(
                searchDto.getKeyword(),
                searchDto.getMinPrice(),
                searchDto.getMaxPrice(),
                searchDto.getInStock(),
                searchDto.getSort(),
                searchDto.getPage(),
                searchDto.getSize()
        );

        // 2. Port를 통해 도메인 객체 페이지 조회
        Page<Product> result = productRepositoryPort.searchProducts(command);

        // 3. DTO 리스트로 변환
        List<ProductDto> content = toProductDtos(result.getContent());

        // 4. PageResponse 형태로 반환
        return PageResponse.of(
                content,
                command.getPage(),
                command.getSize(),
                result.getTotalElements()
        );
    }

    // 내 상품 검색 (판매자)
    @Transactional(readOnly = true)
    public PageResponse<ProductDto> searchMyProducts(Long sellerId, MyProductSearchDto searchDto) {
        MyProductSearchCommand command = new MyProductSearchCommand(
                searchDto.getKeyword(),
                searchDto.getMinPrice(),
                searchDto.getMaxPrice(),
                searchDto.getInStock(),
                searchDto.getSort(),
                searchDto.getPage(),
                searchDto.getSize(),
                searchDto.getStatus()
        );

        Page<Product> result = productRepositoryPort.searchMyProducts(sellerId, command);
        List<ProductDto> content = toProductDtos(result.getContent());

        return PageResponse.of(
                content,
                command.getPage(),
                command.getSize(),
                result.getTotalElements()
        );
    }

    // 공통 로직 추출: List<Product> -> List<ProductDto>
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

        return memberRepository.findAllById(sellerIds).stream()
                .collect(Collectors.toMap(
                        Member::getId,
                        Member::getNickname
                ));
    }
}
