package app.giftify.wishlist.adapter.out.jpa.adapter;

import app.giftify.wishlist.adapter.out.jpa.mapper.WishlistProductReplicaMapper;
import app.giftify.wishlist.adapter.out.jpa.repository.WishlistProductReplicaJpaRepository;
import app.giftify.wishlist.application.port.out.WishlistProductReplicaPort;
import app.giftify.wishlist.core.domain.replica.WishlistProductReplica;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WishlistProductReplicaAdapter implements WishlistProductReplicaPort {

    private final WishlistProductReplicaJpaRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Optional<WishlistProductReplica> findByProductId(Long productId) {
        return repository.findByProductId(productId)
                .map(WishlistProductReplicaMapper::toDomain);
    }

    @Override
    @Transactional
    public void upsert(WishlistProductReplica domain) {
        repository.findByProductId(domain.getProductId())
                .ifPresentOrElse(
                        entity -> entity.update(
                                domain.isWishlistAllowed(),
                                domain.getUpdatedAt(),
                                domain.getName(),
                                domain.getPrice(),
                                domain.getSellerNickName()
                        ),
                        () -> repository.save(WishlistProductReplicaMapper.toEntity(domain))
                );
    }
}
