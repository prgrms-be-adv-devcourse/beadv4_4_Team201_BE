package app.giftify.wishlist.application.service;

import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.wishlist.application.port.in.AddWishlistItemUseCase;
import app.giftify.wishlist.application.port.in.RemoveWishlistItemUseCase;
import app.giftify.wishlist.application.port.out.WishlistItemRepositoryPort;
import app.giftify.wishlist.application.port.out.WishlistProductQueryPort;
import app.giftify.wishlist.application.port.out.WishlistProductReplicaPort;
import app.giftify.wishlist.core.domain.ItemStatus;
import app.giftify.wishlist.core.domain.WishlistItem;
import app.giftify.wishlist.core.domain.exception.WishlistNotFoundException;
import app.giftify.wishlist.core.domain.replica.WishlistProductReplica;
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
    private WishlistProductReplicaPort wishlistProductReplicaPort;

    @Mock
    private WishlistProductQueryPort wishlistProductQueryPort;

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

        // Replica가 존재하고 판매 가능함을 가정
        WishlistProductReplica replica = WishlistProductReplica.builder()
                .productId(productId)
                .wishlistAllowed(true)
                .updatedAt(java.time.LocalDateTime.now())
                .build();
        given(wishlistProductReplicaPort.findByProductId(productId)).willReturn(Optional.of(replica));

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
        Long productId = 1L;
        AddWishlistItemUseCase.WishlistItemAddCommand command =
                new AddWishlistItemUseCase.WishlistItemAddCommand("auth0|123", productId, ItemStatus.ACTIVE);

        // Replica가 없어서 Fallback 발생 상황 가정
        given(wishlistProductReplicaPort.findByProductId(productId)).willReturn(Optional.empty());

        // QueryPort에서 판매 중이지 않은 상태 반환
        given(wishlistProductQueryPort.getProductStatus(productId))
                .willReturn(new WishlistProductQueryPort.ProductStatus(productId, false, "Product", 1000, "Seller"));

        // when & then
        assertThatThrownBy(() -> wishlistItemService.addWishlistItem(command))
                .isInstanceOf(app.giftify.wishlist.core.domain.exception.ProductNotOnSaleException.class);
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
