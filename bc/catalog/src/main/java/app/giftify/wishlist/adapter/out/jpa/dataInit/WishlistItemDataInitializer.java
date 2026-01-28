package app.giftify.wishlist.adapter.out.jpa.dataInit;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import app.giftify.wishlist.adapter.out.jpa.entity.WishlistItemJpaEntity;
import app.giftify.wishlist.adapter.out.jpa.repository.WishlistItemJpaRepository;
import app.giftify.wishlist.core.domain.WishlistItemStatus;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WishlistItemDataInitializer implements ApplicationRunner {
	private final WishlistItemJpaRepository wishlistItemJpaRepository;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		if (wishlistItemJpaRepository.count() > 0) {
			return;
		}

		WishlistItemJpaEntity wishlistItem1 = WishlistItemJpaEntity.builder()
			.wishlistId(1L)
			.productId(1L)
			.wishlistItemStatus(WishlistItemStatus.PENDING)
			.build();
		wishlistItemJpaRepository.save(wishlistItem1);

		WishlistItemJpaEntity wishlistItem2 = WishlistItemJpaEntity.builder()
			.wishlistId(1L)
			.productId(2L)
			.wishlistItemStatus(WishlistItemStatus.IN_PROGRESS)
			.build();
		wishlistItemJpaRepository.save(wishlistItem2);

		WishlistItemJpaEntity wishlistItem3 = WishlistItemJpaEntity.builder()
			.wishlistId(1L)
			.productId(3L)
			.wishlistItemStatus(WishlistItemStatus.PENDING)
			.build();
		wishlistItemJpaRepository.save(wishlistItem3);
	}
}
