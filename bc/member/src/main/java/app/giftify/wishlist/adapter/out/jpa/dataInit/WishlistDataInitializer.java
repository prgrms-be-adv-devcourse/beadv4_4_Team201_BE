package app.giftify.wishlist.adapter.out.jpa.dataInit;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import app.giftify.wishlist.adapter.out.jpa.entity.WishlistJpaEntity;
import app.giftify.wishlist.adapter.out.jpa.repository.WishlistJpaRepository;
import app.giftify.wishlist.core.domain.Visibility;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WishlistDataInitializer implements ApplicationRunner {

	private final WishlistJpaRepository wishlistJpaRepository;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		if (wishlistJpaRepository.count() > 0) {
			return;
		}

		WishlistJpaEntity wishlist = WishlistJpaEntity.builder()
			.memberId(1L)
			.visibility(Visibility.PRIVATE)
			.build();

		wishlistJpaRepository.save(wishlist);
	}

}