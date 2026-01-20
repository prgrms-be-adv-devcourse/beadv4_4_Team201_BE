package app.giftify.member.adapter.out.jpa.adapter.wishlist;

import app.giftify.member.adapter.out.jpa.entity.wishlist.WishlistItemJpaEntity;
import app.giftify.member.adapter.out.jpa.mapper.wishlist.WishlistItemMapper;
import app.giftify.member.adapter.out.jpa.respository.wishlist.WishlistItemJpaRepository;
import app.giftify.member.application.port.out.wishlist.WishlistItemRepositoryPort;
import app.giftify.member.core.domain.wishlist.WishlistItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WishlistItemAdapter implements WishlistItemRepositoryPort {
    private final WishlistItemJpaRepository wishlistItemRepository;

    @Override
    public Optional<WishlistItem> findByWishlistIdAndProductId(Long wishlistId, Long productId) {
        return wishlistItemRepository.findByWishlistIdAndProductId(wishlistId, productId)
                .map(WishlistItemMapper::toDomain);
    }

    @Override
    public Optional<WishlistItem> findById(Long id) {
        return wishlistItemRepository.findById(id)
                .map(WishlistItemMapper::toDomain);
    }

    @Override
    public List<WishlistItem> findByWishlistId(Long wishlistId) {
        return wishlistItemRepository.findByWishlistId(wishlistId)
                .stream()
                .map(WishlistItemMapper::toDomain)
                .toList();
    }

    @Override
    public WishlistItem save(WishlistItem wishlistItem) {
        WishlistItemJpaEntity entity = WishlistItemMapper.toEntity(wishlistItem);
        WishlistItemJpaEntity savedEntity = wishlistItemRepository.save(entity);
        return WishlistItemMapper.toDomain(savedEntity);
    }

    @Override
    public void deleteByWishlistIdAndProductId(Long wishlistId, Long productId) {
        wishlistItemRepository.deleteByWishlistIdAndProductId(wishlistId, productId);
    }

    @Override
    public void delete(WishlistItem wishlistItem) {
        wishlistItemRepository.delete(WishlistItemMapper.toEntity(wishlistItem));
    }

    @Override
    public Long count() {
        return wishlistItemRepository.count();
    }
}
