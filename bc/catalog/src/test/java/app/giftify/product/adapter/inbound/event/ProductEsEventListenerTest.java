package app.giftify.product.adapter.inbound.event;

import app.giftify.product.application.port.out.ProductEsPort;
import app.giftify.product.application.support.ProductSupport;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.event.ProductAcceptedEvent;
import app.giftify.shared.domain.event.product.ProductDeletedEvent;
import app.giftify.shared.domain.event.product.ProductSaleDisabledEvent;
import app.giftify.shared.domain.event.product.ProductSaleEnabledEvent;
import app.giftify.shared.domain.event.product.ProductUpdatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductEsEventListenerTest {

    private final ProductSupport productSupport = Mockito.mock(ProductSupport.class);
    private final ProductEsPort productEsPort = Mockito.mock(ProductEsPort.class);
    private final ProductEsEventListener listener = new ProductEsEventListener(productSupport, productEsPort);

    @Test
    @DisplayName("ProductAcceptedEvent가 발행되면 ES에 문서를 저장한다.")
    void handleProductAcceptedEvent() {
        // given
        Long productId = 1L;
        Product product = createProduct(productId);
        ProductAcceptedEvent event = new ProductAcceptedEvent(productId);

        when(productSupport.findById(productId)).thenReturn(product);

        // when
        listener.handleAccepted(event);

        // then
        verify(productSupport).findById(productId);
        verify(productEsPort).save(product);
    }

    @Test
    @DisplayName("ProductSaleEnabledEvent가 발행되면 ES 문서를 동기화한다.")
    void handleProductSaleEnabledEvent() {
        // given
        Long productId = 2L;
        Product product = createProduct(productId);
        ProductSaleEnabledEvent event = new ProductSaleEnabledEvent(productId);

        when(productSupport.findById(productId)).thenReturn(product);

        // when
        listener.handleSaleEnabled(event);

        // then
        verify(productSupport).findById(productId);
        verify(productEsPort).save(product);
    }

    @Test
    @DisplayName("ProductSaleDisabledEvent가 발행되면 ES 문서를 동기화한다.")
    void handleProductSaleDisabledEvent() {
        // given
        Long productId = 3L;
        Product product = createProduct(productId);
        ProductSaleDisabledEvent event = new ProductSaleDisabledEvent(productId);

        when(productSupport.findById(productId)).thenReturn(product);

        // when
        listener.handleSaleDisabled(event);

        // then
        verify(productSupport).findById(productId);
        verify(productEsPort).save(product);
    }

    @Test
    @DisplayName("ProductUpdatedEvent가 발행되면 ES 문서를 업데이트한다.")
    void handleProductUpdatedEvent() {
        // given
        Long productId = 4L;
        Product product = createProduct(productId);
        ProductUpdatedEvent event = new ProductUpdatedEvent(productId, 10000, "테스트 상품", "image-key");

        when(productSupport.findById(productId)).thenReturn(product);

        // when
        listener.handleUpdated(event);

        // then
        verify(productSupport).findById(productId);
        verify(productEsPort).save(product);
    }

    @Test
    @DisplayName("ProductDeletedEvent가 발행되면 ES 문서를 삭제한다.")
    void handleProductDeletedEvent() {
        // given
        Long productId = 5L;
        ProductDeletedEvent event = new ProductDeletedEvent(productId);

        // when
        listener.handleDeleted(event);

        // then
        verify(productEsPort).deleteById(productId);
    }

    private Product createProduct(Long productId) {
        return Product.builder()
                .id(productId)
                .sellerId(1L)
                .name("테스트 상품")
                .description("테스트 상품 설명")
                .price(10000)
                .stock(100)
                .build();
    }
}
