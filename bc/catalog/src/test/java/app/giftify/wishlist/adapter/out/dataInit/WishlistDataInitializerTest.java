package app.giftify.wishlist.adapter.out.dataInit;

import app.giftify.wishlist.adapter.out.jpa.dataInit.WishlistDataInitializer;
import app.giftify.wishlist.adapter.out.jpa.repository.WishlistJpaRepository;
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
class WishlistDataInitializerTest {

    @Mock
    private WishlistJpaRepository wishlistJpaRepository;

    @InjectMocks
    private WishlistDataInitializer wishlistDataInitializer;

    @Test
    @DisplayName("데이터가 없을 때 초기 데이터 저장 테스트")
    void run_WhenNoData_SaveInitialData() throws Exception {
        // given
        given(wishlistJpaRepository.count()).willReturn(0L);
        ApplicationArguments args = mock(ApplicationArguments.class);

        // when
        wishlistDataInitializer.run(args);

        // then
        verify(wishlistJpaRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("데이터가 이미 존재할 때 저장하지 않음")
    void run_WhenDataExists_DoNotSave() throws Exception {
        // given
        given(wishlistJpaRepository.count()).willReturn(1L);
        ApplicationArguments args = mock(ApplicationArguments.class);

        // when
        wishlistDataInitializer.run(args);

        // then
        verify(wishlistJpaRepository, never()).save(any());
    }
}
