package app.giftify.order.application.inbound.command;

import app.giftify.order.domain.errorCode.OrderErrorCode;
import app.giftify.shared.api.exception.PolicyException;
import app.giftify.shared.domain.type.OrderItemType;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.Money;
import app.giftify.shared.domain.vo.WishlistItemSnapshot;
import app.giftify.support.common.annotation.Amount;
import app.giftify.support.common.annotation.Price;
import jakarta.validation.constraints.NotNull;

public record CreateOrderItemCommand (
        @NotNull
        Long targetId,
        @NotNull
        TargetType targetType,
        @NotNull
        Long receiverId,
        @NotNull
        Long sellerId,
        @NotNull @Price
        Money price,
        @NotNull @Amount
        Money amount,
        @NotNull
        OrderItemType orderItemType
){
    public static CreateOrderItemCommand of(OrderItemType orderItemType, long requestedWishlistId, long receiverId, Money requestAmount, WishlistItemSnapshot wishlistItemSnapshot, Long fundingId) {
        TargetType targetType = determinTargetType(fundingId, orderItemType);

        return new CreateOrderItemCommand(
                fundingId == null ? requestedWishlistId : fundingId,
                targetType,
                receiverId,
                wishlistItemSnapshot.sellerId(),
                Money.of(wishlistItemSnapshot.productPrice()),
                requestAmount,
                orderItemType
        );
    }

    private static TargetType determinTargetType(Long fundingId, OrderItemType orderItemType) {
        if (isNewFundingGifting(fundingId, orderItemType)) {
            return TargetType.FUNDING_PENDING;
        }
        if (isJoiningExistingFundingGifting(fundingId, orderItemType)) {
            return TargetType.FUNDING;
        }
        if (isNormalGifting(orderItemType)) {
            validateNormalGifting(fundingId);
            return TargetType.GENERAL_PRODUCT;
        }
        if (isNormalOrder(orderItemType)) {
            return TargetType.GENERAL_PRODUCT;
        }

        throw new PolicyException(OrderErrorCode.UNSUPPORTED_ORDER_COMBINATION);
    }

    private static boolean isJoiningExistingFundingGifting(Long fundingId, OrderItemType orderItemType) {
        return fundingId != null && orderItemType == OrderItemType.FUNDING_GIFT;
    }

    private static boolean isNewFundingGifting(Long fundingId, OrderItemType orderItemType) {
        return fundingId == null && orderItemType == OrderItemType.FUNDING_GIFT;
    }

    private static boolean isNormalGifting(OrderItemType orderItemType) {
        return orderItemType == OrderItemType.NORMAL_GIFT;
    }

    private static boolean isNormalOrder(OrderItemType orderItemType) {
        return orderItemType == OrderItemType.NORMAL_ORDER;
    }

    private static void validateNormalGifting(Long fundingId) {
        if (fundingId != null) {
            throw new PolicyException(OrderErrorCode.ALREADY_FUNDING_IN_PROGRESS);
        }
    }
}
