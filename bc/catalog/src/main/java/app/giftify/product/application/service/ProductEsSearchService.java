package app.giftify.product.application.service;

import app.giftify.product.adapter.inbound.web.requestDto.ProductEsSearchDto;
import app.giftify.product.application.port.in.ProductEsSearchUseCase;
import app.giftify.product.application.port.in.ProductResult;
import app.giftify.product.application.port.out.ProductEsPort;
import app.giftify.product.application.port.out.ProductEsSearchCommand;
import app.giftify.shared.api.paging.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductEsSearchService implements ProductEsSearchUseCase {

    private final ProductEsPort productEsPort;

    @Override
    public PageResponse<ProductResult> searchByEs(ProductEsSearchDto searchDto) {
        ProductEsSearchCommand command = new ProductEsSearchCommand(
                searchDto.getKeyword(),
                searchDto.getMinPrice(),
                searchDto.getMaxPrice(),
                searchDto.getCategory(),
                searchDto.getSort(),
                searchDto.getPage(),
                searchDto.getSize()
        );
        return productEsPort.searchProducts(command);
    }
}
