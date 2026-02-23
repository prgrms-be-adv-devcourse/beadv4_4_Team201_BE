package app.giftify.order.domain;

import app.giftify.shared.domain.vo.CanceledItemSnapshot;
import app.giftify.shared.domain.vo.Money;

import java.util.List;

public record CancelTargetItems(List<OrderItem> items) {

    public Money calculateCancelAmount() {
        return items.stream()
                .map(OrderItem::getAmount)
                .reduce(Money.zero(), Money::plus);
    }

    public void canceled() {
        items.forEach(OrderItem::canceled);
    }

    public void canceling() {
        items.forEach(OrderItem::canceling);
    }

    public void failCancel() {
        items.forEach(OrderItem::failCancel);
    }

    public List<CanceledItemSnapshot> toSnapshot(Long buyerId) {
        return items.stream()
                .map(i -> new CanceledItemSnapshot(
                        i.getId(),
                        buyerId,
                        i.getTargetId(),
                        i.getTargetType(),
                        i.getAmount())
                )
                .toList();
    }
}
