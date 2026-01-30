package app.giftify.settlement.domain;

import app.giftify.shared.domain.vo.Money;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;

@Embeddable
public record SettlementCore(
        @Convert(converter = MoneyConverter.class)
        Money paidAmount,
        @Convert(converter = MoneyConverter.class)
        Money platformFee,
        @Convert(converter = MoneyConverter.class)
        Money pgFee,
        @Convert(converter = MoneyConverter.class)
        Money settlementAmount
) {
}
