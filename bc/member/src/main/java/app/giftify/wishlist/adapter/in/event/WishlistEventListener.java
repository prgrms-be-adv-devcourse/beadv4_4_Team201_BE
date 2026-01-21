package app.giftify.wishlist.adapter.in.event;

import app.giftify.shared.domain.event.product.ProductSaleDisabledEvent;
import app.giftify.shared.domain.event.product.ProductSaleEnabledEvent;
import app.giftify.shared.domain.event.product.ProductSnapshotCreationRequestedEvent;
import app.giftify.shared.domain.event.product.ProductSnapshotUpdatedEvent;
import app.giftify.wishlist.application.port.out.WishlistProductReplicaPort;
import app.giftify.wishlist.core.domain.replica.WishlistProductReplica;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WishlistEventListener {

    private final WishlistProductReplicaPort wishlistProductReplicaPort;

    // 상품의 상태가 [판매 가능]으로 변경되었을 때
    @EventListener
    public void handleProductSaleEnabled(ProductSaleEnabledEvent event) {
        log.info("[Wishlist] 상품의 상태가 [판매 가능]으로 변경되었습니다 | productId: {}", event.getProductId());
        // 위시리스트ID로 레플리카 조회
        wishlistProductReplicaPort.findByProductId(event.getProductId())
                .ifPresentOrElse(
                        // 존재하면 상품 상태 true로 변경
                        replica -> {
                            replica.enableSale();
                            wishlistProductReplicaPort.upsert(replica);
                        },
                        // 존재하지 않으면 상품 조회 API 요청 후 값 받아와 레플리카 새롭게 생성
                        () -> {
                            WishlistProductReplica newReplica = WishlistProductReplica.builder()
                                    .productId(event.getProductId())
                                    .wishlistAllowed(true)
                                    .build();
                            wishlistProductReplicaPort.upsert(newReplica);
                        }
                );
    }

    // 상품의 상태가 [판매 불가능]으로 변경되었을 때
    @EventListener
    public void handleProductSaleDisabled(ProductSaleDisabledEvent event) {
        log.info("[Wishlist] 상품의 상태가 [판매 불가능]으로 변경되었습니다 | productId: {}", event.getProductId());
        wishlistProductReplicaPort.findByProductId(event.getProductId())
                .ifPresentOrElse(
                        // 존재하면 상품 상태 false로 변경
                        replica -> {
                            replica.disableSale();
                            wishlistProductReplicaPort.upsert(replica);
                        },
                        // 존재하지 않으면 상품 조회 API 요청 후 값 받아와 레플리카 새롭게 생성
                        () -> {
                            WishlistProductReplica newReplica = WishlistProductReplica.builder()
                                    .productId(event.getProductId())
                                    .wishlistAllowed(false)
                                    .build();
                            wishlistProductReplicaPort.upsert(newReplica);
                        }
                );
    }

    // 상품이 등록되었을 때
    @EventListener
    public void handleProductSnapshotCreationRequested(ProductSnapshotCreationRequestedEvent event) {
        log.info("[Wishlist] 상품이 등록되었습니다 | productId: {}", event.getId());
        wishlistProductReplicaPort.findByProductId(event.getId())
                .ifPresentOrElse(
                        // 존재하면 값만 업데이트
                        replica -> {
                            replica.updateInfo(
                                    event.getName(),
                                    event.getPrice(),
                                    event.getSellerNickName()
                            );
                            wishlistProductReplicaPort.upsert(replica);
                        },
                        // 존재하지 않으면 새로운 레플리카 생성
                        () -> {
                            WishlistProductReplica newReplica = WishlistProductReplica.builder()
                                    .productId(event.getId())
                                    .name(event.getName())
                                    .price(event.getPrice())
                                    .sellerNickName(event.getSellerNickName())
                                    .wishlistAllowed(false)
                                    .build();
                            wishlistProductReplicaPort.upsert(newReplica);
                        }
                );
    }

    // 상품 정보가 변경되었을 때
    @EventListener
    public void handleProductSnapshotUpdated(ProductSnapshotUpdatedEvent event) {
        log.info("[Wishlist] Product snapshot updated event received for productId: {}", event.getId());
        wishlistProductReplicaPort.findByProductId(event.getId())
                .ifPresentOrElse(
                        replica -> {
                            replica.updateInfo(event.getName(), event.getPrice(), event.getSellerNickName());
                            wishlistProductReplicaPort.upsert(replica);
                        },
                        () -> {
                            WishlistProductReplica newReplica = WishlistProductReplica.builder()
                                    .productId(event.getId())
                                    .name(event.getName())
                                    .price(event.getPrice())
                                    .sellerNickName(event.getSellerNickName()) // TODO: 이벤트 형식에 맞게 수정
                                    .wishlistAllowed(false)
                                    .build();
                            wishlistProductReplicaPort.upsert(newReplica);
                        }
                );
    }

}
