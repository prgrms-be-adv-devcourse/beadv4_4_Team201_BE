package app.giftify.wishlist.application.service;

import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.wishlist.application.port.in.UpdateWishlistSettingsUseCase;
import app.giftify.wishlist.application.port.out.WishlistRepositoryPort;
import app.giftify.wishlist.core.domain.Visibility;
import app.giftify.wishlist.core.domain.Wishlist;
import app.giftify.wishlist.core.domain.exception.WishlistNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistRepositoryPort wishlistRepositoryPort;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private WishlistService wishlistService;

    @Test
    @DisplayName("authSub로 위시리스트를 조회한다")
    void getWishlistByAuthSub() {
        // given
        String authSub = "auth0|123";
        Wishlist wishlist = Wishlist.builder().memberId(1L).authSub(authSub).build();
        given(wishlistRepositoryPort.findByAuthSub(authSub)).willReturn(Optional.of(wishlist));

        // when
        Optional<Wishlist> result = wishlistService.getWishlistByAuthSub(authSub);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getAuthSub()).isEqualTo(authSub);
    }

    @Test
    @DisplayName("위시리스트 설정을 변경한다")
    void updateSettings() {
        // given
        String authSub = "auth0|123";
        Visibility newVisibility = Visibility.PUBLIC;
        UpdateWishlistSettingsUseCase.UpdateSettingsCommand command =
                new UpdateWishlistSettingsUseCase.UpdateSettingsCommand(authSub, newVisibility);

        Wishlist wishlist = Wishlist.builder().memberId(1L).authSub(authSub).visibility(Visibility.PRIVATE).build();
        given(wishlistRepositoryPort.findByAuthSub(authSub)).willReturn(Optional.of(wishlist));
        given(wishlistRepositoryPort.save(any(Wishlist.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        Wishlist result = wishlistService.updateSettings(command);

        // then
        assertThat(result.getVisibility()).isEqualTo(newVisibility);
        verify(wishlistRepositoryPort).save(wishlist);
    }

    @Test
    @DisplayName("존재하지 않는 위시리스트의 설정을 변경하려 하면 예외가 발생한다")
    void updateSettingsFail() {
        // given
        String authSub = "auth0|not_found";
        UpdateWishlistSettingsUseCase.UpdateSettingsCommand command =
                new UpdateWishlistSettingsUseCase.UpdateSettingsCommand(authSub, Visibility.PUBLIC);

        given(wishlistRepositoryPort.findByAuthSub(authSub)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> wishlistService.updateSettings(command))
                .isInstanceOf(WishlistNotFoundException.class);
    }
}
