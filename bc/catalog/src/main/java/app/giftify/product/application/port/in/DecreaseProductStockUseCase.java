package app.giftify.product.application.port.in;

public interface DecreaseProductStockUseCase {
    // 펀딩에 의한 재고 감소
    void decreaseStockByFunding(Long productId);

    // todo 일반 주문에 의한 재고 감소
}
