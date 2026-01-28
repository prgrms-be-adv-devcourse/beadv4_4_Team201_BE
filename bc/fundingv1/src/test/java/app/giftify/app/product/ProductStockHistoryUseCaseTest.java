package app.giftify.app.product;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import app.giftify.domain.product.ProductStockHistory;
import app.giftify.domain.product.StockChangeType;
import app.giftify.in.product.StockHistoryDto;
import app.giftify.in.product.StockHistorySearchDto;
import app.giftify.out.product.ProductStockHistoryRepository;
import app.giftify.shared.api.paging.PageResponse;

@ExtendWith(MockitoExtension.class)
class ProductStockHistoryUseCaseTest {

	@Mock
	private ProductStockHistoryRepository stockHistoryRepository;

	@InjectMocks
	private ProductStockHistoryUseCase productStockHistoryUseCase;

	@Test
	@DisplayName("재고 이력 조회 - 페이징된 결과를 반환한다")
	void searchStockHistories_returnsPagedResults() {
		// given
		Long sellerId = 1L;
		StockHistorySearchDto searchDto = new StockHistorySearchDto();
		searchDto.setPage(0);
		searchDto.setSize(10);

		List<ProductStockHistory> histories = List.of(
			ProductStockHistory.manualAdjust(sellerId, 100L, 10, 100, 110),
			ProductStockHistory.orderDeduct(sellerId, 100L, 5, 110, 105)
		);
		Page<ProductStockHistory> page = new PageImpl<>(histories, PageRequest.of(0, 10), 2);

		when(stockHistoryRepository.searchStockHistories(sellerId, searchDto)).thenReturn(page);

		// when
		PageResponse<StockHistoryDto> result = productStockHistoryUseCase.searchStockHistories(sellerId, searchDto);

		// then
		assertThat(result.content()).hasSize(2);
		assertThat(result.pageNumber()).isZero();
		assertThat(result.pageSize()).isEqualTo(10);
		assertThat(result.totalElements()).isEqualTo(2);
	}

	@Test
	@DisplayName("재고 이력 조회 - DTO로 변환된다")
	void searchStockHistories_convertsToDto() {
		// given
		Long sellerId = 1L;
		Long productId = 100L;
		StockHistorySearchDto searchDto = new StockHistorySearchDto();

		ProductStockHistory history = ProductStockHistory.manualAdjust(sellerId, productId, 10, 100, 110);
		Page<ProductStockHistory> page = new PageImpl<>(List.of(history), PageRequest.of(0, 20), 1);

		when(stockHistoryRepository.searchStockHistories(sellerId, searchDto)).thenReturn(page);

		// when
		PageResponse<StockHistoryDto> result = productStockHistoryUseCase.searchStockHistories(sellerId, searchDto);

		// then
		StockHistoryDto dto = result.content().get(0);
		assertThat(dto.productId()).isEqualTo(productId);
		assertThat(dto.changeType()).isEqualTo(StockChangeType.MANUAL_ADJUST);
		assertThat(dto.delta()).isEqualTo(10);
		assertThat(dto.beforeStock()).isEqualTo(100);
		assertThat(dto.afterStock()).isEqualTo(110);
	}

	@Test
	@DisplayName("재고 이력 조회 - 결과가 없으면 빈 리스트 반환")
	void searchStockHistories_returnsEmptyList() {
		// given
		Long sellerId = 1L;
		StockHistorySearchDto searchDto = new StockHistorySearchDto();

		Page<ProductStockHistory> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
		when(stockHistoryRepository.searchStockHistories(sellerId, searchDto)).thenReturn(emptyPage);

		// when
		PageResponse<StockHistoryDto> result = productStockHistoryUseCase.searchStockHistories(sellerId, searchDto);

		// then
		assertThat(result.content()).isEmpty();
		assertThat(result.totalElements()).isZero();
	}
}