package domain.funding;

import domain.product.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "FUNDING_WISHLIST_ITEM")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FundingWishlistItem extends BaseEntity {

    public enum WishListItemStatus {
        PENDING,                // 위시리스트에 담긴 상태
        IN_PROGRESS,            // 펀딩 진행 중
        REQUESTED_CONFIRM,      // 수령자 확정 대기 중
        COMPLETED               // 수령자 확정 완료
    }

    @Column(nullable = false)
    private Long wishlistId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WishListItemStatus status;


    public FundingWishlistItem(Long wishlistId, Product product, WishListItemStatus status) {
        this.wishlistId = wishlistId;
        this.product = product;
        this.status = status;
    }

}
