package app.giftify.orderDemo.domain;

import app.giftify.shared.domain.vo.CanceledItemSnapshot;
import app.giftify.shared.domain.vo.Money;

import java.time.LocalDateTime;
import java.util.List;

public record CancelTargetItems(List<OrderItem> items) {

    public Money calculateCancelAmount() {
        return items.stream()
                .map(OrderItem::getAmount)
                .reduce(Money.zero(), Money::plus);
    }

    public void pendingToCancel(LocalDateTime cancelRequestedAt) {
        items.forEach(i -> i.pendingToCancel(cancelRequestedAt));
    }

    public void cancel(LocalDateTime canceledAt) {
        items.forEach(i -> i.cancel(canceledAt));
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
