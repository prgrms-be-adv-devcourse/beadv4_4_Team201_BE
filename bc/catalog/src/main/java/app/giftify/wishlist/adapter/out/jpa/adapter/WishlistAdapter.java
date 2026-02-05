package app.giftify.wishlist.adapter.out.jpa.adapter;

import app.giftify.wishlist.adapter.out.jpa.entity.WishlistJpaEntity;
import app.giftify.wishlist.adapter.out.jpa.mapper.WishlistMapper;
import app.giftify.wishlist.adapter.out.jpa.repository.WishlistJpaRepository;
import app.giftify.wishlist.application.port.out.WishlistRepositoryPort;
import app.giftify.wishlist.core.domain.Wishlist;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WishlistAdapter implements WishlistRepositoryPort {

    private final WishlistJpaRepository wishlistRepository;

    @Override
    public Optional<Wishlist> findById(Long id) {
        return wishlistRepository.findById(id)
                .map(WishlistMapper::toDomain);
    }

    @Override
    public Optional<Wishlist> findByMemberId(Long memberId) {
        return wishlistRepository.findById(memberId)
                .map(WishlistMapper::toDomain);
    }

    @Override
    public List<Wishlist> findAllById(List<Long> wishlistIds) {
        return wishlistRepository.findAllById(wishlistIds)
                .stream().map(WishlistMapper::toDomain).toList();
    }

    @Override
    public Wishlist save(Wishlist wishlist) {
        WishlistJpaEntity entity = WishlistMapper.toEntity(wishlist);
        WishlistJpaEntity savedEntity = wishlistRepository.save(entity);
        return WishlistMapper.toDomain(savedEntity);
    }
}
