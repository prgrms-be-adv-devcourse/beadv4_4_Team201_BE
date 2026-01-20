package app.giftify.wishlist.adapter.out.mapper;

import app.giftify.wishlist.adapter.out.jpa.entity.WishlistItemJpaEntity;
import app.giftify.wishlist.adapter.out.jpa.mapper.WishlistItemMapper;
import app.giftify.wishlist.core.domain.ItemStatus;
import app.giftify.wishlist.core.domain.WishlistItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WishlistItemMapperTest {

    @Test
    @DisplayName("WishlistItem 도메인을 Entity로 변환 테스트")
    void toEntityTest() {
        WishlistItem domain = WishlistItem.builder()
                .id(1L)
                .authSub("user123")
                .productId(100L)
                .itemStatus(ItemStatus.ACTIVE)
                .build();

        WishlistItemJpaEntity entity = WishlistItemMapper.toEntity(domain);

        assertThat(entity.getAuthSub()).isEqualTo(domain.getAuthSub());
        assertThat(entity.getProductId()).isEqualTo(domain.getProductId());
        assertThat(entity.getItemStatus()).isEqualTo(domain.getItemStatus());
    }

    @Test
    @DisplayName("WishlistItem Entity를 도메인으로 변환 테스트")
    void toDomainTest() {
        WishlistItemJpaEntity entity = WishlistItemJpaEntity.builder()
                .authSub("user123")
                .productId(100L)
                .itemStatus(ItemStatus.ACTIVE)
                .build();

        WishlistItem domain = WishlistItemMapper.toDomain(entity);

        assertThat(domain.getId()).isEqualTo(entity.getId());
        assertThat(domain.getAuthSub()).isEqualTo(entity.getAuthSub());
        assertThat(domain.getProductId()).isEqualTo(entity.getProductId());
        assertThat(domain.getItemStatus()).isEqualTo(entity.getItemStatus());
    }
}
