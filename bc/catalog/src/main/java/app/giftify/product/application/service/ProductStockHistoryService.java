package app.giftify.product.application.service;

import app.giftify.product.adapter.inbound.web.requestDto.StockHistorySearchDto;
import app.giftify.product.adapter.inbound.web.responseDto.StockHistoryDto;
import app.giftify.product.adapter.outbound.jpa.entity.ProductStockHistory;
import app.giftify.product.adapter.outbound.jpa.repository.ProductStockHistoryRepository;
import app.giftify.product.application.port.in.ProductStockHistoryUseCase;
import app.giftify.shared.api.paging.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductStockHistoryService implements ProductStockHistoryUseCase {

    private final ProductStockHistoryRepository stockHistoryRepository;

    @Override
    public PageResponse<StockHistoryDto> searchStockHistories(Long sellerId, StockHistorySearchDto searchDto) {
        Page<ProductStockHistory> result = stockHistoryRepository.searchStockHistories(sellerId, searchDto);

        return PageResponse.of(
                result.getContent().stream().map(StockHistoryDto::from).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements()
        );
    }
}
