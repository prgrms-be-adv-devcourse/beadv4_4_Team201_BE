package app.giftify.member.application.service.wishlist;

import app.giftify.member.application.port.in.wishlist.AddWishlistItemUseCase;
import app.giftify.member.application.port.in.wishlist.RemoveWishlistItemUseCase;
import app.giftify.member.application.port.out.wishlist.WishlistItemRepositoryPort;
import app.giftify.member.core.domain.exception.wishlist.InvalidWishlistItemStatusException;
import app.giftify.member.core.domain.exception.wishlist.WishlistNotFoundException;
import app.giftify.member.core.domain.wishlist.ItemStatus;
import app.giftify.member.core.domain.wishlist.WishlistItem;
import app.giftify.shared.domain.event.EventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WishlistItemServiceTest {

    @Mock
    private WishlistItemRepositoryPort wishlistItemRepositoryPort;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private WishlistItemService wishlistItemService;

    @Test
    @DisplayName("위시리스트 아이템을 추가한다")
    void addWishlistItem() {
        // given
        String authSub = "auth0|123";
        Long productId = 1L;
        AddWishlistItemUseCase.WishlistItemAddCommand command =
                new AddWishlistItemUseCase.WishlistItemAddCommand(authSub, productId, ItemStatus.ACTIVE);

        given(wishlistItemRepositoryPort.save(any(WishlistItem.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        WishlistItem result = wishlistItemService.addWishlistItem(command);

        // then
        assertThat(result.getAuthSub()).isEqualTo(authSub);
        assertThat(result.getProductId()).isEqualTo(productId);
        verify(wishlistItemRepositoryPort).save(any(WishlistItem.class));
    }

    @Test
    @DisplayName("ACTIVE가 아닌 상태로 아이템을 추가하려 하면 예외가 발생한다")
    void addWishlistItemFailStatus() {
        // given
        AddWishlistItemUseCase.WishlistItemAddCommand command =
                new AddWishlistItemUseCase.WishlistItemAddCommand("auth0|123", 1L, ItemStatus.DRAFT);

        // when & then
        assertThatThrownBy(() -> wishlistItemService.addWishlistItem(command))
                .isInstanceOf(InvalidWishlistItemStatusException.class);
    }

    @Test
    @DisplayName("위시리스트 아이템을 제거한다")
    void removeWishlistItem() {
        // given
        String authSub = "auth0|123";
        Long productId = 1L;
        RemoveWishlistItemUseCase.WishlistItemRemoveCommand command =
                new RemoveWishlistItemUseCase.WishlistItemRemoveCommand(authSub, productId);

        WishlistItem wishlistItem = WishlistItem.builder().authSub(authSub).productId(productId).build();
        given(wishlistItemRepositoryPort.findByAuthSubAndProductId(authSub, productId)).willReturn(Optional.of(wishlistItem));

        // when
        wishlistItemService.removeWishlistItem(command);

        // then
        verify(wishlistItemRepositoryPort).delete(wishlistItem);
    }

    @Test
    @DisplayName("존재하지 않는 아이템을 제거하려 하면 예외가 발생한다")
    void removeWishlistItemFail() {
        // given
        String authSub = "auth0|123";
        Long productId = 1L;
        RemoveWishlistItemUseCase.WishlistItemRemoveCommand command =
                new RemoveWishlistItemUseCase.WishlistItemRemoveCommand(authSub, productId);

        given(wishlistItemRepositoryPort.findByAuthSubAndProductId(authSub, productId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> wishlistItemService.removeWishlistItem(command))
                .isInstanceOf(WishlistNotFoundException.class);
    }

    @Test
    @DisplayName("위시리스트 아이템 목록을 조회한다")
    void getWishlistItems() {
        // given
        String authSub = "auth0|123";
        List<WishlistItem> items = List.of(
                WishlistItem.builder().authSub(authSub).productId(1L).build(),
                WishlistItem.builder().authSub(authSub).productId(2L).build()
        );
        given(wishlistItemRepositoryPort.findByAuthSub(authSub)).willReturn(items);

        // when
        List<WishlistItem> result = wishlistItemService.getWishlistItems(authSub);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(WishlistItem::getProductId).containsExactly(1L, 2L);
    }
}
