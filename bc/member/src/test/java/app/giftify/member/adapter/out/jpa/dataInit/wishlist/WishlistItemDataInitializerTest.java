package app.giftify.member.adapter.out.jpa.dataInit.wishlist;

import app.giftify.member.adapter.out.jpa.respository.wishlist.WishlistItemJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistItemDataInitializerTest {

    @Mock
    private WishlistItemJpaRepository wishlistItemJpaRepository;

    @InjectMocks
    private wishlistItemDataInitializer wishlistItemDataInitializer;

    @Test
    @DisplayName("데이터가 없을 때 초기 아이템 저장 테스트")
    void run_WhenNoData_SaveInitialItems() throws Exception {
        // given
        given(wishlistItemJpaRepository.count()).willReturn(0L);
        ApplicationArguments args = mock(ApplicationArguments.class);

        // when
        wishlistItemDataInitializer.run(args);

        // then
        verify(wishlistItemJpaRepository, atLeastOnce()).save(any());
    }

    @Test
    @DisplayName("데이터가 이미 존재할 때 저장하지 않음")
    void run_WhenDataExists_DoNotSave() throws Exception {
        // given
        given(wishlistItemJpaRepository.count()).willReturn(3L);
        ApplicationArguments args = mock(ApplicationArguments.class);

        // when
        wishlistItemDataInitializer.run(args);

        // then
        verify(wishlistItemJpaRepository, never()).save(any());
    }
}
