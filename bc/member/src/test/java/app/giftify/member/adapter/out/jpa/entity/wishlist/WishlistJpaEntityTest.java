package app.giftify.member.adapter.out.jpa.entity.wishlist;

import app.giftify.member.core.domain.wishlist.Visibility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WishlistJpaEntityTest {

    @Test
    @DisplayName("WishlistJpaEntity Builder 및 Getter 테스트")
    void entityTest() {
        WishlistJpaEntity entity = WishlistJpaEntity.builder()
                .id(1L)
                .authSub("user123")
                .memberId(10L)
                .visibility(Visibility.PUBLIC)
                .build();

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getAuthSub()).isEqualTo("user123");
        assertThat(entity.getMemberId()).isEqualTo(10L);
        assertThat(entity.getVisibility()).isEqualTo(Visibility.PUBLIC);
    }
}
