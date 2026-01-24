package app.giftify.wishlist.adapter.out.jpa.adapter;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import app.giftify.wishlist.adapter.out.api.dto.ProductResponse;
import app.giftify.wishlist.application.port.out.WishlistProductQueryPort;
import app.giftify.wishlist.core.domain.replica.WishlistProductReplica;
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
	public ProductStatus getProductStatus(Long productId, Optional<WishlistProductReplica> replica) {
		log.info("[Wishlist] Fallback API Call[/api/products/{id}]: productId: {}", productId);
		try {
			ProductResponse productResponse = restClient.get()
				.uri("/api/products/{id}", productId)
				.retrieve()
				.body(ProductResponse.class);

			// ACTIVE 상태의 상품만 응답한다.
			if (productResponse == null) {
				return replica.map(wishlistProductReplica -> ProductStatus.apiFailedKeepReplica(productId,
						wishlistProductReplica.getName(), wishlistProductReplica.getPrice(),
						wishlistProductReplica.getSellerNickname()))
					.orElseGet(() -> ProductStatus.apiFailedNoReplica(productId));
			}

			return ProductStatus.fromApi(productId, productResponse);

		} catch (Exception e) { // 상품 조회 실패 (판매 중이 아님, 네트워크 오류 등)
			log.error("[Wishlist] 위시리스트에서 상품 정보를 조회하는 API 호출에 실패했습니다 | productId: {}", productId, e);

			return replica.map(wishlistProductReplica -> ProductStatus.apiFailedKeepReplica(
					productId,
					wishlistProductReplica.getName(),
					wishlistProductReplica.getPrice(),
					wishlistProductReplica.getSellerNickname()
				))
				.orElseGet(() -> ProductStatus.apiFailedNoReplica(productId));
		}
	}
}