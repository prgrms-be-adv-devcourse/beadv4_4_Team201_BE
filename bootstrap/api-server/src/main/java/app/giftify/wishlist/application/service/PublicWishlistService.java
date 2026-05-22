package app.giftify.wishlist.application.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.wishlist.application.port.in.GetPublicWishlistUseCase;
import app.giftify.wishlist.application.port.out.WishlistItemRepositoryPort;
import app.giftify.wishlist.application.port.out.WishlistRepositoryPort;
import app.giftify.wishlist.core.domain.Visibility;
import app.giftify.wishlist.core.domain.Wishlist;
import app.giftify.wishlist.core.domain.WishlistItem;
import lombok.RequiredArgsConstructor;
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PublicWishlistService implements GetPublicWishlistUseCase {
	private final WishlistRepositoryPort wishlistRepositoryPort;
	private final WishlistItemRepositoryPort wishlistItemRepositoryPort;

	@Override
	public List<WishlistItem> getPublicWishlistItems(Long memberId) {
		return wishlistRepositoryPort.findByMemberIdAndVisibility(memberId, Visibility.PUBLIC)
			.map(wishlist -> wishlistItemRepositoryPort.findByWishlistId(wishlist.getId()))
			.orElse(Collections.emptyList());
	}

	@Override
	public List<Wishlist> findPublicWishlists(List<Long> memberIds) {
		if (memberIds.isEmpty()) {
			return Collections.emptyList();
		}
		return wishlistRepositoryPort.findByMemberIdInAndVisibility(memberIds, Visibility.PUBLIC);
	}
}
