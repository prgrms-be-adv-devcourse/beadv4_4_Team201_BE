package app.giftify.domain.funding;

import app.giftify.shared.api.exception.BusinessException;
import app.giftify.support.jpa.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "FUNDING")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Funding extends BaseJpaEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funding_wishlist_item_id", nullable = false)
    private FundingWishlistItem fundingWishlistItem;

    @Column(nullable = false)
    private Integer targetAmount;

    @Column(nullable = false)
    private Integer currentAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FundingStatus status;

    @Column(nullable = false)
    private LocalDateTime endAt;


    private Funding(FundingWishlistItem item, Integer currentAmount) {
        this.fundingWishlistItem = item;
        this.targetAmount = item.getProductPrice();
        this.currentAmount = currentAmount;
        this.status = FundingStatus.IN_PROGRESS;
        this.endAt = LocalDateTime.now().plusDays(15);
    }

    public static Funding startFunding(FundingWishlistItem item, Integer amount) {
        validateAmount(amount);
        
        Funding funding = new Funding(item, amount);
        
        return funding;
    }

    public static void validateAmount(Integer amount) {
        if (amount == null || amount < 1000) {
            throw new BusinessException(FundingErrorCode.INVALID_AMOUNT);
        }
    }

    /**
     * 펀딩 참여
     */
    public void contribute(Integer amount) {
        if (this.status != FundingStatus.IN_PROGRESS) {
            throw new BusinessException(FundingErrorCode.NOT_IN_PROGRESS);
        }

        validateAmount(amount);

        // 잔여 금액 계산
        int remainingAmount = this.targetAmount - this.currentAmount;
        
        // 잔여 금액 초과 검증
        if (amount > remainingAmount) {
            throw new BusinessException(
                FundingErrorCode.EXCEED_REMAINING_AMOUNT,
                String.format("펀딩 잔여 금액(%d원)을 초과할 수 없습니다. 요청 금액: %d원", 
                    remainingAmount, amount)
            );
        }

        this.currentAmount += amount;

        if (this.currentAmount >= this.targetAmount) {
            this.status = FundingStatus.ACHIEVED;
        }
    }

    /**
     * 펀딩 만료 처리
     */
    public void expire() {
        if (this.status == FundingStatus.CLOSED) {
            throw new BusinessException(FundingErrorCode.ALREADY_CLOSED);
        }
        this.status = FundingStatus.EXPIRED;
    }

    /**
     * 펀딩 기간 종료 여부
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.endAt);
    }

    /**
     * 목표 금액 달성 여부
     */
    public boolean isAchieved() {
        return this.currentAmount >= this.targetAmount;
    }

}