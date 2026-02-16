package app.giftify.settlement.domain.model;

import app.giftify.settlement.domain.errorCode.SettlementErrorCode;
import app.giftify.settlement.domain.support.MoneyConverter;
import app.giftify.shared.api.exception.DomainException;
import app.giftify.shared.domain.vo.Money;
import jakarta.persistence.Convert;

public record SettlementAmountSummary(
        @Convert(converter = MoneyConverter.class)
        Money salesAmount,
        @Convert(converter = MoneyConverter.class)
        Money platformFee,
        @Convert(converter = MoneyConverter.class)
        Money pgFee,
        @Convert(converter = MoneyConverter.class)
        Money settlementAmount
) {
    // 내부 검증 로직 추가 (불변식 확인)
    public SettlementAmountSummary {
        Money calculated = salesAmount.minus(platformFee).minus(pgFee);
        if (!calculated.equals(settlementAmount)) {
            throw new DomainException(SettlementErrorCode.INVALID_SETTLEMENT_CORE);
        }
    }
}
