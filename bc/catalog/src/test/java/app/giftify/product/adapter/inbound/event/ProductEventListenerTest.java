package app.giftify.product.adapter.inbound.event;

import app.giftify.product.application.port.in.DecreaseProductStockUseCase;
import app.giftify.product.application.port.in.StockHistoryCreateUseCase;
import app.giftify.product.domain.StockChangeResult;
import app.giftify.product.domain.StockChangeType;
import app.giftify.product.domain.event.ProductStockUpdatedEvent;
import app.giftify.product.domain.exception.ProductErrorCode;
import app.giftify.product.domain.exception.ProductException;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.order.OrderConfirmPendingEvent;
import app.giftify.shared.domain.event.product.ProductSellerOrderReceivedEvent;
import app.giftify.shared.domain.vo.ConfirmItem;
import app.giftify.shared.domain.vo.SellerOrderItem;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ProductEventListenerTest {

    @Mock
    private DecreaseProductStockUseCase decreaseProductStockUseCase;

    @Mock
    private StockHistoryCreateUseCase stockHistoryCreateUseCase;

    @InjectMocks
    private ProductEventListener productEventListener;

    @Mock
    private EventPublisher eventPublisher;

    @Test
    @DisplayName("주문 확정 이벤트를 받으면 상품별 재고를 감소시키고 판매자 주문 인입 이벤트를 발행한다")
    void handleOrderConfirmed_Success() {
        // given
        List<ConfirmItem> items = List.of(
                ConfirmItem.of(1L, 2),
                ConfirmItem.of(2L, 3)
        );
        OrderConfirmPendingEvent event = new OrderConfirmPendingEvent(items);

        List<SellerOrderItem> sellerOrderItems = List.of(
                new SellerOrderItem(10L, 1L, "상품A", 2),
                new SellerOrderItem(20L, 2L, "상품B", 3)
        );
        given(decreaseProductStockUseCase.decreaseStockByOrder(anyMap())).willReturn(sellerOrderItems);

        // when
        productEventListener.handleOrderConfirmed(event);

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<Long, Integer>> captor = ArgumentCaptor.forClass(Map.class);
        verify(decreaseProductStockUseCase).decreaseStockByOrder(captor.capture());

        Map<Long, Integer> captured = captor.getValue();
        assertThat(captured).containsEntry(1L, 2);
        assertThat(captured).containsEntry(2L, 3);

        ArgumentCaptor<ProductSellerOrderReceivedEvent> eventCaptor =
                ArgumentCaptor.forClass(ProductSellerOrderReceivedEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getItems()).hasSize(2);
    }

    @Test
    @DisplayName("주문 확정 이벤트 처리 중 재고가 부족하면 예외가 전파되고 이벤트가 발행되지 않는다")
    void handleOrderConfirmed_OutOfStock() {
        // given
        List<ConfirmItem> items = List.of(ConfirmItem.of(1L, 5));
        OrderConfirmPendingEvent event = new OrderConfirmPendingEvent(items);

        doThrow(new ProductException(ProductErrorCode.PRODUCT_OUT_OF_STOCK))
                .when(decreaseProductStockUseCase).decreaseStockByOrder(anyMap());

        // when & then
        assertThatThrownBy(() -> productEventListener.handleOrderConfirmed(event))
                .isInstanceOf(ProductException.class);
        verify(eventPublisher, never()).publish(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("주문 확정 이벤트 처리 중 상품이 존재하지 않으면 예외가 전파되고 이벤트가 발행되지 않는다")
    void handleOrderConfirmed_ProductNotFound() {
        // given
        List<ConfirmItem> items = List.of(ConfirmItem.of(999L, 1));
        OrderConfirmPendingEvent event = new OrderConfirmPendingEvent(items);

        doThrow(new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND))
                .when(decreaseProductStockUseCase).decreaseStockByOrder(anyMap());

        // when & then
        assertThatThrownBy(() -> productEventListener.handleOrderConfirmed(event))
                .isInstanceOf(ProductException.class);
        verify(eventPublisher, never()).publish(org.mockito.ArgumentMatchers.any());
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
