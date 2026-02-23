package app.giftify.cart.adapter.inbound;

import app.giftify.cart.core.domain.CartItem;
import app.giftify.cart.core.domain.ItemStatus;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductStatus;
import app.giftify.shared.domain.type.TargetType;

public record CartItemResponse(
        TargetType targetType,
        Long targetId,
        String productName,
        long productPrice,
        long contributionAmount,
        ItemStatus status,
        String statusMessage
) {
    public static CartItemResponse from(CartItem item, Product product) {
        if (product == null) {
            return unavailable(item, ItemStatus.DISCONTINUED, "더 이상 판매되지 않는 상품입니다.");
        }
        if (product.getStock() <= 0) {
            return unavailable(item, ItemStatus.SOLD_OUT, "품절된 상품입니다.");
        }
        if (product.getStatus() != ProductStatus.ACTIVE) {
            return unavailable(item, ItemStatus.DISCONTINUED, "판매 중지된 상품입니다.");
        }
        // Todo: 펀딩 상태 필터링 추가

        return new CartItemResponse(
                item.getTargetType(),
                item.getTargetId(),
                product.getName(),
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
                0,
                item.getAmount().amount().longValue(),
                status,
                message
        );
    }
}