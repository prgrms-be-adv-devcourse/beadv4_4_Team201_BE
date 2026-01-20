package app.giftify.member.adapter.out.jpa.adapter.wishlist;

import app.giftify.member.adapter.out.jpa.entity.wishlist.WishlistJpaEntity;
import app.giftify.member.adapter.out.jpa.respository.wishlist.WishlistJpaRepository;
import app.giftify.member.core.domain.wishlist.Visibility;
import app.giftify.member.core.domain.wishlist.Wishlist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WishlistAdapterTest {

    @Mock
    private WishlistJpaRepository wishlistRepository;

    @InjectMocks
    private WishlistAdapter wishlistAdapter;

    @Test
    @DisplayName("memberId로 위시리스트 조회 테스트")
    void findByMemberIdTest() {
        Long memberId = 10L;
        WishlistJpaEntity entity = WishlistJpaEntity.builder()
                .id(1L)
                .authSub("user123")
                .memberId(memberId)
                .visibility(Visibility.PUBLIC)
                .build();
        given(wishlistRepository.findById(memberId)).willReturn(Optional.of(entity));

        Optional<Wishlist> result = wishlistAdapter.findByMemberId(memberId);

        assertThat(result).isPresent();
        assertThat(result.get().getMemberId()).isEqualTo(memberId);
    }

    @Test
    @DisplayName("authSub로 위시리스트 조회 테스트")
    void findByAuthSubTest() {
        String authSub = "user123";
        WishlistJpaEntity entity = WishlistJpaEntity.builder()
                .id(1L)
                .authSub(authSub)
                .memberId(10L)
                .visibility(Visibility.PUBLIC)
                .build();
        given(wishlistRepository.findByAuthSub(authSub)).willReturn(Optional.of(entity));

        Optional<Wishlist> result = wishlistAdapter.findByAuthSub(authSub);

        assertThat(result).isPresent();
        assertThat(result.get().getAuthSub()).isEqualTo(authSub);
    }

    @Test
    @DisplayName("위시리스트 저장 테스트")
    void saveTest() {
        Wishlist domain = Wishlist.builder()
                .id(1L)
                .authSub("user123")
                .memberId(10L)
                .visibility(Visibility.PUBLIC)
                .build();
        WishlistJpaEntity entity = WishlistJpaEntity.builder()
                .id(1L)
                .authSub("user123")
                .memberId(10L)
                .visibility(Visibility.PUBLIC)
                .build();
        given(wishlistRepository.save(any(WishlistJpaEntity.class))).willReturn(entity);

        Wishlist result = wishlistAdapter.save(domain);

        assertThat(result.getAuthSub()).isEqualTo(domain.getAuthSub());
        verify(wishlistRepository).save(any(WishlistJpaEntity.class));
    }
}
