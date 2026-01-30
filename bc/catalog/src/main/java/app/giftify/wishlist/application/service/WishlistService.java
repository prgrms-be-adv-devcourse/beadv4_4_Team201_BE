package app.giftify.wishlist.application.service;

import app.giftify.wishlist.application.port.in.GetWishlistUseCase;
import app.giftify.wishlist.application.port.in.UpdateWishlistSettingsUseCase;
import app.giftify.wishlist.application.port.out.WishlistRepositoryPort;
import app.giftify.wishlist.core.domain.Wishlist;
import app.giftify.wishlist.core.domain.exception.WishlistNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WishlistService implements GetWishlistUseCase, UpdateWishlistSettingsUseCase {

    private final WishlistRepositoryPort wishlistRepositoryPort;

    @Override
    @Transactional
    public Wishlist getOrCreateWishlistByMemberId(Long memberId) {
        return wishlistRepositoryPort.findByMemberId(memberId)
                .orElseGet(() -> {
                    Wishlist wishlist = Wishlist.builder()
                            .memberId(memberId)
                            .build();
                    return wishlistRepositoryPort.save(wishlist);
                });
    }

    @Override
    @Transactional
    public Wishlist updateSettings(UpdateSettingsCommand command) {
        Wishlist wishlist = wishlistRepositoryPort.findByMemberId(command.memberId())
                .orElseThrow(() -> new WishlistNotFoundException(command.memberId()));

        wishlist.changeVisibility(command.visibility());

        Wishlist updatedWishlist = wishlistRepositoryPort.save(wishlist);

        // 위시리스트 상태 변경 이벤트 발행 todo

        return updatedWishlist;
    }
}
