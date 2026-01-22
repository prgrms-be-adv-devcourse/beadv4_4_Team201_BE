package app.giftify.wishlist.adapter.out.jpa.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import app.giftify.wishlist.adapter.out.jpa.entity.WishlistItemJpaEntity;
import app.giftify.wishlist.adapter.out.jpa.mapper.WishlistItemMapper;
import app.giftify.wishlist.adapter.out.jpa.repository.WishlistItemJpaRepository;
import app.giftify.wishlist.application.port.out.WishlistItemRepositoryPort;
import app.giftify.wishlist.core.domain.WishlistItem;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WishlistItemAdapter implements WishlistItemRepositoryPort {
	private final WishlistItemJpaRepository wishlistItemRepository;

	// @Override
	// public Optional<WishlistItem> findByAuthSubAndProductId(String authSub, Long productId) {
	//     return wishlistItemRepository.findByAuthSubAndProductId(authSub, productId)
	//             .map(WishlistItemMapper::toDomain);
	// }

	@Override
	public Optional<WishlistItem> findByWishlistIdAndProductId(Long wishlistId, Long productId) {
		return wishlistItemRepository.findByWishlistIdAndProductId(wishlistId, productId)
			.map(WishlistItemMapper::toDomain);
	}

	@Override
	public Optional<WishlistItem> findById(Long id) {
		return wishlistItemRepository.findById(id)
			.map(WishlistItemMapper::toDomain);
	}

	@Override
	public List<WishlistItem> findByWishlistId(Long wishlistId) {
		return wishlistItemRepository.findByWishlistId(wishlistId)
			.stream()
			.map(WishlistItemMapper::toDomain)
			.toList();
	}

	@Override
	public WishlistItem save(WishlistItem wishlistItem) {
		WishlistItemJpaEntity entity = WishlistItemMapper.toEntity(wishlistItem);
		WishlistItemJpaEntity savedEntity = wishlistItemRepository.save(entity);
		return WishlistItemMapper.toDomain(savedEntity);
	}

	@Override
	public void deleteByWishlistIdAndProductId(Long wishlistId, Long productId) {
		wishlistItemRepository.deleteByWishlistIdAndProductId(wishlistId, productId);
	}

	// @Override
	// public void deleteByAuthSubAndProductId(String authSub, Long productId) {
	// 	wishlistItemRepository.deleteByAuthSubAndProductId(authSub, productId);
	// }

	@Override
	public void delete(WishlistItem wishlistItem) {
		wishlistItemRepository.deleteById(wishlistItem.getId());
	}

	@Override
	public Long count() {
		return wishlistItemRepository.count();
	}
}
