package app.giftify.funding.adapter.inbound;

import app.giftify.funding.adapter.outbound.jpa.Funding;
import app.giftify.order.domain.OrderItemSnapshot;

public record FundingCreateResult(
        Funding funding,
        OrderItemSnapshot orderItemSnapshot
) {
}
