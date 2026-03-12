package app.giftify.product.adapter.inbound.event;

import app.giftify.product.application.port.in.DecreaseProductStockUseCase;
import app.giftify.product.application.port.in.StockHistoryCreateUseCase;
import app.giftify.product.domain.StockChangeResult;
import app.giftify.product.domain.StockChangeType;
import app.giftify.product.domain.event.ProductStockUpdatedEvent;
import app.giftify.product.domain.exception.ProductErrorCode;
import app.giftify.product.domain.exception.ProductException;
import app.giftify.shared.domain.event.order.OrderConfirmPendingEvent;
import app.giftify.shared.domain.vo.ConfirmItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
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
    @DisplayName("주문 확정 이벤트를 받으면 상품별 재고를 감소시킨다")
    void handleOrderConfirmed_Success() {
        // given
        List<ConfirmItem> items = List.of(
                ConfirmItem.of(1L, 2L),
                ConfirmItem.of(2L, 3L)
        );
        OrderConfirmPendingEvent event = new OrderConfirmPendingEvent(items);

        // when
        productEventListener.handleOrderConfirmed(event);

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<Long, Long>> captor = ArgumentCaptor.forClass(Map.class);
        verify(decreaseProductStockUseCase).decreaseStockByOrder(captor.capture());

        Map<Long, Long> captured = captor.getValue();
        assertThat(captured).containsEntry(1L, 2L);
        assertThat(captured).containsEntry(2L, 3L);
    }

    @Test
    @DisplayName("주문 확정 이벤트 처리 중 재고가 부족하면 예외가 전파된다")
    void handleOrderConfirmed_OutOfStock() {
        // given
        List<ConfirmItem> items = List.of(ConfirmItem.of(1L, 5L));
        OrderConfirmPendingEvent event = new OrderConfirmPendingEvent(items);

        doThrow(new ProductException(ProductErrorCode.PRODUCT_OUT_OF_STOCK))
                .when(decreaseProductStockUseCase).decreaseStockByOrder(anyMap());

        // when & then
        assertThatThrownBy(() -> productEventListener.handleOrderConfirmed(event))
                .isInstanceOf(ProductException.class);
    }

    @Test
    @DisplayName("주문 확정 이벤트 처리 중 상품이 존재하지 않으면 예외가 전파된다")
    void handleOrderConfirmed_ProductNotFound() {
        // given
        List<ConfirmItem> items = List.of(ConfirmItem.of(999L, 1L));
        OrderConfirmPendingEvent event = new OrderConfirmPendingEvent(items);

        doThrow(new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND))
                .when(decreaseProductStockUseCase).decreaseStockByOrder(anyMap());

        // when & then
        assertThatThrownBy(() -> productEventListener.handleOrderConfirmed(event))
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
