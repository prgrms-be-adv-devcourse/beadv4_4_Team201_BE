package app.giftify.settlement.domain.model;

import app.giftify.settlement.domain.errorCode.SettlementErrorCode;
import app.giftify.settlement.domain.support.MoneyConverter;
import app.giftify.shared.api.exception.DomainException;
import app.giftify.shared.domain.vo.Money;
import jakarta.persistence.Convert;

import java.util.List;

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
}
