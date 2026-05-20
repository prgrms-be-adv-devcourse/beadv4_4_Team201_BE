package app.giftify.product.application.service;

import app.giftify.product.application.port.in.StockHistorySearchCommand;
import app.giftify.product.application.port.out.ProductStockHistoryRepositoryPort;
import app.giftify.product.domain.ProductStockHistory;
import app.giftify.product.domain.StockChangeResult;
import app.giftify.product.domain.StockChangeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ProductStockHistoryServiceTest {

    @Mock
    private ProductStockHistoryRepositoryPort stockHistoryRepositoryPort;

    @InjectMocks
    private ProductStockHistoryService productStockHistoryService;

    @Test
    @DisplayName("재고 이력 생성 - StockChangeResult의 값으로 이력이 저장된다")
    void createStockHistory_Success() {
        // given
        StockChangeResult result = new StockChangeResult(
                10L, 1L, 5, 4, -1, StockChangeType.ORDER_COMPLETED
        );

        // when
        productStockHistoryService.createStockHistory(result);

        // then
        ArgumentCaptor<ProductStockHistory> captor = ArgumentCaptor.forClass(ProductStockHistory.class);
        verify(stockHistoryRepositoryPort).save(captor.capture());

        ProductStockHistory saved = captor.getValue();
        assertThat(saved.getSellerId()).isEqualTo(10L);
        assertThat(saved.getProductId()).isEqualTo(1L);
        assertThat(saved.getBeforeStock()).isEqualTo(5);
        assertThat(saved.getAfterStock()).isEqualTo(4);
        assertThat(saved.getDelta()).isEqualTo(-1);
        assertThat(saved.getChangeType()).isEqualTo(StockChangeType.ORDER_COMPLETED);
    }

    @Test
    @DisplayName("재고 이력 생성 - 판매자 수동 조정 이력이 올바르게 저장된다")
    void createStockHistory_ManualSeller() {
        // given
        StockChangeResult result = new StockChangeResult(
                10L, 1L, 45, 50, 5, StockChangeType.MANUAL_SELLER
        );

        // when
        productStockHistoryService.createStockHistory(result);

        // then
        ArgumentCaptor<ProductStockHistory> captor = ArgumentCaptor.forClass(ProductStockHistory.class);
        verify(stockHistoryRepositoryPort).save(captor.capture());

        ProductStockHistory saved = captor.getValue();
        assertThat(saved.getDelta()).isEqualTo(5);
        assertThat(saved.getChangeType()).isEqualTo(StockChangeType.MANUAL_SELLER);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("재고 이력 생성 - 환불 복구 이력이 올바르게 저장된다")
    void createStockHistory_OrderRefunded() {
        // given
        StockChangeResult result = new StockChangeResult(
                10L, 1L, 4, 5, 1, StockChangeType.ORDER_REFUNDED
        );

        // when
        productStockHistoryService.createStockHistory(result);

        // then
        ArgumentCaptor<ProductStockHistory> captor = ArgumentCaptor.forClass(ProductStockHistory.class);
        verify(stockHistoryRepositoryPort).save(captor.capture());

        ProductStockHistory saved = captor.getValue();
        assertThat(saved.getDelta()).isEqualTo(1);
        assertThat(saved.getBeforeStock()).isEqualTo(4);
        assertThat(saved.getAfterStock()).isEqualTo(5);
        assertThat(saved.getChangeType()).isEqualTo(StockChangeType.ORDER_REFUNDED);
    }

    @Test
    @DisplayName("재고 이력 생성 실패 - TransientDataAccessException 발생 시 예외가 전파된다 (재시도 대상)")
    void createStockHistory_TransientDataAccessException() {
        // given
        StockChangeResult result = new StockChangeResult(
                10L, 1L, 5, 4, -1, StockChangeType.ORDER_COMPLETED
        );
        willThrow(new QueryTimeoutException("DB query timeout"))
                .given(stockHistoryRepositoryPort).save(any(ProductStockHistory.class));

        // when & then
        assertThatThrownBy(() -> productStockHistoryService.createStockHistory(result))
                .isInstanceOf(TransientDataAccessException.class);
    }

    @Test
    @DisplayName("재고 이력 생성 최종 실패 - @Recover 메서드가 예외 없이 실행된다")
    void recoverCreateStockHistory_DoesNotThrow() {
        // given
        StockChangeResult result = new StockChangeResult(
                10L, 1L, 5, 4, -1, StockChangeType.ORDER_COMPLETED
        );
        DataAccessException exception = new QueryTimeoutException("DB query timeout");

        // when & then - recover 메서드는 로그만 남기고 예외를 던지지 않는다
        productStockHistoryService.recoverCreateStockHistory(exception, result);
    }

    @Test
    @DisplayName("재고 이력 조회 - sellerId와 검색 조건으로 repositoryPort에 위임한다")
    void searchStockHistories_Success() {
        // given
        Long sellerId = 10L;
        StockHistorySearchCommand searchCommand = new StockHistorySearchCommand(
                1L, StockChangeType.ORDER_COMPLETED, null, null, null, null, null
        );
        Page<ProductStockHistory> expectedPage = new PageImpl<>(List.of());
        given(stockHistoryRepositoryPort.searchStockHistories(eq(sellerId), eq(searchCommand)))
                .willReturn(expectedPage);

        // when
        Page<ProductStockHistory> result = productStockHistoryService.searchStockHistories(sellerId, searchCommand);

        // then
        assertThat(result).isEqualTo(expectedPage);
        verify(stockHistoryRepositoryPort).searchStockHistories(sellerId, searchCommand);
    }

    @Test
    @DisplayName("재고 이력 조회 - 검색 조건 없이 전체 조회를 위임한다")
    void searchStockHistories_NoFilter() {
        // given
        Long sellerId = 10L;
        StockHistorySearchCommand searchCommand = new StockHistorySearchCommand(
                null, null, null, null, null, null, null
        );
        Page<ProductStockHistory> expectedPage = new PageImpl<>(List.of());
        given(stockHistoryRepositoryPort.searchStockHistories(eq(sellerId), eq(searchCommand)))
                .willReturn(expectedPage);

        // when
        Page<ProductStockHistory> result = productStockHistoryService.searchStockHistories(sellerId, searchCommand);

        // then
        assertThat(result).isEqualTo(expectedPage);
        verify(stockHistoryRepositoryPort).searchStockHistories(sellerId, searchCommand);
    }
}
