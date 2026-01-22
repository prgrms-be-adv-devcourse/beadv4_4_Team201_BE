package app.giftify.app.product;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import app.giftify.domain.product.ProductStockHistory;
import app.giftify.in.product.StockHistoryDto;
import app.giftify.in.product.StockHistorySearchDto;
import app.giftify.out.product.ProductStockHistoryRepository;
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