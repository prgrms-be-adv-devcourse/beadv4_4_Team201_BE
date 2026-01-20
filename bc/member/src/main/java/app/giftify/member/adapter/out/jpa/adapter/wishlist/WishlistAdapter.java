package app.giftify.member.adapter.out.jpa.adapter.wishlist;

import app.giftify.member.adapter.out.jpa.entity.wishlist.WishlistJpaEntity;
import app.giftify.member.adapter.out.jpa.mapper.wishlist.WishlistMapper;
import app.giftify.member.adapter.out.jpa.respository.wishlist.WishlistJpaRepository;
import app.giftify.member.application.port.out.wishlist.WishlistRepositoryPort;
import app.giftify.member.core.domain.wishlist.Wishlist;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WishlistAdapter implements WishlistRepositoryPort {

    private final WishlistJpaRepository wishlistRepository;

    @Override
    public Optional<Wishlist> findByMemberId(Long memberId) {
        return wishlistRepository.findById(memberId)
                .map(WishlistMapper::toDomain);
    }

    @Override
    public Optional<Wishlist> findByAuthSub(String authSub) {
        return wishlistRepository.findByAuthSub(authSub)
                .map(WishlistMapper::toDomain);
    }

    @Override
    public Wishlist save(Wishlist wishlist) {
        WishlistJpaEntity entity = WishlistMapper.toEntity(wishlist);
        WishlistJpaEntity savedEntity = wishlistRepository.save(entity);
        return WishlistMapper.toDomain(savedEntity);
    }
}
