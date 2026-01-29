package app.giftify.wishlist.application.port.out;

import java.util.Optional;

import app.giftify.wishlist.adapter.out.api.dto.ProductResponse;
import app.giftify.wishlist.core.domain.replica.WishlistProductReplica;

public interface WishlistProductQueryPort {
	// bc:funding 모듈 API 호출용
	ProductStatus getProductStatus(Long productId, Optional<WishlistProductReplica> replica);

	record ProductStatus(
		Long productId,
		boolean onSale,
		String name,
		Integer price,
		String sellerNickName

	) {
		// 정상 조회
		public static ProductStatus fromApi(Long productId, ProductResponse res) {
			return new ProductStatus(productId, true, res.name(), res.price(), res.sellerNickname());
		}

		// 조회 오류 - 기존 레플리카 O
		public static ProductStatus apiFailedKeepReplica(
			Long productId, String name, Integer price, String sellerNickName
		) {
			return new ProductStatus(productId, false, name, price, sellerNickName);
		}

		// 조회 오류 - 기존 레플리카 X
		public static ProductStatus apiFailedNoReplica(Long productId) {
			return new ProductStatus(productId, false, null, null, null);
		}
	}
}
