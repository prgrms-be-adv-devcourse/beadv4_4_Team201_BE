package app.giftify.wishlist.adapter.out.entity;

import app.giftify.wishlist.adapter.out.jpa.entity.WishlistItemJpaEntity;
import app.giftify.wishlist.core.domain.ItemStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WishlistItemJpaEntityTest {

    @Test
    @DisplayName("WishlistItemJpaEntity Builder 및 Getter 테스트")
    void entityTest() {
        WishlistItemJpaEntity entity = WishlistItemJpaEntity.builder()
                .authSub("user123")
                .productId(100L)
                .itemStatus(ItemStatus.ACTIVE)
                .build();

        assertThat(entity.getAuthSub()).isEqualTo("user123");
        assertThat(entity.getProductId()).isEqualTo(100L);
        assertThat(entity.getItemStatus()).isEqualTo(ItemStatus.ACTIVE);
    }
}
