package app.giftify.wishlist.adapter.out.jpa.adapter;

import app.giftify.support.common.api.paging.Page;
import app.giftify.support.common.api.paging.PageRequest;
import app.giftify.wishlist.adapter.out.jpa.entity.WishlistItemJpaEntity;
import app.giftify.wishlist.adapter.out.jpa.mapper.WishlistItemMapper;
import app.giftify.wishlist.adapter.out.jpa.repository.WishlistItemJpaRepository;
import app.giftify.wishlist.application.port.out.WishlistItemRepositoryPort;
import app.giftify.wishlist.core.domain.WishlistItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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
    public Page<WishlistItem> findByWishlistId(Long wishlistId, PageRequest pageRequest) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.page(), pageRequest.size());
        org.springframework.data.domain.Page<WishlistItemJpaEntity> jpaPage =
                wishlistItemRepository.findByWishlistId(wishlistId, pageable);
        List<WishlistItem> items = jpaPage.getContent().stream()
                .map(WishlistItemMapper::toDomain)
                .toList();
        return Page.of(items, jpaPage.getTotalElements());
    }

    @Override
    public List<WishlistItem> findAllById(List<Long> wishlistItemIds) {
        return wishlistItemRepository.findAllById(wishlistItemIds)
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
        wishlistItemRepository.deleteById(wishlistItem.getId());
    }

    @Override
    public int deleteCompletedItemsUpdatedBefore(LocalDateTime cutoff) {
        return wishlistItemRepository.deleteCompletedItemsUpdatedBefore(cutoff);
    }

    @Override
    public Long count() {
        return wishlistItemRepository.count();
    }
}
