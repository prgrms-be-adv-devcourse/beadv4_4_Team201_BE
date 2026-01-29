package app.giftify.settlement.domain;

import app.giftify.shared.domain.vo.Money;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SettlementCore(
        Money totalAmount,
        Money platformFee,
        Money pgFee,
        Money settlementAmount,
        LocalDate expectedDate
) {
}
