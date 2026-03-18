package app.giftify.product.application.service;

import app.giftify.product.application.support.ProductSupport;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductStatus;
import app.giftify.product.domain.exception.ProductNotActiveException;
import app.giftify.product.domain.exception.ProductOutOfStockException;
import app.giftify.shared.domain.vo.ProductSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductSnapshotServiceTest {

    @Mock
    private ProductSupport productSupport;

    @InjectMocks
    private ProductSnapshotService productSnapshotService;

    @Nested
    @DisplayName("상품 스냅샷 조회 (getSnapshots)")
    class GetSnapshotsTests {

        @Test
        @DisplayName("성공: 상품 ID 목록으로 스냅샷을 조회한다")
        void getSnapshots_Success() {
            // given
            List<Long> productIds = List.of(1L, 2L);
            List<Product> products = List.of(
                    createProduct(1L, 100L, 10000, 5),
                    createProduct(2L, 200L, 20000, 3)
            );
            given(productSupport.findAllById(productIds)).willReturn(products);

            // when
            Map<Long, ProductSnapshot> result = productSnapshotService.getSnapshots(productIds);

            // then
            assertThat(result).hasSize(2);

            assertThat(result.get(1L).productId()).isEqualTo(1L);
            assertThat(result.get(1L).price()).isEqualTo(10000);
            assertThat(result.get(1L).sellerId()).isEqualTo(100L);

            assertThat(result.get(2L).productId()).isEqualTo(2L);
            assertThat(result.get(2L).price()).isEqualTo(20000);
            assertThat(result.get(2L).sellerId()).isEqualTo(200L);

            verify(productSupport).validatePurchasable(products);
        }

        @Test
        @DisplayName("성공: 단건 상품 스냅샷을 조회한다")
        void getSnapshots_SingleProduct() {
            // given
            List<Long> productIds = List.of(1L);
            List<Product> products = List.of(createProduct(1L, 100L, 15000, 10));
            given(productSupport.findAllById(productIds)).willReturn(products);

            // when
            Map<Long, ProductSnapshot> result = productSnapshotService.getSnapshots(productIds);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(1L).productId()).isEqualTo(1L);
            assertThat(result.get(1L).price()).isEqualTo(15000);
            assertThat(result.get(1L).sellerId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("실패: 판매 중이 아닌 상품이 포함되면 ProductNotActiveException이 발생한다")
        void getSnapshots_Fail_ProductNotActive() {
            // given
            List<Long> productIds = List.of(1L, 2L);
            List<Product> products = List.of(
                    createProduct(1L, 100L, 10000, 5),
                    createProduct(2L, 200L, 20000, 3)
            );
            given(productSupport.findAllById(productIds)).willReturn(products);
            Mockito.doThrow(new ProductNotActiveException(2L))
                    .when(productSupport).validatePurchasable(products);

            // when & then
            assertThatThrownBy(() -> productSnapshotService.getSnapshots(productIds))
                    .isInstanceOf(ProductNotActiveException.class);
        }

        @Test
        @DisplayName("실패: 재고가 없는 상품이 포함되면 ProductOutOfStockException이 발생한다")
        void getSnapshots_Fail_ProductOutOfStock() {
            // given
            List<Long> productIds = List.of(1L, 2L);
            List<Product> products = List.of(
                    createProduct(1L, 100L, 10000, 5),
                    createProduct(2L, 200L, 20000, 3)
            );
            given(productSupport.findAllById(productIds)).willReturn(products);
            Mockito.doThrow(new ProductOutOfStockException(2L))
                    .when(productSupport).validatePurchasable(products);

            // when & then
            assertThatThrownBy(() -> productSnapshotService.getSnapshots(productIds))
                    .isInstanceOf(ProductOutOfStockException.class);
        }

        @Test
        @DisplayName("성공: 빈 목록을 전달하면 빈 Map을 반환한다")
        void getSnapshots_EmptyList() {
            // given
            List<Long> productIds = List.of();
            given(productSupport.findAllById(productIds)).willReturn(List.of());

            // when
            Map<Long, ProductSnapshot> result = productSnapshotService.getSnapshots(productIds);

            // then
            assertThat(result).isEmpty();
        }
    }

    private Product createProduct(Long id, Long sellerId, int price, int stock) {
        return Product.builder()
                .id(id)
                .sellerId(sellerId)
                .name("테스트 상품")
                .description("테스트 설명")
                .price(price)
                .stock(stock)
                .status(ProductStatus.ACTIVE)
                .build();
    }
}
