package app.giftify.shared.domain.vo;

public record ConfirmItem(
        Long productId,
        Long quantity
) {
    public static ConfirmItem ofFunding(Long productId) {
        return new ConfirmItem(productId, 1L);
    }

    public static ConfirmItem of(Long productId, Long quantity) {
        return new ConfirmItem(productId, quantity);
    }
}
