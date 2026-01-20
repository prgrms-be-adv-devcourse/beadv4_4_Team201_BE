package app.giftify.wishlist.adapter.out.jpa.dataInit;

import app.giftify.wishlist.adapter.out.jpa.entity.WishlistItemJpaEntity;
import app.giftify.wishlist.adapter.out.jpa.repository.WishlistItemJpaRepository;
import app.giftify.wishlist.core.domain.ItemStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class wishlistItemDataInitializer implements ApplicationRunner {
    private final WishlistItemJpaRepository wishlistItemJpaRepository;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (wishlistItemJpaRepository.count() > 0) {
            return;
        }

        WishlistItemJpaEntity wishlistItem1 = WishlistItemJpaEntity.builder()
                .authSub("google-oauth2|104844495450678108304")
                .productId(1L)
                .itemStatus(ItemStatus.ACTIVE)
                .build();
        wishlistItemJpaRepository.save(wishlistItem1);

        WishlistItemJpaEntity wishlistItem2 = WishlistItemJpaEntity.builder()
                .authSub("google-oauth2|104844495450678108304")
                .productId(2L)
                .itemStatus(ItemStatus.ACTIVE)
                .build();
        wishlistItemJpaRepository.save(wishlistItem2);

        WishlistItemJpaEntity wishlistItem3 = WishlistItemJpaEntity.builder()
                .authSub("google-oauth2|104844495450678108304")
                .productId(3L)
                .itemStatus(ItemStatus.DRAFT)
                .build();
        wishlistItemJpaRepository.save(wishlistItem3);
    }
}
