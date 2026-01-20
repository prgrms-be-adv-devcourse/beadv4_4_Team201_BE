package app.giftify.member.adapter.out.jpa.dataInit.wishlist;

import app.giftify.member.adapter.out.jpa.entity.wishlist.WishlistJpaEntity;
import app.giftify.member.adapter.out.jpa.respository.wishlist.WishlistJpaRepository;
import app.giftify.member.core.domain.wishlist.Visibility;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

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
                .authSub("google-oauth2|104844495450678108304")
                .memberId(1L)
                .visibility(Visibility.PRIVATE)
                .build();

        wishlistJpaRepository.save(wishlist);
    }

}