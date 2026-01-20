package app.giftify.wishlist.adapter.out.adapter;

import app.giftify.wishlist.adapter.out.jpa.adapter.WishlistItemAdapter;
import app.giftify.wishlist.adapter.out.jpa.entity.WishlistItemJpaEntity;
import app.giftify.wishlist.adapter.out.jpa.repository.WishlistItemJpaRepository;
import app.giftify.wishlist.core.domain.ItemStatus;
import app.giftify.wishlist.core.domain.WishlistItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WishlistItemAdapterTest {

    @Mock
    private WishlistItemJpaRepository wishlistItemRepository;

    @InjectMocks
    private WishlistItemAdapter wishlistItemAdapter;

    @Test
    @DisplayName("authSub와 productId로 위시리스트 아이템 조회 테스트")
    void findByAuthSubAndProductIdTest() {
        String authSub = "user123";
        Long productId = 100L;
        WishlistItemJpaEntity entity = WishlistItemJpaEntity.builder()
                .authSub(authSub)
                .productId(productId)
                .itemStatus(ItemStatus.ACTIVE)
                .build();
        given(wishlistItemRepository.findByAuthSubAndProductId(authSub, productId)).willReturn(Optional.of(entity));

        Optional<WishlistItem> result = wishlistItemAdapter.findByAuthSubAndProductId(authSub, productId);

        assertThat(result).isPresent();
        assertThat(result.get().getProductId()).isEqualTo(productId);
    }

    @Test
    @DisplayName("authSub로 모든 위시리스트 아이템 조회 테스트")
    void findByAuthSubTest() {
        String authSub = "user123";
        WishlistItemJpaEntity entity = WishlistItemJpaEntity.builder()
                .authSub(authSub)
                .productId(100L)
                .itemStatus(ItemStatus.ACTIVE)
                .build();
        given(wishlistItemRepository.findByAuthSub(authSub)).willReturn(List.of(entity));

        List<WishlistItem> result = wishlistItemAdapter.findByAuthSub(authSub);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAuthSub()).isEqualTo(authSub);
    }

    @Test
    @DisplayName("위시리스트 아이템 저장 테스트")
    void saveTest() {
        WishlistItem domain = WishlistItem.builder()
                .authSub("user123")
                .productId(100L)
                .itemStatus(ItemStatus.ACTIVE)
                .build();
        WishlistItemJpaEntity entity = WishlistItemJpaEntity.builder()
                .authSub("user123")
                .productId(100L)
                .itemStatus(ItemStatus.ACTIVE)
                .build();
        given(wishlistItemRepository.save(any(WishlistItemJpaEntity.class))).willReturn(entity);

        WishlistItem result = wishlistItemAdapter.save(domain);

        assertThat(result.getProductId()).isEqualTo(domain.getProductId());
        verify(wishlistItemRepository).save(any(WishlistItemJpaEntity.class));
    }

    @Test
    @DisplayName("authSub와 productId로 삭제 테스트")
    void deleteByAuthSubAndProductIdTest() {
        String authSub = "user123";
        Long productId = 100L;

        wishlistItemAdapter.deleteByAuthSubAndProductId(authSub, productId);

        verify(wishlistItemRepository).deleteByAuthSubAndProductId(authSub, productId);
    }

    @Test
    @DisplayName("도메인 객체로 삭제 테스트")
    void deleteTest() {
        WishlistItem domain = WishlistItem.builder()
                .id(1L)
                .authSub("user123")
                .productId(100L)
                .itemStatus(ItemStatus.ACTIVE)
                .build();

        wishlistItemAdapter.delete(domain);

        verify(wishlistItemRepository).delete(any(WishlistItemJpaEntity.class));
    }

    @Test
    @DisplayName("카운트 테스트")
    void countTest() {
        given(wishlistItemRepository.count()).willReturn(10L);

        Long count = wishlistItemAdapter.count();

        assertThat(count).isEqualTo(10L);
    }
}
