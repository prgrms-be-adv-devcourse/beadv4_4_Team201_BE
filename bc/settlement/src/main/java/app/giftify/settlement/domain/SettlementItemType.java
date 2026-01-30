package app.giftify.settlement.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SettlementItemType {
    ITEM_PAYMENT(false),
    DEDUCTION_REFUND(true);

    private final boolean requiresOriginId;

    public boolean requiresOriginId() {
        return requiresOriginId;
    }
}