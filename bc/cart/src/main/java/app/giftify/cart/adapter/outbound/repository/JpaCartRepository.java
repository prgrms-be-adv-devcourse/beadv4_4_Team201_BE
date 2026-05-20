package app.giftify.cart.adapter.outbound.repository;

import app.giftify.cart.adapter.outbound.jpa.JpaCart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaCartRepository extends JpaRepository<JpaCart, Long> {
    Optional<JpaCart> findByMemberId(Long memberId);
}
