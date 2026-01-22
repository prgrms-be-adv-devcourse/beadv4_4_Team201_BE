package app.giftify.wishlist.adapter.out.jpa.adapter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import app.giftify.wishlist.adapter.out.api.dto.ProductResponse;
import app.giftify.wishlist.application.port.out.WishlistProductQueryPort;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WishlistProductQueryAdapter implements WishlistProductQueryPort {

	private final RestClient restClient;

	public WishlistProductQueryAdapter(
		RestClient.Builder restClientBuilder,
		@Value("${custom.global.internalBackUrl:http://localhost:8080}") String internalBackUrl
	) {
		this.restClient = restClientBuilder
			.baseUrl(internalBackUrl)
			.build();
	}

	@Override
	public ProductStatus getProductStatus(Long productId) {
		log.info("[Wishlist] Fallback API Call[/api/products/{id}]: productId: {}", productId);
		try {
			ProductResponse productResponse = restClient.get()
				.uri("/api/products/{id}", productId)
				.retrieve()
				.body(ProductResponse.class);

			if (productResponse == null) {
				return new ProductStatus(productId, false, "Unknown", 0, "Unknown");
			}

			// ACTIVE일 때만 가능
			boolean onSale = "ACTIVE".equals(productResponse.status());

			return new ProductStatus(
				productId,
				onSale,
				productResponse.name(),
				productResponse.price(),
				productResponse.sellerNickname()
			);
		} catch (Exception e) {
			log.error("[Wishlist] 위시리스트에서 상품 정보를 조회하는 API 호출에 실패했습니다 | productId: {}", productId, e);
			return new ProductStatus(productId, false, "Unknown", 0, "Unknown");
		}
	}
}
