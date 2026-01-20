package app.giftify.member.application.service.wishlist;

import app.giftify.member.application.port.in.wishlist.AddWishlistItemUseCase;
import app.giftify.member.application.port.in.wishlist.GetWishlistItemUseCase;
import app.giftify.member.application.port.in.wishlist.RemoveWishlistItemUseCase;
import app.giftify.member.application.port.out.wishlist.WishlistItemRepositoryPort;
import app.giftify.member.core.domain.exception.wishlist.InvalidWishlistItemStatusException;
import app.giftify.member.core.domain.exception.wishlist.WishlistNotFoundException;
import app.giftify.member.core.domain.wishlist.ItemStatus;
import app.giftify.member.core.domain.wishlist.WishlistItem;
import app.giftify.shared.domain.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishlistItemService implements AddWishlistItemUseCase, GetWishlistItemUseCase, RemoveWishlistItemUseCase {

    private final WishlistItemRepositoryPort wishlistItemRepositoryPort;
    private final EventPublisher eventPublisher;

    @Override
    public Long getWishlistItemCount(Long wishlistId) {
        return wishlistItemRepositoryPort.count();
    }

    @Override
    public boolean isItemExists(String authSub, Long productId) {
        Optional<WishlistItem> wishlistItem = wishlistItemRepositoryPort.findByAuthSubAndProductId(authSub, productId);
        return wishlistItem.isPresent();
    }

    @Override
    public List<WishlistItem> getWishlistItems(String authSub) {
        return wishlistItemRepositoryPort.findByAuthSub(authSub);
    }

    @Override
    public WishlistItem addWishlistItem(WishlistItemAddCommand command) {
        if (command.itemStatus() != ItemStatus.ACTIVE) {
            throw new InvalidWishlistItemStatusException(command.itemStatus().toString());
        }

        WishlistItem wishlistItem = WishlistItem.builder()
                .authSub(command.authSub())
                .productId(command.productId())
                .itemStatus(command.itemStatus())
                .build();

        WishlistItem addedwishlistItem = wishlistItemRepositoryPort.save(wishlistItem);

        // 위시리스트 아이템 추가 이벤트 발행

        return addedwishlistItem;
    }

    @Override
    public void removeWishlistItem(WishlistItemRemoveCommand command) {
        WishlistItem wishlistItem = wishlistItemRepositoryPort
                .findByAuthSubAndProductId(command.authSub(), command.productId())
                .orElseThrow(() -> new WishlistNotFoundException(command.authSub()));

        wishlistItemRepositoryPort.delete(wishlistItem);

        // 위시리스트 아이템 삭제 이벤트 발행
    }
}
