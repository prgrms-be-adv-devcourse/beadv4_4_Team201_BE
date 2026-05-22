package app.giftify.order.application;

import app.giftify.order.domain.errorCode.OrderErrorCode;
import app.giftify.shared.api.exception.PolicyException;
import app.giftify.shared.domain.type.OrderItemType;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.FundingInfo;
import org.springframework.stereotype.Service;

@Service
public class TargetTypeResolver {

    public TargetType resolve(OrderItemType orderItemType, FundingInfo fundingInfo) {
        if (isNewFundingGifting(orderItemType, fundingInfo)) {
            return TargetType.FUNDING_PENDING;
        }
        if (isJoiningExistingFundingGifting(orderItemType, fundingInfo)) {
            return TargetType.FUNDING;
        }
        if (isNormalGiftingOnFunding(orderItemType, fundingInfo)) {
            return TargetType.DIRECT_GIFT_ON_FUNDING;
        }
        if (isNormalGifting(orderItemType, fundingInfo)) {
            return TargetType.DIRECT_GIFT;
        }
        if (isNormalOrder(orderItemType)) {
            return TargetType.DIRECT_PURCHASE;
        }

        throw new PolicyException(OrderErrorCode.UNSUPPORTED_ORDER_COMBINATION);
    }

    private static boolean isNewFundingGifting(OrderItemType orderItemType, FundingInfo fundingInfo) {
        return orderItemType == OrderItemType.FUNDING_GIFT && fundingInfo == null;
    }

    private static boolean isJoiningExistingFundingGifting(OrderItemType orderItemType, FundingInfo fundingInfo) {
        return orderItemType == OrderItemType.FUNDING_GIFT && fundingInfo != null;
    }

    private static boolean isNormalGiftingOnFunding(OrderItemType orderItemType, FundingInfo fundingInfo) {
        return orderItemType == OrderItemType.NORMAL_GIFT && fundingInfo != null;
    }

    private static boolean isNormalGifting(OrderItemType orderItemType, FundingInfo fundingInfo) {
        return orderItemType == OrderItemType.NORMAL_GIFT && fundingInfo == null;
    }

    private static boolean isNormalOrder(OrderItemType orderItemType) {
        return orderItemType == OrderItemType.NORMAL_ORDER;
    }
}
