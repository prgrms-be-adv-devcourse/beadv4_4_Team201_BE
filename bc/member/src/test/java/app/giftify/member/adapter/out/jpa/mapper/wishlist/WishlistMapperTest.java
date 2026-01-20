package app.giftify.member.adapter.out.jpa.mapper.wishlist;

import app.giftify.member.adapter.out.jpa.entity.wishlist.WishlistJpaEntity;
import app.giftify.member.core.domain.wishlist.Visibility;
import app.giftify.member.core.domain.wishlist.Wishlist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WishlistMapperTest {

    @Test
    @DisplayName("Wishlist 도메인을 Entity로 변환 테스트")
    void toEntityTest() {
        Wishlist domain = Wishlist.builder()
                .id(1L)
                .authSub("user123")
                .memberId(10L)
                .visibility(Visibility.PUBLIC)
                .build();

        WishlistJpaEntity entity = WishlistMapper.toEntity(domain);

        assertThat(entity.getId()).isEqualTo(domain.getId());
        assertThat(entity.getAuthSub()).isEqualTo(domain.getAuthSub());
        assertThat(entity.getMemberId()).isEqualTo(domain.getMemberId());
        assertThat(entity.getVisibility()).isEqualTo(domain.getVisibility());
    }

    @Test
    @DisplayName("Wishlist Entity를 도메인으로 변환 테스트")
    void toDomainTest() {
        WishlistJpaEntity entity = WishlistJpaEntity.builder()
                .id(1L)
                .authSub("user123")
                .memberId(10L)
                .visibility(Visibility.PRIVATE)
                .build();

        Wishlist domain = WishlistMapper.toDomain(entity);

        assertThat(domain.getId()).isEqualTo(entity.getId());
        assertThat(domain.getAuthSub()).isEqualTo(entity.getAuthSub());
        assertThat(domain.getMemberId()).isEqualTo(entity.getMemberId());
        assertThat(domain.getVisibility()).isEqualTo(entity.getVisibility());
    }
}
