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
        String receiverNickname,
        String productName,
        String imageKey,
        long productPrice,
        long contributionAmount,
        ItemStatus status,
        String statusMessage
) {
    public static CartItemResponse from(CartItem item, boolean isFundingEnded, Product product, Long receiverId, String receiverNickname) {
        if (isFundingEnded) {
            return unavailable(item, receiverId, receiverNickname, ItemStatus.FUNDING_ENDED, "종료된 펀딩입니다.");
        }
        if (product == null) {
            return unavailable(item, receiverId, receiverNickname,ItemStatus.DISCONTINUED, "더 이상 판매되지 않는 상품입니다.");
        }
        if (product.getStatus() != ProductStatus.ACTIVE) {
            return unavailable(item, receiverId, receiverNickname, ItemStatus.DISCONTINUED, "판매 중지된 상품입니다.");
        }
        if (product.getStock() <= 0) {
            return new CartItemResponse(
                    item.getTargetType(),
                    item.getTargetId(),
                    receiverId,
                    receiverNickname,
                    product.getName(),
                    product.getImageKey(),
                    (long) product.getPrice(),
                    item.getAmount().amount().longValue(),
                    ItemStatus.SOLD_OUT,
                    "품절된 상품입니다."
            );
        }

        return new CartItemResponse(
                item.getTargetType(),
                item.getTargetId(),
                receiverId,
                receiverNickname,
                product.getName(),
                product.getImageKey(),
                (long) product.getPrice(),
                item.getAmount().amount().longValue(),
                ItemStatus.AVAILABLE,
                null
        );
    }

    private static CartItemResponse unavailable(CartItem item,  Long receiverId, String receiverNickname, ItemStatus status, String message) {
        return new CartItemResponse(
                item.getTargetType(),
                item.getTargetId(),
                receiverId,
                receiverNickname,
                null,
                null,
                0,
                item.getAmount().amount().longValue(),
                status,
                message
        );
    }
}
