package app.giftify.settlement.domain.model;

import app.giftify.settlement.domain.errorCode.SettlementErrorCode;
import app.giftify.settlement.domain.support.MoneyConverter;
import app.giftify.support.common.api.exception.DomainException;
import app.giftify.support.common.money.Money;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;

import java.util.List;
import java.util.Objects;

@Embeddable
public class SettlementAmountSummary {

    @Convert(converter = MoneyConverter.class)
    private Money salesAmount;
    @Convert(converter = MoneyConverter.class)
    private Money platformFee;
    @Convert(converter = MoneyConverter.class)
    private Money pgFee;
    @Convert(converter = MoneyConverter.class)
    private Money settlementAmount;

    protected SettlementAmountSummary() {}

    public SettlementAmountSummary(Money salesAmount, Money platformFee, Money pgFee, Money settlementAmount) {
        Money calculated = salesAmount.minus(platformFee).minus(pgFee);
        if (!calculated.equals(settlementAmount)) {
            throw new DomainException(SettlementErrorCode.INVALID_SETTLEMENT_CORE);
        }
        this.salesAmount = salesAmount;
        this.platformFee = platformFee;
        this.pgFee = pgFee;
        this.settlementAmount = settlementAmount;
    }

    public static SettlementAmountSummary of(List<SettlementQueue> queues) {
        Money totalSales = Money.zero();
        Money totalPlatformFee = Money.zero();
        Money totalPgFee = Money.zero();
        Money totalSettlement = Money.zero();

        for (SettlementQueue queue : queues) {
            SettlementItem item = queue.getItem();

            totalSales = totalSales.plus(item.getCore().paidAmount());
            totalPlatformFee = totalPlatformFee.plus(item.getCore().platformFee());
            totalPgFee = totalPgFee.plus(item.getCore().pgFee());
            totalSettlement = totalSettlement.plus(item.getCore().settlementAmount());
        }

        return new SettlementAmountSummary(
                totalSales,
                totalPlatformFee,
                totalPgFee,
                totalSettlement
        );
    }

    public Money salesAmount() { return salesAmount; }
    public Money platformFee() { return platformFee; }
    public Money pgFee() { return pgFee; }
    public Money settlementAmount() { return settlementAmount; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SettlementAmountSummary that)) return false;
        return Objects.equals(salesAmount, that.salesAmount)
                && Objects.equals(platformFee, that.platformFee)
                && Objects.equals(pgFee, that.pgFee)
                && Objects.equals(settlementAmount, that.settlementAmount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(salesAmount, platformFee, pgFee, settlementAmount);
    }

    @Override
    public String toString() {
        return "SettlementAmountSummary{salesAmount=" + salesAmount
                + ", platformFee=" + platformFee
                + ", pgFee=" + pgFee
                + ", settlementAmount=" + settlementAmount + "}";
    }
}
