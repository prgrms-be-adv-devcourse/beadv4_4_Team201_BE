package app.giftify.shared.domain.vo;

import java.util.Objects;

public record AmountInfo(
        Money totalAmount,
        Money platformFee,
        Money pgFee,
        Money settlementAmount
) {
    public AmountInfo {
        Objects.requireNonNull(totalAmount, "totalAmount는 필수입니다.");
        Objects.requireNonNull(platformFee, "platformFee는 필수입니다.");
        Objects.requireNonNull(pgFee, "pgFee는 필수입니다.");

        Money expectedAmount = calculateSettlementAmount(totalAmount, platformFee, pgFee);
        if (!expectedAmount.equals(settlementAmount)) {
            throw new IllegalArgumentException("정산 금액 계산이 데이터와 일치하지 않습니다.");
        }
    }

    public static AmountInfo create(Money totalAmount, Money platformFee, Money pgFee) {
        return new AmountInfo(
                totalAmount,
                platformFee,
                pgFee,
                calculateSettlementAmount(totalAmount, platformFee, pgFee)
        );
    }

    private static Money calculateSettlementAmount(Money totalAmount, Money platformFee, Money pgFee) {
        return totalAmount.minus(platformFee).minus(pgFee);
    }
}