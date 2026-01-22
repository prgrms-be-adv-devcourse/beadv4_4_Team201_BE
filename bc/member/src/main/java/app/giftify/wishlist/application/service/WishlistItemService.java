package app.giftify.wishlist.application.service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.wishlist.application.port.in.AddWishlistItemUseCase;
import app.giftify.wishlist.application.port.in.GetWishlistItemUseCase;
import app.giftify.wishlist.application.port.in.RemoveWishlistItemUseCase;
import app.giftify.wishlist.application.port.out.WishlistItemRepositoryPort;
import app.giftify.wishlist.application.port.out.WishlistProductQueryPort;
import app.giftify.wishlist.application.port.out.WishlistProductReplicaPort;
import app.giftify.wishlist.application.port.out.WishlistRepositoryPort;
import app.giftify.wishlist.core.domain.Wishlist;
import app.giftify.wishlist.core.domain.WishlistItem;
import app.giftify.wishlist.core.domain.exception.DuplicateWishlistItemException;
import app.giftify.wishlist.core.domain.exception.ProductNotOnSaleException;
import app.giftify.wishlist.core.domain.exception.WishlistNotFoundException;
import app.giftify.wishlist.core.domain.replica.WishlistProductReplica;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistItemService implements AddWishlistItemUseCase, GetWishlistItemUseCase, RemoveWishlistItemUseCase {

	private final WishlistItemRepositoryPort wishlistItemRepositoryPort;
	private final WishlistProductReplicaPort wishlistProductReplicaPort;
	private final WishlistProductQueryPort wishlistProductQueryPort;
	private final WishlistRepositoryPort wishlistRepositoryPort;
	// private final EventPublisher eventPublisher; // todo

	private static final java.time.Duration REPLICA_TTL = Duration.ofHours(1);

	/**
	 * 위시리스트아이템 개수 카운팅
	 */
	@Override
	@Transactional(readOnly = true)
	public Long getWishlistItemCount() {
		return wishlistItemRepositoryPort.count();
	}

	/**
	 * 위시리스트아이템 단건 조회
	 */
	@Override
	@Transactional(readOnly = true)
	public boolean isItemExists(Long memberId, Long productId) {
		Wishlist wishlist = getWishlistByMemberIdOrThrow(memberId);

		Optional<WishlistItem> wishlistItem = wishlistItemRepositoryPort.findByWishlistIdAndProductId(
			wishlist.getId(),
			productId
		);
		return wishlistItem.isPresent();
	}

	/**
	 * 위시리스트아이템 목록 조회
	 */
	@Override
	@Transactional(readOnly = true)
	public List<WishlistItem> getWishlistItems(Long memberId) {
		Wishlist wishlist = getWishlistByMemberIdOrThrow(memberId);
		return wishlistItemRepositoryPort.findByWishlistId(wishlist.getId());
	}

	/**
	 * 위시리스트아이템 추가
	 */
	@Override
	@Transactional
	public WishlistItem addWishlistItem(Long memberId, WishlistItemAddCommand command) {
		Wishlist wishlist = getWishlistByMemberIdOrThrow(memberId);

		// 위시리스트 DB에 상품 id가 있는지 조회 (중복 확인)
		if (wishlistItemRepositoryPort.findByWishlistIdAndProductId(wishlist.getId(), command.productId())
			.isPresent()) {
			throw new DuplicateWishlistItemException(wishlist.getId(), command.productId());
		}

		// 상품 레플리카 조회 및 상태 검증
		// 없으면 레플리카 생성
		validateProductActive(command.productId());

		// 3. 위시리스트 item에 등록
		WishlistItem wishlistItem = WishlistItem.builder()
			// .authSub(command.authSub())
			.wishlistId(wishlist.getId())
			.productId(command.productId())
			.wishlistItemStatus(command.wishListItemStatus())
			.build();

		// WishlistItem addedWishlistItem = wishlistItemRepositoryPort.save(wishlistItem);
		//
		// // 4. 이벤트 발행 (현재 구현 생략)

		// 5. 새롭게 생성된 WishlistItem 반환
		return wishlistItemRepositoryPort.save(wishlistItem);
	}

	// 상품이 판매 중인지 검증
	private void validateProductActive(Long productId) {
		Optional<WishlistProductReplica> replica = wishlistProductReplicaPort.findByProductId(productId);

		// 레플리카가 있고 신선한(Fresh) 상태라면 바로 사용
		if (replica.isPresent() && replica.get().isFresh(REPLICA_TTL)) {
			if (!replica.get().isWishlistAllowed()) {
				throw new ProductNotOnSaleException(productId);
			}
			return;
		}

		// 레플리카가 없거나 만료된 경우 Fallback (직접 조회) //todo
		// 요구사항: /api/product/{id}로 API 호출해서 가져온 후 db에 저장/갱신
		WishlistProductQueryPort.ProductStatus status = wishlistProductQueryPort.getProductStatus(productId);

		// 조회된 정보로 레플리카 갱신 (Upsert)
		WishlistProductReplica newReplica = WishlistProductReplica.builder()
			.productId(productId)
			.wishlistAllowed(status.onSale())
			.name(status.name())
			.price(status.price())
			.sellerNickname(status.sellerNickName())
			.build();
		wishlistProductReplicaPort.upsert(newReplica);

		if (!status.onSale()) {
			throw new ProductNotOnSaleException(productId);
		}
	}

	/**
	 * 위시리스트아이템 수동 삭제
	 * todo PENDING, NO_THANKS 상태일 때만 수동 삭제 가능
	 */
	@Override
	@Transactional
	public void removeWishlistItem(WishlistItemRemoveCommand command) {
		Wishlist wishlist = getWishlistByMemberIdOrThrow(command.memberId());

		WishlistItem wishlistItem = wishlistItemRepositoryPort
			.findByWishlistIdAndProductId(wishlist.getId(), command.productId())
			.orElseThrow(() -> new WishlistNotFoundException(command.memberId()));

		wishlistItemRepositoryPort.delete(wishlistItem);

		// 위시리스트 아이템 삭제 이벤트 발행
	}

	/**
	 * memberId로 Wishlist 조회, 없으면 예외 발생
	 */
	private Wishlist getWishlistByMemberIdOrThrow(Long memberId) {
		return wishlistRepositoryPort.findByMemberId(memberId)
			.orElseThrow(() -> new WishlistNotFoundException(memberId));
	}
}
