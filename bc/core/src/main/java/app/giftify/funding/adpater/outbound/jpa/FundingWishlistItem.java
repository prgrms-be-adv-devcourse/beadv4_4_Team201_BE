package app.giftify.funding.adpater.outbound.jpa;

import org.hibernate.annotations.BatchSize;

import app.giftify.support.jpa.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "FUNDING_WISHLIST_ITEM")
@Getter
@BatchSize(size = 100)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FundingWishlistItem extends BaseJpaEntity {

    public enum WishListItemStatus {
        PENDING,                // 위시리스트에 담긴 상태
        IN_PROGRESS,            // 펀딩 진행 중
        REQUESTED_CONFIRM,      // 수령자 확정 대기 중
        COMPLETED               // 수령자 확정 완료
    }

    @Column(nullable = false)
    private Long wishlistId;

    @Column(nullable = false)
    private Long receiverId;

    // Product 정보를 값으로 직접 저장 (스냅샷)
    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private int productPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WishListItemStatus status;


    public FundingWishlistItem(Long wishlistId, Long receiverId, Long productId, String productName, int productPrice, WishListItemStatus status) {
        this.wishlistId = wishlistId;
        this.receiverId = receiverId;
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.status = status;
    }

}
