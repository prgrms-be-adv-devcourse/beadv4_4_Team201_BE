package app.giftify.product.application.service;

import app.giftify.product.adapter.outbound.jpa.repository.ProductStockHistoryRepository;
import app.giftify.product.application.port.out.ProductRepositoryPort;
import app.giftify.product.application.support.ProductSupport;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductStatus;
import app.giftify.product.domain.exception.ProductException;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.product.ProductSaleDisabledEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductSupport productSupport;

    @Mock
    private ProductStockHistoryRepository productStockHistoryRepository;

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("펀딩에 의한 재고 감소 성공 - 재고 5에서 4로 감소하고 재고 이력이 저장된다")
    void decreaseStockByFunding_Success() {
        // given
        Long productId = 1L;
        Product product = Product.builder()
                .id(productId)
                .sellerId(10L)
                .name("테스트 상품")
                .description("테스트 설명")
                .price(10000)
                .stock(5)
                .status(ProductStatus.ACTIVE)
                .build();
        given(productSupport.findById(productId)).willReturn(product);

        // when
        productService.decreaseStockByFunding(productId);

        // then
        assertThat(product.getStock()).isEqualTo(4);
        verify(productRepositoryPort).save(product);
    }

    @Test
    @DisplayName("펀딩에 의한 재고 감소 - 재고가 0이 되면 판매 중지 이벤트가 발행된다")
    void decreaseStockByFunding_StockBecomesZero() {
        // given
        Long productId = 1L;
        Product product = Product.builder()
                .id(productId)
                .sellerId(10L)
                .name("테스트 상품")
                .description("테스트 설명")
                .price(10000)
                .stock(1)
                .status(ProductStatus.ACTIVE)
                .build();
        given(productSupport.findById(productId)).willReturn(product);

        // when
        productService.decreaseStockByFunding(productId);

        // then
        assertThat(product.getStock()).isEqualTo(0);
        verify(productRepositoryPort).save(product);
        verify(eventPublisher).publish(any(ProductSaleDisabledEvent.class));
    }

    @Test
    @DisplayName("펀딩에 의한 재고 감소 실패 - 재고가 0이면 PRODUCT_OUT_OF_STOCK 예외가 발생한다")
    void decreaseStockByFunding_OutOfStock() {
        // given
        Long productId = 1L;
        Product product = Product.builder()
                .id(productId)
                .sellerId(10L)
                .name("테스트 상품")
                .description("테스트 설명")
                .price(10000)
                .stock(0)
                .status(ProductStatus.ACTIVE)
                .build();
        given(productSupport.findById(productId)).willReturn(product);

        // when & then
        assertThatThrownBy(() -> productService.decreaseStockByFunding(productId))
                .isInstanceOf(ProductException.class);
        assertThat(product.getStock()).isEqualTo(0);
        verify(productRepositoryPort, never()).save(product);
    }

    @Test
    @DisplayName("펀딩에 의한 재고 감소 실패 - 존재하지 않는 상품이면 예외가 발생한다")
    void decreaseStockByFunding_ProductNotFound() {
        // given
        Long productId = 999L;
        given(productSupport.findById(productId))
                .willThrow(new ProductException(app.giftify.product.domain.exception.ProductErrorCode.PRODUCT_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> productService.decreaseStockByFunding(productId))
                .isInstanceOf(ProductException.class);
    }
}
