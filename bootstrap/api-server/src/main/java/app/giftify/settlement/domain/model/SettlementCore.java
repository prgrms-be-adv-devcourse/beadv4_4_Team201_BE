package app.giftify.settlement.domain.model;

import app.giftify.settlement.domain.errorCode.SettlementErrorCode;
import app.giftify.settlement.domain.support.MoneyConverter;
import app.giftify.support.common.api.exception.DomainException;
import app.giftify.support.common.money.Money;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class SettlementCore {

        @Convert(converter = MoneyConverter.class)
        private Money paidAmount;

        @Convert(converter = MoneyConverter.class)
        private Money platformFee;

        @Convert(converter = MoneyConverter.class)
        private Money pgFee;

        @Convert(converter = MoneyConverter.class)
        private Money settlementAmount;

        protected SettlementCore() {}

        public SettlementCore(Money paidAmount, Money platformFee, Money pgFee, Money settlementAmount) {
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

                this.paidAmount = paidAmount;
                this.platformFee = platformFee;
                this.pgFee = pgFee;
                this.settlementAmount = settlementAmount;
        }

        public Money paidAmount() { return paidAmount; }
        public Money platformFee() { return platformFee; }
        public Money pgFee() { return pgFee; }
        public Money settlementAmount() { return settlementAmount; }

        @Override
        public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof SettlementCore that)) return false;
                return Objects.equals(paidAmount, that.paidAmount)
                        && Objects.equals(platformFee, that.platformFee)
                        && Objects.equals(pgFee, that.pgFee)
                        && Objects.equals(settlementAmount, that.settlementAmount);
        }

        @Override
        public int hashCode() {
                return Objects.hash(paidAmount, platformFee, pgFee, settlementAmount);
        }

        @Override
        public String toString() {
                return "SettlementCore{paidAmount=" + paidAmount
                        + ", platformFee=" + platformFee
                        + ", pgFee=" + pgFee
                        + ", settlementAmount=" + settlementAmount + "}";
        }
}
