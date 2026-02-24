package app.giftify.settlement.domain.model;

import app.giftify.settlement.domain.errorCode.SettlementErrorCode;
import app.giftify.settlement.domain.support.MoneyConverter;
import app.giftify.shared.api.exception.DomainException;
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
                if (paidAmount == null) throw new DomainException(SettlementErrorCode.MISSING_FIELD, "PaidAmount");
                if (platformFee == null) throw new DomainException(SettlementErrorCode.MISSING_FIELD, "PlatformFee");
                if (pgFee == null) throw new DomainException(SettlementErrorCode.MISSING_FIELD, "PgFee");

                Money expected = paidAmount.minus(platformFee).minus(pgFee);

                if (!expected.equals(settlementAmount)) throw new DomainException(
                        SettlementErrorCode.SETTLEMENT_AMOUNT_MISMATCH,
                        String.format("정산 금액과 계산된 금액이 일치하지 않습니다. (정산 금액: %s, 계산된 금액: %s)",
                                settlementAmount.amount().toPlainString(), expected.amount().toPlainString()
                        )
                );
        }
}
