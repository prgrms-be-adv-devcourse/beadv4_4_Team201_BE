package app.giftify.orderDemo.domain;

import app.giftify.shared.domain.vo.Money;

import java.util.List;

public record CancelTargetItems(List<OrderItem> items) {

    public Money calculateCancelAmount() {
        return items.stream()
                .map(OrderItem::getAmount)
                .reduce(Money.zero(), Money::plus);
    }

    public void cancel() {
        items.forEach(OrderItem::cancel);
    }
}
