package app.giftify.wishlist.application.service;

import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.wishlist.application.port.in.GetWishlistUseCase;
import app.giftify.wishlist.application.port.in.UpdateWishlistSettingsUseCase;
import app.giftify.wishlist.application.port.out.WishlistRepositoryPort;
import app.giftify.wishlist.core.domain.Wishlist;
import app.giftify.wishlist.core.domain.exception.WishlistNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishlistService implements GetWishlistUseCase, UpdateWishlistSettingsUseCase {

    private final WishlistRepositoryPort wishlistRepositoryPort;
    private final EventPublisher eventPublisher;

    @Override
    public Optional<Wishlist> getWishlistByAuthSub(String authSub) {
        return wishlistRepositoryPort.findByAuthSub(authSub);
    }

    @Override
    @Transactional
    public Wishlist updateSettings(UpdateSettingsCommand command) {
        Wishlist wishlist = wishlistRepositoryPort.findByAuthSub(command.authSub())
                .orElseThrow(() -> new WishlistNotFoundException(command.authSub()));

        wishlist.changeVisibility(command.visibility());

        Wishlist updatedWishlist = wishlistRepositoryPort.save(wishlist);

        // 위시리스트 상태 변경 이벤트 발행

        return updatedWishlist;
    }
}
