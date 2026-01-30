package app.giftify.settlement.domain;

import app.giftify.settlement.domain.errorCode.SettlementErrorCode;
import app.giftify.settlement.domain.exception.DomainException;
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
        public SettlementCore {
                if (paidAmount == null || platformFee == null || pgFee == null || settlementAmount == null) {
                        throw new DomainException(SettlementErrorCode.INVALID_SETTLEMENT_CORE);
                }

                if (paidAmount.isNegative() || platformFee.isNegative() || pgFee.isNegative() || settlementAmount.isNegative()) {
                        throw new DomainException(SettlementErrorCode.INVALID_SETTLEMENT_CORE);
                }

                Money expected = paidAmount.minus(platformFee).minus(pgFee);

                if (!expected.equals(settlementAmount)) {
                        throw new DomainException(SettlementErrorCode.INVALID_SETTLEMENT_CORE);
                }
        }
}
