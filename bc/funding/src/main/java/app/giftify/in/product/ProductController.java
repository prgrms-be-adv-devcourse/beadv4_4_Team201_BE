package app.giftify.in.product;

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
import app.giftify.shared.api.paging.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
@Slf4j
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

	// 상품 등록 승인 (관리자)
	// todo 관리자 권한만 승인 가능하도록 인가 적용
	@PatchMapping("/{id}/approve")
	public ResponseEntity<String> approveProduct(@PathVariable Long id) {
		productFacade.approveProduct(id);
		return ResponseEntity.status(OK).body("상품 등록을 승인하였습니다. 상품 ID: " + id);
	}

	// 상품 단건 조회
	@GetMapping("/{id}")
	public ResponseEntity<ProductDto> getProduct(@PathVariable Long id) {
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
	@GetMapping("/me")
	public ResponseEntity<PageResponse<ProductDto>> searchMyProducts(
		@RequestParam Long sellerId, //todo auth memberId
		@ModelAttribute MyProductSearchDto searchDto
	) {
		PageResponse<ProductDto> myProducts = productFacade.searchMyProducts(sellerId, searchDto);
		return ResponseEntity.status(OK).body(myProducts);
	}

}
