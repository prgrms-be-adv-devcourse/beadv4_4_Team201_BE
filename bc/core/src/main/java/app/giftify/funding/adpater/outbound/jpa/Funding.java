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
@Table(name = "FUNDING")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Funding extends BaseJpaEntity {

    @Column(nullable = false)
    private Long wishlistItemId;

    @Column(nullable = false)
    private Long productId;

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


    private Funding(Long wishlistItemId, Integer productPrice, Long productId) {
        this.wishlistItemId = wishlistItemId;
        this.targetAmount = productPrice;
        this.productId = productId;
        this.currentAmount = 0;
        this.status = FundingStatus.IN_PROGRESS;
        this.deadline = LocalDateTime.now().plusDays(15);
    }

    public static Funding startFunding(Long wishlistItemId, Integer targetAmount, Long productId) {
        return new Funding(wishlistItemId, targetAmount, productId);
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
            this.achievedAt = LocalDateTime.now();
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

    public boolean isExpired(LocalDateTime now) {return now.isAfter(this.deadline); }

    public boolean isExpired() {return LocalDateTime.now().isAfter(this.deadline); }

    public boolean isAchieved() {
        return this.currentAmount.equals(this.targetAmount);
    }

    public void refuse() {
        if (this.getStatus() != FundingStatus.ACHIEVED) {
            throw new FundingException(FundingErrorCode.NOT_ACHIEVED);
        }
        this.status = FundingStatus.REFUSED;
        this.closedAt = LocalDateTime.now();
    }

    public void accept() {
        if (this.getStatus() != FundingStatus.ACHIEVED) {
            throw new FundingException(FundingErrorCode.NOT_ACHIEVED);
        }
        this.status = FundingStatus.ACCEPTED;
        this.closedAt = LocalDateTime.now();
    }
}
