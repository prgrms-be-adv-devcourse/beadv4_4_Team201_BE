package app.giftify.app.product;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.domain.FundingMember;
import app.giftify.in.product.ProductCreateRequestDto;
import app.giftify.in.product.ProductDto;
import app.giftify.in.product.ProductSearchDto;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductFacade {
	private final ProductCreateUseCase productCreateUseCase;
	private final ProductGetUseCase getProductsUseCase;
	private final ProductSearchUseCase productSearchUseCase;
	private final ProductApproveUseCase productApproveUseCase;

	@Transactional
	public void createProduct(FundingMember seller, ProductCreateRequestDto requestDto) {
		productCreateUseCase.createProduct(seller, requestDto);
	}

	@Transactional(readOnly = true)
	public List<ProductDto> getProducts() {
		return getProductsUseCase.getProducts();
	}

	@Transactional(readOnly = true)
	public Page<ProductDto> search(ProductSearchDto searchDto) {
		return productSearchUseCase.search(searchDto);
	}

	@Transactional
	public void approveProduct(Long id) {
		productApproveUseCase.approveProduct(id);
	}
}
