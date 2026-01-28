package app.giftify.funding.app.product;

import static app.giftify.funding.domain.product.exception.ProductErrorCode.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import app.giftify.funding.domain.product.Product;
import app.giftify.funding.domain.product.ProductSnapshot;
import app.giftify.funding.domain.product.exception.ProductException;
import app.giftify.funding.in.product.ProductSnapshotDto;
import app.giftify.funding.in.product.ProductSnapshotRequestDto;
import app.giftify.funding.out.product.ProductSnapshotRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductSnapshotCreateUseCase {
	private final ProductSupport productSupport;
	private final ProductSnapshotRepository productSnapshotRepository;

	/**
	 * 장바구니에 담긴 상품들을 결제할 때, 상품들을 스냅샷으로 저장하여 전달
	 */
	public List<ProductSnapshotDto> createProductSnapshots(ProductSnapshotRequestDto requestDto) {
		List<Long> productIds = requestDto.items().stream()
			.map(ProductSnapshotRequestDto.SnapshotItem::productId)
			.toList();

		List<Product> products = productSupport.findAllById(productIds);

		if (products.size() != productIds.size()) {
			throw new ProductException(PRODUCTS_NOT_FOUND);
		}

		// 요청 순서 보장을 위해 Map으로 변환
		Map<Long, Product> productMap = products.stream()
			.collect(Collectors.toMap(Product::getId, Function.identity()));

		// 요청된 productIds 순서대로 스냅샷 생성
		return productIds.stream()
			.map(productId -> {
				Product product = productMap.get(productId);
				ProductSnapshot snapshot = productSnapshotRepository.save(ProductSnapshot.from(product));
				return ProductSnapshotDto.from(snapshot);
			}).toList();
	}
}
