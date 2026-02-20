package app.giftify.shared.domain.event.order;

import app.giftify.shared.domain.vo.CanceledItemSnapshot;

import java.util.List;

public record OrderCanceledEvent(
        Long orderId,
        List<CanceledItemSnapshot> items
) {
}
