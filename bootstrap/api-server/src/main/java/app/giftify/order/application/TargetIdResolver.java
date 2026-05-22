package app.giftify.order.application;

import app.giftify.order.application.inbound.command.PlaceOrderItemCommand;
import app.giftify.order.domain.errorCode.OrderErrorCode;
import app.giftify.shared.api.exception.DomainException;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.FundingInfo;
import org.springframework.stereotype.Service;

@Service
public class TargetIdResolver {

    public Long resolve(PlaceOrderItemCommand command, TargetType targetType, FundingInfo fundingInfo) {
        return switch (targetType) {
            case DIRECT_PURCHASE -> command.productId();
            case DIRECT_GIFT, DIRECT_GIFT_ON_FUNDING, FUNDING_PENDING -> command.wishlistItemId();
            case FUNDING -> {
                if (!command.fundingId().equals(fundingInfo.fundingId()))
                    throw new DomainException(OrderErrorCode.INVALID_FUNDING_REFERENCE,
                            String.format("wishlistItemId = %d, requestedFundingId = %d, foundFundingId = %d",
                                    command.wishlistItemId(), command.fundingId(), fundingInfo.fundingId()));

                yield command.fundingId();
            }
        };
    }
}
