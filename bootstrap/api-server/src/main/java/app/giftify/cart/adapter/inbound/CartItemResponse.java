package app.giftify.cart.adapter.inbound;

import app.giftify.cart.core.domain.CartItem;
import app.giftify.cart.core.domain.ItemStatus;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductStatus;
import app.giftify.funding.domain.type.FundingStatus;
import app.giftify.funding.domain.vo.FundingInfo;

/**
 * “펀딩이 존재하는가?” ❌
 * “지금 화면에서 이동 가능한가?” ✅
 * -> 어차피 타인의 펀딩 조회 시, 종료된 펀딩은 볼 수 없는 정책임
 */
public record CartItemResponse(
        Long wishlistItemId,
        Long wishlistId,
        Long receiverId,
        String receiverNickname,
        Long productId,
        String productName,
        String imageKey,
        long productPrice,
        long contributionAmount,

        // 이동 가능한 경우에만 세팅
        Long fundingId,
        Integer currentAmount,

        ItemStatus status,
        String statusMessage
) {
    public static CartItemResponse from(CartItem item, boolean isFundingEnded, Product product, Long receiverId, String receiverNickname, FundingInfo fundingInfo, Long wishlistId) {

        if (isFundingEnded) {
            return unavailable(item, receiverId, receiverNickname, ItemStatus.FUNDING_ENDED, "종료된 펀딩입니다.", wishlistId);
        }
        if (product == null) {
            return unavailable(item, receiverId, receiverNickname, ItemStatus.DISCONTINUED, "더 이상 판매되지 않는 상품입니다.", wishlistId);
        }
        if (product.getStatus() != ProductStatus.ACTIVE) {
            return unavailable(item, receiverId, receiverNickname, ItemStatus.DISCONTINUED, "판매 중지된 상품입니다.", wishlistId);
        }

        if (product.getStock() <= 0) {
            return new CartItemResponse(
                    item.getWishlistItemId(),
                    wishlistId,
                    receiverId,
                    receiverNickname,
                    product.getId(),
                    product.getName(),
                    product.getImageKey(),
                    (long) product.getPrice(),
                    item.getAmount().amount().longValue(),
                    null,
                    null,
                    ItemStatus.SOLD_OUT,
                    "품절된 상품입니다."
            );
        }

        Long fundingId = null;
        Integer currentAmount = null;

        if (fundingInfo != null && fundingInfo.status() == FundingStatus.IN_PROGRESS) {
            fundingId = fundingInfo.fundingId();
            currentAmount = fundingInfo.currentAmount();
        }

        return new CartItemResponse(
                item.getWishlistItemId(),
                wishlistId,
                receiverId,
                receiverNickname,
                product.getId(),
                product.getName(),
                product.getImageKey(),
                (long) product.getPrice(),
                item.getAmount().amount().longValue(),
                fundingId,
                currentAmount,
                ItemStatus.AVAILABLE,
                null
        );
    }

    private static CartItemResponse unavailable(CartItem item, Long receiverId, String receiverNickname, ItemStatus status, String message, Long wishlistId) {
        return new CartItemResponse(
                item.getWishlistItemId(),
                wishlistId,
                receiverId,
                receiverNickname,
                null,
                null,
                null,
                0,
                item.getAmount().amount().longValue(),
                null,
                null,
                status,
                message
        );
    }
}
