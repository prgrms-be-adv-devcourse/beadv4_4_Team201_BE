package app.giftify.product.application.port.in;

public interface DecreaseProductStockUseCase {
    // 펀딩에 의한 재고 감소
    void decreaseStockByFunding(Long productid);

    // todo 일반 주문에 의한 재고 감소
}
