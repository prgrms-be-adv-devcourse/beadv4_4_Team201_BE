package app.giftify.in.product;

import static app.giftify.domain.product.exception.ProductErrorCode.*;
import static org.springframework.http.HttpStatus.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.giftify.app.product.ProductFacade;
import app.giftify.domain.FundingMember;
import app.giftify.domain.product.exception.ProductException;
import app.giftify.shared.api.paging.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {
	private final ProductFacade productFacade;

	// 상품 등록
	@PostMapping
	public ResponseEntity<ProductDto> createProduct(
		@Valid @RequestBody ProductCreateRequestDto requestDto
	) {
		FundingMember seller = new FundingMember(1L); // todo auth

		ProductDto productDto = productFacade.createProduct(seller, requestDto);
		// productFacade.createProduct(sellerId, requestDto); // todo auth
		return ResponseEntity.status(CREATED).body(productDto);
	}

	// 상품 등록 승인 및 거절 (관리자) // todo 관리자 인가
	@PatchMapping("/{id}/{action}")
	public ResponseEntity<String> changeApproval(
		@PathVariable Long id,
		@PathVariable String action
	) {
		switch (action) {
			case "approve" -> {
				productFacade.approveProduct(id);
				return ResponseEntity.status(OK).body("상품 등록 승인, 상품 ID: " + id);
			}
			case "reject" -> {
				productFacade.rejectProduct(id);
				return ResponseEntity.status(OK).body("상품 등록 거절, 상품 ID: " + id);
			}
			default -> throw new ProductException(INVALID_APPROVAL_ACTION);
		}
	}

	// 상품 수정
	@PatchMapping("/my/{productId}")
	public ResponseEntity<ProductUpdateResponseDto> updateProduct(
		@PathVariable Long productId,
		@RequestParam Long sellerId, // todo jwt로 memberId get
		@RequestBody ProductUpdateRequestDto requestDto
	) {
		ProductUpdateResponseDto productDto = productFacade.updateProduct(productId, sellerId, requestDto);

		return ResponseEntity.status(OK).body(productDto);
	}

	// 상품 단건 조회
	@GetMapping("/{id}")
	public ResponseEntity<ProductDto> getProduct(
		@PathVariable Long id
	) {
		ProductDto product = productFacade.getProduct(id);
		return ResponseEntity.status(OK).body(product);
	}

	// 상품 검색
	// todo 엘라스틱서치
	@GetMapping("/search")
	public ResponseEntity<PageResponse<ProductDto>> searchProducts(
		@ModelAttribute ProductSearchDto searchDto
	) {
		PageResponse<ProductDto> searchResult = productFacade.searchProducts(searchDto);
		return ResponseEntity.status(OK).body(searchResult);
	}

	// (판매자) 나의 상품 조회
	@GetMapping("/my")
	public ResponseEntity<PageResponse<ProductDto>> searchMyProducts(
		@RequestParam Long sellerId, //todo auth memberId
		@ModelAttribute MyProductSearchDto searchDto
	) {
		PageResponse<ProductDto> myProducts = productFacade.searchMyProducts(sellerId, searchDto);
		return ResponseEntity.status(OK).body(myProducts);
	}

}
