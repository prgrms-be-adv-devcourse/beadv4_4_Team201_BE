package app.giftify.shared.domain.vo;

import java.util.Objects;

public record FeeInfo(
        Money platformFee,
        Money pgFee
) {
    public FeeInfo {
        Objects.requireNonNull(platformFee, "platformFee는 필수입니다.");
        Objects.requireNonNull(pgFee, "pgFee는 필수입니다.");
    }

    public Money getSettlementAmount(Money totalAmount) {
        return totalAmount.minus(platformFee).minus(pgFee);
    }
}