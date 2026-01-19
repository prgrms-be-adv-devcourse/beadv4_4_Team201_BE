package app.giftify.domain.funding;

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
    private LocalDateTime endAt;        // 펀딩 종료 예정 시점

    @Column
    private LocalDateTime closedAt;     // 펀딩이 실제 종료된 시점


    private Funding(FundingWishlistItem item, Integer currentAmount) {
        this.fundingWishlistItem = item;
        this.targetAmount = item.getProductPrice();
        this.currentAmount = currentAmount;
        this.status = FundingStatus.IN_PROGRESS;
        this.endAt = LocalDateTime.now().plusDays(15);
    }

    public static Funding startFunding(FundingWishlistItem item, Integer amount) {
        validateLeastAmount(amount);

        Funding funding = new Funding(item, amount);

        // 첫 결제 금액이 목표 금액과 같으면 바로 달성 상태로 변경
        if (amount.equals(funding.targetAmount)) {
            funding.status = FundingStatus.ACHIEVED;
        }

        return funding;
    }

    public static void validateLeastAmount(Integer amount) {
        if (amount == null || amount < 1000) {
            throw new FundingException(FundingErrorCode.INVALID_AMOUNT);
        }
    }

    /**
     * 펀딩 참여
     */
    public void contribute(Integer amount) {
        if (this.status != FundingStatus.IN_PROGRESS) {
            throw new FundingException(FundingErrorCode.NOT_IN_PROGRESS);
        }

        validateLeastAmount(amount);

        int remainingAmount = this.targetAmount - this.currentAmount;

        // TODO : 동시성 문제 해결 필요 (낙관적 락 등)
        // 잔여 금액 초과 검증
        if (amount > remainingAmount) {
            throw new FundingException(
                FundingErrorCode.EXCEED_REMAINING_AMOUNT,
                String.format("펀딩 잔여 금액(%d원)을 초과할 수 없습니다. 신청 금액: %d원",
                    remainingAmount, amount)
            );
        }

        this.currentAmount += amount;

        // Integer 타입은 == 비교 시 캐싱 범위(-128~127) 밖에서는 false가 될 수 있음
        if (this.currentAmount.equals(this.targetAmount)) {
            this.status = FundingStatus.ACHIEVED;
        }
    }

    public void expire() {
        if (this.status == FundingStatus.CLOSED) {
            throw new FundingException(FundingErrorCode.ALREADY_TERMINATED);
        }

        if (!isExpired()) {
            throw new FundingException(FundingErrorCode.IS_NOT_EXPIRED);
        }

        this.status = FundingStatus.EXPIRED;
        this.closedAt = LocalDateTime.now();
    }

    public void close() {
        if (this.getStatus() == FundingStatus.CLOSED || this.getStatus() == FundingStatus.EXPIRED) {
            throw new FundingException(FundingErrorCode.ALREADY_TERMINATED);
        }

        this.status = FundingStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
    }

    public boolean isExpired(LocalDateTime now) {return now.isAfter(this.endAt); }

    public boolean isExpired() {return LocalDateTime.now().isAfter(this.endAt); }

    public boolean isAchieved() {
        return this.currentAmount.equals(this.targetAmount);
    }

}