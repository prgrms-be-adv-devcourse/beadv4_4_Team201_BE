package app.giftify.domain.funding;


import app.giftify.support.jpa.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "FUNDING_WISHLIST_ITEM")
@Getter
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
    private Long fundingReceiverId;  // 위시리스트 소유자(펀딩 수령자) ID

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


    public FundingWishlistItem(Long wishlistId, Long fundingReceiverId, Long productId, String productName, int productPrice, WishListItemStatus status) {
        this.wishlistId = wishlistId;
        this.fundingReceiverId = fundingReceiverId;
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.status = status;
    }

}
