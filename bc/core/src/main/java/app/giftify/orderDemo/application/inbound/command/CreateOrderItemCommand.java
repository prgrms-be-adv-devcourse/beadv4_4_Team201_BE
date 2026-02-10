package app.giftify.orderDemo.application.inbound.command;

import app.giftify.orderDemo.adapter.inbound.web.dto.request.PlaceOrderItemRequest;
import app.giftify.orderDemo.domain.errorCode.OrderErrorCode;
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
    public static CreateOrderItemCommand of(PlaceOrderItemRequest request, WishlistItemSnapshot wishlistItemSnapshot, Long fundingId) {
        TargetType targetType = determinTargetType(fundingId, request.orderItemType());

        return new CreateOrderItemCommand(
                fundingId == null ? request.wishlistItemId() : fundingId,
                targetType,
                request.receiverId(),
                wishlistItemSnapshot.sellerId(),
                Money.of(wishlistItemSnapshot.productPrice()),
                request.amount(),
                request.orderItemType()
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

