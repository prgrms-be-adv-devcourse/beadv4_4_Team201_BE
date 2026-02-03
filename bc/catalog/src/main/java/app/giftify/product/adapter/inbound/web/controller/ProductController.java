package app.giftify.product.adapter.inbound.web.controller;

import static app.giftify.product.domain.exception.ProductErrorCode.*;
import static org.springframework.http.HttpStatus.*;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.giftify.product.adapter.inbound.web.requestDto.MyProductSearchDto;
import app.giftify.product.adapter.inbound.web.requestDto.ProductCreateRequestDto;
import app.giftify.product.adapter.inbound.web.requestDto.ProductSearchDto;
import app.giftify.product.adapter.inbound.web.requestDto.ProductUpdateRequestDto;
import app.giftify.product.adapter.inbound.web.requestDto.StockHistorySearchDto;
import app.giftify.product.adapter.inbound.web.responseDto.MyProductDto;
import app.giftify.product.adapter.inbound.web.responseDto.ProductDto;
import app.giftify.product.adapter.inbound.web.responseDto.ProductUpdateResponseDto;
import app.giftify.product.adapter.inbound.web.responseDto.StockHistoryDto;
import app.giftify.product.application.port.in.MyProductResult;
import app.giftify.product.application.port.in.ProductApproveUseCase;
import app.giftify.product.application.port.in.ProductCreateCommand;
import app.giftify.product.application.port.in.ProductCreateUseCase;
import app.giftify.product.application.port.in.ProductGetUseCase;
import app.giftify.product.application.port.in.ProductRejectUseCase;
import app.giftify.product.application.port.in.ProductResult;
import app.giftify.product.application.port.in.ProductSearchUseCase;
import app.giftify.product.application.port.in.ProductStockHistoryUseCase;
import app.giftify.product.application.port.in.ProductUpdateResult;
import app.giftify.product.application.port.in.ProductUpdateUseCase;
import app.giftify.product.domain.exception.ProductException;
import app.giftify.security.common.CurrentMemberId;
import app.giftify.shared.api.paging.PageResponse;
import app.giftify.shared.api.response.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/products")
@Slf4j
public class ProductController implements ProductV2ApiSpec {
	private final ProductCreateUseCase productCreateUseCase;
	private final ProductGetUseCase productGetUseCase;
	private final ProductSearchUseCase productSearchUseCase;
	private final ProductApproveUseCase productApproveUseCase;
	private final ProductRejectUseCase productRejectUseCase;
	private final ProductUpdateUseCase productUpdateUseCase;
	private final ProductStockHistoryUseCase productStockHistoryUseCase;

	// 상품 등록
	@PostMapping
	@PreAuthorize("hasRole('SELLER')")
	public ResponseEntity<RsData<ProductDto>> createProduct(
		@CurrentMemberId Long sellerId,
		@Valid @RequestBody ProductCreateRequestDto requestDto
	) {
		var command = new ProductCreateCommand(
			requestDto.name(),
			requestDto.description(),
			requestDto.price(),
			requestDto.stock()
		);
		ProductResult result = productCreateUseCase.createProduct(sellerId, command);
		ProductDto responseDto = ProductDto.from(result);
		return ResponseEntity.status(CREATED).body(RsData.success(responseDto));
	}

	// 상품 등록 승인 및 거절 (관리자)
	@PatchMapping("/{id}/{action}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<RsData<String>> changeApproval(
		@PathVariable("id") Long id,
		@PathVariable("action") String action
	) {
		switch (action) {
			case "approve" -> {
				productApproveUseCase.approveProduct(id);
				return ResponseEntity.ok(RsData.success(null, "상품 등록 승인, 상품 ID: " + id));
			}
			case "reject" -> {
				productRejectUseCase.rejectProduct(id);
				return ResponseEntity.ok(RsData.success(null, "상품 등록 거절, 상품 ID: " + id));
			}
			default -> throw new ProductException(INVALID_APPROVAL_ACTION);
		}
	}

	// 상품 수정
	@PreAuthorize("hasRole('SELLER')")
	@PatchMapping("/my/{productId}")
	public ResponseEntity<RsData<ProductUpdateResponseDto>> updateProduct(
		@PathVariable("productId") Long productId,
		@CurrentMemberId Long sellerId,
		@RequestBody ProductUpdateRequestDto requestDto
	) {
		ProductUpdateResult result = productUpdateUseCase.updateProduct(productId, sellerId, requestDto);
		ProductUpdateResponseDto responseDto = ProductUpdateResponseDto.from(result);
		return ResponseEntity.ok(RsData.success(responseDto));
	}

	// 상품 단건 조회
	@GetMapping("/{id}")
	public ResponseEntity<RsData<ProductDto>> getProduct(
		@PathVariable("id") Long id
	) {
		ProductResult result = productGetUseCase.getProduct(id);
		ProductDto responseDto = ProductDto.from(result);
		return ResponseEntity.ok(RsData.success(responseDto));
	}

	// 상품 검색
	@GetMapping("/search")
	public ResponseEntity<RsData<PageResponse<ProductDto>>> searchProducts(
		@Valid @ModelAttribute ProductSearchDto searchDto
	) {
		PageResponse<ProductResult> resultPage = productSearchUseCase.searchProducts(searchDto);
		List<ProductDto> dtoList = resultPage.content().stream()
			.map(ProductDto::from)
			.collect(Collectors.toList());
		var response = PageResponse.of(dtoList, resultPage.pageNumber(), resultPage.pageSize(),
			resultPage.totalElements());
		return ResponseEntity.ok(RsData.success(response));
	}

	// (판매자) 나의 상품 조회
	@GetMapping("/my")
	@PreAuthorize("hasRole('SELLER')")
	public ResponseEntity<RsData<PageResponse<MyProductDto>>> searchMyProducts(
		@CurrentMemberId Long sellerId,
		@Valid @ModelAttribute MyProductSearchDto searchDto
	) {
		PageResponse<MyProductResult> resultPage = productSearchUseCase.searchMyProducts(sellerId, searchDto);
		List<MyProductDto> dtoList = resultPage.content().stream()
			.map(MyProductDto::from)
			.collect(Collectors.toList());
		var response = PageResponse.of(dtoList, resultPage.pageNumber(), resultPage.pageSize(),
			resultPage.totalElements());
		return ResponseEntity.ok(RsData.success(response));
	}

	// (판매자) 재고 이력 조회
	@GetMapping("/my/stock-histories")
	@PreAuthorize("hasRole('SELLER')")
	public ResponseEntity<RsData<PageResponse<StockHistoryDto>>> searchStockHistories(
		@CurrentMemberId Long sellerId,
		@Valid @ModelAttribute StockHistorySearchDto searchDto
	) {
		PageResponse<StockHistoryDto> stockHistories = productStockHistoryUseCase.searchStockHistories(sellerId,
			searchDto);
		return ResponseEntity.ok(RsData.success(stockHistories));
	}
}
