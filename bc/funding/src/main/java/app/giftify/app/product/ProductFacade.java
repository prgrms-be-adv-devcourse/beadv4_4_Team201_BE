package app.giftify.app.product;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.in.product.MyProductSearchDto;
import app.giftify.in.product.ProductCreateRequestDto;
import app.giftify.in.product.ProductDto;
import app.giftify.in.product.ProductSearchDto;
import app.giftify.in.product.ProductUpdateRequestDto;
import app.giftify.in.product.ProductUpdateResponseDto;
import app.giftify.in.product.StockHistoryDto;
import app.giftify.in.product.StockHistorySearchDto;
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

	@Transactional //todo event
	public ProductDto createProduct(Long sellerId, ProductCreateRequestDto requestDto) {
		return productCreateUseCase.createProduct(sellerId, requestDto);
	}

	@Transactional(readOnly = true)
	public ProductDto getProduct(Long id) { // todo auth: ACTIVE 상태가 아닌 상품은 판매자 본인만 조회 가능 (seller == 로그인 유저)
		return productGetUseCase.getProduct(id);
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
}
