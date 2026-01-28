package app.giftify.product.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.product.adapter.inbound.MyProductSearchDto;
import app.giftify.product.adapter.inbound.ProductCreateRequestDto;
import app.giftify.product.adapter.inbound.ProductDto;
import app.giftify.product.adapter.inbound.ProductSearchDto;
import app.giftify.product.adapter.inbound.ProductSnapshotDto;
import app.giftify.product.adapter.inbound.ProductSnapshotRequestDto;
import app.giftify.product.adapter.inbound.ProductUpdateRequestDto;
import app.giftify.product.adapter.inbound.ProductUpdateResponseDto;
import app.giftify.product.adapter.inbound.StockHistoryDto;
import app.giftify.product.adapter.inbound.StockHistorySearchDto;
import app.giftify.shared.api.paging.PageResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductFacade {
	private final ProductCreateUseCase productCreateUseCase;
	private final ProductGetUseCase productGetUseCase;
	private final ProductSearchUseCase productSearchUseCase;
	private final ProductApproveUseCase productApproveUseCase;
	private final ProductRejectUseCase productRejectUseCase;
	private final ProductUpdateUseCase productUpdateUseCase;
	private final ProductStockHistoryUseCase productStockHistoryUseCase;
	private final ProductSnapshotCreateUseCase productSnapshotCreateUseCase;

	@Transactional //todo event
	public ProductDto createProduct(Long sellerId, ProductCreateRequestDto requestDto) {
		return productCreateUseCase.createProduct(sellerId, requestDto);
	}

	@Transactional(readOnly = true)
	public ProductDto getProduct(Long productId) {
		return productGetUseCase.getProduct(productId);
	}

	@Transactional(readOnly = true)
	public PageResponse<ProductDto> searchProducts(ProductSearchDto searchDto) {
		return productSearchUseCase.searchProducts(searchDto);
	}

	@Transactional(readOnly = true)
	public PageResponse<ProductDto> searchMyProducts(Long sellerId, MyProductSearchDto searchDto) {
		return productSearchUseCase.searchMyProducts(sellerId, searchDto);
	}

	@Transactional
	public void approveProduct(Long id) {
		productApproveUseCase.approveProduct(id);
	}

	@Transactional
	public void rejectProduct(Long id) {
		productRejectUseCase.rejectProduct(id);
	}

	@Transactional
	public ProductUpdateResponseDto updateProduct(Long productId, Long sellerId, ProductUpdateRequestDto requestDto) {
		return productUpdateUseCase.updateProduct(productId, sellerId, requestDto);
	}

	@Transactional(readOnly = true)
	public PageResponse<StockHistoryDto> searchStockHistories(Long sellerId, StockHistorySearchDto searchDto) {
		return productStockHistoryUseCase.searchStockHistories(sellerId, searchDto);
	}

	@Transactional
	public List<ProductSnapshotDto> createProductSnapshots(ProductSnapshotRequestDto requestDto) {
		return productSnapshotCreateUseCase.createProductSnapshots(requestDto);
	}
}
