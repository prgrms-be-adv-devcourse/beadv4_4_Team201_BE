package in.product;

import static org.springframework.http.HttpStatus.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.product.ProductFacade;
import domain.FundingMember;
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
	public ResponseEntity<String> createProduct(@RequestBody ProductCreateRequestDto requestDto) { //todo response 형태
		FundingMember seller = new FundingMember(1L); // todo auth

		productFacade.createProduct(seller, requestDto);
		// productFacade.createProduct(sellerId, requestDto); // todo auth
		log.info("상품 생성 완료");
		return ResponseEntity.status(CREATED).body("상품 생성 완료");
	}

	// 상품 등록 승인 (관리자)
	// todo 관리자 권한만 승인 가능하도록 인가 적용
	@PatchMapping("/{id}/approve")
	public ResponseEntity<String> approveProduct(@PathVariable Long id) {
		productFacade.approveProduct(id);

		return ResponseEntity.status(OK).body("상품 등록을 승인하였습니다. 상품 ID: " + id);
	}

	// 상품 목록 조회
	// todo 페이징
	@GetMapping
	public ResponseEntity<List<ProductDto>> getProducts() {
		List<ProductDto> allProducts = productFacade.getProducts();
		return ResponseEntity.status(OK).body(allProducts);
	}

	// 상품 단건 조회

	// 상품 검색
	// todo 엘라스틱서치
	@GetMapping("/search")
	public ResponseEntity<Page<ProductDto>> search(
		@RequestParam(required = false) String keyword,
		@RequestParam(required = false) Integer minPrice,
		@RequestParam(required = false) int maxPrice,
		@RequestParam(required = false) boolean inStock,
		@RequestParam(required = false, defaultValue = "latest") String sort,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size
	) {
		var searchQuery = new ProductSearchDto(keyword, minPrice, maxPrice, inStock, sort, page, size);
		Page<ProductDto> searchResult = productFacade.search(searchQuery);

		return ResponseEntity.status(OK).body(searchResult);
	}

}
