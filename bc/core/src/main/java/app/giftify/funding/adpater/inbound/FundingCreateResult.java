package app.giftify.funding.adpater.inbound;

import app.giftify.funding.adpater.outbound.jpa.Funding;
import app.giftify.orderDemo.domain.OrderItemSnapshot;

public record FundingCreateResult(
        Funding funding,
        OrderItemSnapshot orderItemSnapshot
) {
}
