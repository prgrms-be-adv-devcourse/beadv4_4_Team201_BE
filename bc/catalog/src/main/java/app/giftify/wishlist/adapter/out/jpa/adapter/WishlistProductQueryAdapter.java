//package app.giftify.wishlist.adapter.out.jpa.adapter;
//
//import java.util.Optional;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.HttpClientErrorException;
//import org.springframework.web.client.RestClient;
//
//import app.giftify.wishlist.adapter.out.api.dto.ProductResponse;
//import app.giftify.wishlist.application.port.out.WishlistProductQueryPort;
//import app.giftify.wishlist.core.domain.replica.WishlistProductReplica;
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//@Component
//public class WishlistProductQueryAdapter implements WishlistProductQueryPort {
//
//	private final RestClient restClient;
//
//	public WishlistProductQueryAdapter(
//		RestClient.Builder restClientBuilder,
//		@Value("${custom.global.internalBackUrl:http://localhost:8080}") String internalBackUrl
//	) {
//		this.restClient = restClientBuilder
//			.baseUrl(internalBackUrl)
//			.build();
//	}
//
//	@Override
//	public ProductStatus getProductStatus(Long productId, Optional<WishlistProductReplica> replica) {
//		log.info("[Wishlist] Fallback API Call[/api/products/{id}]: productId: {}", productId);
//		try {
//			ProductResponse productResponse = restClient.get()
//				.uri("/api/products/{id}", productId)
//				.retrieve()
//				.body(ProductResponse.class);
//
//			// ACTIVE 상태의 상품만 응답한다.
//			if (productResponse == null) {
//				return replica.map(wishlistProductReplica -> ProductStatus.apiFailedKeepReplica(productId,
//						wishlistProductReplica.getName(), wishlistProductReplica.getPrice(),
//						wishlistProductReplica.getSellerNickname()))
//					.orElseGet(() -> ProductStatus.apiFailedNoReplica(productId));
//			}
//
//			return ProductStatus.fromApi(productId, productResponse);
//
//		} catch (HttpClientErrorException.BadRequest e) {
//			// 상품이 판매 중이 아님 (ProductException → 400 Bad Request)
//			log.warn("[Wishlist] 상품이 판매 중이 아닙니다 | productId: {}", productId);
//
//			return replica.map(wishlistProductReplica -> ProductStatus.apiFailedKeepReplica(
//					productId,
//					wishlistProductReplica.getName(),
//					wishlistProductReplica.getPrice(),
//					wishlistProductReplica.getSellerNickname()
//				))
//				.orElseGet(() -> ProductStatus.apiFailedNoReplica(productId));
//
//		} catch (Exception e) {
//			// 404, 네트워크 오류, 서버 오류 등 예상치 못한 예외
//			log.error("[Wishlist] 상품 API 호출 중 오류 발생 | productId: {}", productId, e);
//			throw new RuntimeException("상품 정보 조회에 실패했습니다", e);
//		}
//	}
//}