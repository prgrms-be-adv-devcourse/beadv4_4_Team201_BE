package app.giftify.cart.adapter.inbound;

import app.giftify.cart.core.domain.CartItem;
import app.giftify.product.domain.Product;
import app.giftify.shared.domain.type.TargetType;

public record CartItemResponse(
        TargetType targetType,
        Long targetId,
        String productName,
        int productPrice,          // 상품 원래 가격
        long contributionAmount,    // 사용자가 담은 펀딩 금액
        boolean saleStatus          // FIXME : 판매 가능 여부 -> 이제 필요 없는거 아닌가?
) {
    public static CartItemResponse from(CartItem item, Product product) {
        return new CartItemResponse(
                item.getTargetType(),
                item.getTargetId(),
                product.getName(),
                product.getPrice(),
                item.getAmount().amount().longValue(),
                product.isSale()
        );
    }
}