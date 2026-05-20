package app.giftify.product.application.service;

import app.giftify.product.application.support.ProductSupport;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductStatus;
import app.giftify.shared.domain.vo.ProductSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

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
                    createProduct(1L, 100L, 10000, 5, ProductStatus.ACTIVE),
                    createProduct(2L, 200L, 20000, 3, ProductStatus.ACTIVE)
            );
            given(productSupport.findAllById(productIds)).willReturn(products);

            // when
            Map<Long, ProductSnapshot> result = productSnapshotService.getSnapshots(productIds);

            // then
            assertThat(result).hasSize(2);

            assertThat(result.get(1L).productId()).isEqualTo(1L);
            assertThat(result.get(1L).price()).isEqualTo(10000);
            assertThat(result.get(1L).sellerId()).isEqualTo(100L);
            assertThat(result.get(1L).purchasable()).isTrue();

            assertThat(result.get(2L).productId()).isEqualTo(2L);
            assertThat(result.get(2L).price()).isEqualTo(20000);
            assertThat(result.get(2L).sellerId()).isEqualTo(200L);
            assertThat(result.get(2L).purchasable()).isTrue();
        }

        @Test
        @DisplayName("성공: 비활성 상품은 purchasable=false로 반환한다")
        void getSnapshots_InactiveProduct() {
            // given
            List<Long> productIds = List.of(1L);
            List<Product> products = List.of(
                    createProduct(1L, 100L, 10000, 5, ProductStatus.INACTIVE)
            );
            given(productSupport.findAllById(productIds)).willReturn(products);

            // when
            Map<Long, ProductSnapshot> result = productSnapshotService.getSnapshots(productIds);

            // then
            assertThat(result.get(1L).purchasable()).isFalse();
        }

        @Test
        @DisplayName("성공: 재고가 0인 상품은 purchasable=false로 반환한다")
        void getSnapshots_OutOfStock() {
            // given
            List<Long> productIds = List.of(1L);
            List<Product> products = List.of(
                    createProduct(1L, 100L, 10000, 0, ProductStatus.ACTIVE)
            );
            given(productSupport.findAllById(productIds)).willReturn(products);

            // when
            Map<Long, ProductSnapshot> result = productSnapshotService.getSnapshots(productIds);

            // then
            assertThat(result.get(1L).purchasable()).isFalse();
        }

        @Test
        @DisplayName("성공: 존재하지 않는 상품 ID는 Map에서 누락된다")
        void getSnapshots_MissingProduct() {
            // given
            List<Long> productIds = List.of(1L, 999L);
            List<Product> products = List.of(
                    createProduct(1L, 100L, 10000, 5, ProductStatus.ACTIVE)
            );
            given(productSupport.findAllById(productIds)).willReturn(products);

            // when
            Map<Long, ProductSnapshot> result = productSnapshotService.getSnapshots(productIds);

            // then
            assertThat(result).hasSize(1);
            assertThat(result).containsKey(1L);
            assertThat(result).doesNotContainKey(999L);
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

    private Product createProduct(Long id, Long sellerId, int price, int stock, ProductStatus status) {
        return Product.builder()
                .id(id)
                .sellerId(sellerId)
                .name("테스트 상품")
                .description("테스트 설명")
                .price(price)
                .stock(stock)
                .status(status)
                .build();
    }
}
