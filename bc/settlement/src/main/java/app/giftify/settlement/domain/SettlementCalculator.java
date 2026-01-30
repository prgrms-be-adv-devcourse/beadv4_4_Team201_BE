package app.giftify.settlement.domain;

import app.giftify.shared.domain.vo.Money;

import java.math.BigDecimal;

public class SettlementCalculator {

    public static SettlementCore calculate(Money paidAmount, BigDecimal platformRate, BigDecimal pgRate) {
        Money platformFee = paidAmount.multiply(platformRate);
        Money pgFee = paidAmount.multiply(pgRate);

        Money settlementAmount = paidAmount.minus(platformFee).minus(pgFee);

        return new SettlementCore(paidAmount, platformFee, pgFee, settlementAmount);
    }
}