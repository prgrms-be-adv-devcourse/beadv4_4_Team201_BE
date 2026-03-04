package app.giftify.product.application.port.out;

public interface FundingClientPort {
    boolean checkFundingExistsByProductId(Long productId);
}
