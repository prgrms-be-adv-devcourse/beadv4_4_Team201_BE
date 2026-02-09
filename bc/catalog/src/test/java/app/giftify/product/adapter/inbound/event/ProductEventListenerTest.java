package app.giftify.product.adapter.inbound.event;

import app.giftify.product.application.port.in.DecreaseProductStockUseCase;
import app.giftify.product.application.port.in.StockHistoryCreateUseCase;
import app.giftify.product.domain.StockChangeResult;
import app.giftify.product.domain.StockChangeType;
import app.giftify.product.domain.event.ProductStockUpdatedEvent;
import app.giftify.product.domain.exception.ProductErrorCode;
import app.giftify.product.domain.exception.ProductException;
import app.giftify.shared.domain.event.funding.FundingAcceptedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class ProductEventListenerTest {

    @Mock
    private DecreaseProductStockUseCase decreaseProductStockUseCase;

    @Mock
    private StockHistoryCreateUseCase stockHistoryCreateUseCase;

    @InjectMocks
    private ProductEventListener productEventListener;

    @Test
    @DisplayName("펀딩 수락 이벤트를 받으면 해당 상품의 재고를 감소시킨다")
    void handleFundingAccepted_Success() {
        // given
        Long productId = 1L;
        FundingAcceptedEvent event = new FundingAcceptedEvent(
                100L, 200L, productId, 3L, LocalDateTime.now()
        );

        // when
        productEventListener.handleFundingAccepted(event);

        // then
        verify(decreaseProductStockUseCase).decreaseStockByFunding(productId);
    }

    @Test
    @DisplayName("펀딩 수락 이벤트 처리 중 재고가 부족하면 예외가 전파된다")
    void handleFundingAccepted_OutOfStock() {
        // given
        Long productId = 1L;
        FundingAcceptedEvent event = new FundingAcceptedEvent(
                100L, 200L, productId, 3L, LocalDateTime.now()
        );
        doThrow(new ProductException(ProductErrorCode.PRODUCT_OUT_OF_STOCK))
                .when(decreaseProductStockUseCase).decreaseStockByFunding(productId);

        // when & then
        assertThatThrownBy(() -> productEventListener.handleFundingAccepted(event))
                .isInstanceOf(ProductException.class);
    }

    @Test
    @DisplayName("펀딩 수락 이벤트 처리 중 상품이 존재하지 않으면 예외가 전파된다")
    void handleFundingAccepted_ProductNotFound() {
        // given
        Long productId = 999L;
        FundingAcceptedEvent event = new FundingAcceptedEvent(
                100L, 200L, productId, 3L, LocalDateTime.now()
        );
        doThrow(new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND))
                .when(decreaseProductStockUseCase).decreaseStockByFunding(productId);

        // when & then
        assertThatThrownBy(() -> productEventListener.handleFundingAccepted(event))
                .isInstanceOf(ProductException.class);
    }

    @Test
    @DisplayName("재고 변경 이벤트를 받으면 재고 이력을 생성한다")
    void handleStockUpdated_Success() {
        // given
        StockChangeResult result = new StockChangeResult(
                10L, 1L, 5, 4, -1, StockChangeType.ORDER_COMPLETED
        );
        ProductStockUpdatedEvent event = new ProductStockUpdatedEvent(result);

        // when
        productEventListener.handleStockUpdated(event);

        // then
        verify(stockHistoryCreateUseCase).createStockHistory(result);
    }
}
