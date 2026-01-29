package app.giftify.product.application;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import app.giftify.product.domain.ProductStockHistory;
import app.giftify.product.adapter.inbound.StockHistoryDto;
import app.giftify.product.adapter.inbound.StockHistorySearchDto;
import app.giftify.product.adapter.outbound.ProductStockHistoryRepository;
import app.giftify.shared.api.paging.PageResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductStockHistoryUseCase {
	private final ProductStockHistoryRepository stockHistoryRepository;

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
