package app.giftify.cart.adapter.inbound;

import app.giftify.cart.core.domain.CartItem;
import app.giftify.cart.core.domain.ItemStatus;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductStatus;
import app.giftify.shared.domain.type.TargetType;

public record CartItemResponse(
        TargetType targetType,
        Long targetId,
        Long receiverId,
        String productName,
        String imageKey,
        long productPrice,
        long contributionAmount,
        ItemStatus status,
        String statusMessage
) {
    public static CartItemResponse from(CartItem item, boolean isFundingEnded, Product product, Long receiverId) {
        if (isFundingEnded) {
            return unavailable(item, ItemStatus.FUNDING_ENDED, "진행 중인 펀딩이 아닙니다.");
        }
        if (product == null) {
            return unavailable(item, ItemStatus.DISCONTINUED, "더 이상 판매되지 않는 상품입니다.");
        }
        if (product.getStock() <= 0) {
            return unavailable(item, ItemStatus.SOLD_OUT, "품절된 상품입니다.");
        }
        if (product.getStatus() != ProductStatus.ACTIVE) {
            return unavailable(item, ItemStatus.DISCONTINUED, "판매 중지된 상품입니다.");
        }

        return new CartItemResponse(
                item.getTargetType(),
                item.getTargetId(),
                receiverId,
                product.getName(),
                product.getImageKey(),
                (long) product.getPrice(),
                item.getAmount().amount().longValue(),
                ItemStatus.AVAILABLE,
                null
        );
    }

    private static CartItemResponse unavailable(CartItem item, ItemStatus status, String message) {
        return new CartItemResponse(
                item.getTargetType(),
                item.getTargetId(),
                null,
                null,
                null,
                0,
                item.getAmount().amount().longValue(),
                status,
                message
        );
    }
}
