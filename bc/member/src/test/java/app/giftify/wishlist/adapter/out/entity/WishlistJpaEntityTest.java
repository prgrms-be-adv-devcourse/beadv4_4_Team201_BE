package app.giftify.wishlist.adapter.out.entity;

import app.giftify.wishlist.adapter.out.jpa.entity.WishlistJpaEntity;
import app.giftify.wishlist.core.domain.Visibility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WishlistJpaEntityTest {

    @Test
    @DisplayName("WishlistJpaEntity Builder 및 Getter 테스트")
    void entityTest() {
        WishlistJpaEntity entity = WishlistJpaEntity.builder()
                .authSub("user123")
                .memberId(10L)
                .visibility(Visibility.PUBLIC)
                .build();

        assertThat(entity.getAuthSub()).isEqualTo("user123");
        assertThat(entity.getMemberId()).isEqualTo(10L);
        assertThat(entity.getVisibility()).isEqualTo(Visibility.PUBLIC);
    }
}
