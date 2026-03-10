package app.giftify.funding.adpater.outbound.jpa;

import app.giftify.funding.domain.exception.FundingErrorCode;
import app.giftify.funding.domain.exception.FundingException;
import app.giftify.shared.domain.type.FundingStatus;
import app.giftify.support.jpa.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "FUNDINGS",
        indexes = {
        @Index(
                name = "idx_funding_wishlist_status",
                columnList = "wishlist_item_id, status"
        )
            })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Funding extends BaseJpaEntity {

    @Version
    private long version;

    @Column(nullable = false)
    private Long wishlistItemId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String productName;

    @Column
    private String imageKey;

    @Column(nullable = false)
    private Long receiverId;

    @Column(nullable = false)
    private Integer targetAmount;       // 상품 원가 (목표액)

    @Column(nullable = false)
    private Integer currentAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FundingStatus status;

    @Column(nullable = false)
    private LocalDateTime deadline;      // 펀딩 종료 예정 시점

    @Column
    private LocalDateTime closedAt;     // 펀딩이 실제 종료된 시점

    @Column
    private LocalDateTime achievedAt;   // 펀딩 달성 시각 : 달성 후 2주내 미수락 시 종료되어야 하니까


    private Funding(Long wishlistItemId, Long productId, String productName, String imageKey, Long receiverId, Integer productPrice) {
        this.wishlistItemId = wishlistItemId;
        this.productId = productId;
        this.productName = productName;
        this.imageKey = imageKey;
        this.receiverId = receiverId;
        this.targetAmount = productPrice;
        this.currentAmount = 0;
        this.status = FundingStatus.IN_PROGRESS;
        this.deadline = LocalDateTime.now().plusDays(15);
    }

    public static Funding startFunding(Long wishlistItemId, Long productId, String productName, String imageKey, Long receiverId, Integer targetAmount) {
        return new Funding(wishlistItemId, productId, productName, imageKey, receiverId, targetAmount);
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

        Integer remainingAmount = this.targetAmount - this.currentAmount;

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
            this.achievedAt = LocalDateTime.now();
        }
    }

    public boolean expire(LocalDateTime now) {
        if (this.status != FundingStatus.IN_PROGRESS) {
            return false; // 이미 종료됨 → 멱등
        }

        if (!isExpired(now)) {
            throw new FundingException(FundingErrorCode.IS_NOT_EXPIRED);
        }

        this.status = FundingStatus.EXPIRED;
        this.closedAt = now;
        return true;
    }

    public void close() {
        if (this.status != FundingStatus.IN_PROGRESS && this.status != FundingStatus.ACHIEVED) {
            return;
        }

        this.status = FundingStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
    }

    public boolean isExpired(LocalDateTime now) { return now.isAfter(this.deadline); }

    public boolean isExpired() { return LocalDateTime.now().isAfter(this.deadline); }

    public boolean isAchieved() { return this.currentAmount.equals(this.targetAmount); }

    public void refuse() {
        if (this.getStatus() == FundingStatus.ACCEPTED || this.getStatus() == FundingStatus.REFUSED) {
            throw new FundingException(FundingErrorCode.ALREADY_DECIDED, this.getId());
        }

        if (this.getStatus() != FundingStatus.ACHIEVED) {
            throw new FundingException(FundingErrorCode.NOT_ACHIEVED);
        }
        this.status = FundingStatus.REFUSED;
        this.closedAt = LocalDateTime.now();
    }

    public void accept() {
        if (this.getStatus() == FundingStatus.ACCEPTED || this.getStatus() == FundingStatus.REFUSED) {
            throw new FundingException(FundingErrorCode.ALREADY_DECIDED, this.getId());
        }

        if (this.getStatus() != FundingStatus.ACHIEVED) {
            throw new FundingException(FundingErrorCode.NOT_ACHIEVED);
        }

        this.status = FundingStatus.ACCEPTED;
        this.closedAt = LocalDateTime.now();
    }

    public void validateReceiver(Long memberId) {
        if (!memberId.equals(this.getReceiverId())) { throw new FundingException(FundingErrorCode.FORBIDDEN); }
    }

    /**
     * 상품 정보 수정
     */
    public void updateProductInfo(Integer productPrice, String productName, String imageKey) {
        if (this.status != FundingStatus.IN_PROGRESS) throw new FundingException(FundingErrorCode.NOT_IN_PROGRESS);
        this.targetAmount = productPrice;
        this.productName = productName;
        this.imageKey = imageKey;
    }

    /**
     * 펀딩 기여 취소
     */
    public void withdraw(Integer amount) {
        if (this.status != FundingStatus.IN_PROGRESS && this.status != FundingStatus.ACHIEVED) {
            throw new FundingException(FundingErrorCode.INVALID_STATUS_FOR_WITHDRAWAL);
        }
        this.currentAmount -= amount;
    }
}
