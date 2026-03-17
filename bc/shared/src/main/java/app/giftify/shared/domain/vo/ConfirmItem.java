package app.giftify.shared.domain.vo;

public record ConfirmItem(
        Long productId,
        int quantity
) {
    public static ConfirmItem ofFunding(Long productId) {
        return new ConfirmItem(productId, 1);
    }

    public static ConfirmItem of(Long productId, int quantity) {
        return new ConfirmItem(productId, quantity);
    }
}
