package app.giftify.product.application.service;

import app.giftify.product.adapter.inbound.web.requestDto.MyProductSearchDto;
import app.giftify.product.adapter.inbound.web.requestDto.ProductUpdateRequestDto;
import app.giftify.product.application.port.in.MyProductResult;
import app.giftify.product.application.port.in.ProductUpdateResult;
import app.giftify.product.application.port.out.FundingClientPort;
import app.giftify.product.application.port.out.MyProductSearchCommand;
import app.giftify.product.application.port.out.ProductRepositoryPort;
import app.giftify.product.application.port.out.ProductStockHistoryRepositoryPort;
import app.giftify.product.application.support.ProductSupport;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductStatus;
import app.giftify.product.domain.event.ProductStockUpdatedEvent;
import app.giftify.product.domain.exception.ProductErrorCode;
import app.giftify.product.domain.exception.ProductException;
import app.giftify.replica.member.MemberRepository;
import app.giftify.shared.api.paging.PageResponse;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.product.ProductSaleDisabledEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductSupport productSupport;

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ProductStockHistoryRepositoryPort productStockHistoryRepositoryPort;

    @Mock
    private FundingClientPort fundingClientPort;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private ProductService productService;

    @Nested
    @DisplayName("재고 관리 기능 테스트 (decreaseStockByOrder)")
    class StockManagementTests {
        @Test
        @DisplayName("성공: 재고 5에서 3으로 감소하고 재고 변경 이벤트가 발행된다")
        void decreaseStockByOrder_Success() {
            // given
            Long productId = 1L;
            Product product = createProduct(productId, 5);
            given(productSupport.findByIdForUpdate(productId)).willReturn(product);

            // when
            productService.decreaseStockByOrder(Map.of(productId, 2L));

            // then
            assertThat(product.getStock()).isEqualTo(3);
            verify(productRepositoryPort).save(product);
            verify(eventPublisher).publish(any(ProductStockUpdatedEvent.class));
        }

        @Test
        @DisplayName("성공: 재고가 0이 되면 판매 중지 이벤트가 발행된다")
        void decreaseStockByOrder_StockBecomesZero() {
            // given
            Long productId = 1L;
            Product product = createProduct(productId, 1);
            given(productSupport.findByIdForUpdate(productId)).willReturn(product);

            // when
            productService.decreaseStockByOrder(Map.of(productId, 1L));

            // then
            assertThat(product.getStock()).isEqualTo(0);
            verify(productRepositoryPort).save(product);
            verify(eventPublisher).publish(any(ProductSaleDisabledEvent.class));
        }

        @Test
        @DisplayName("실패: 재고가 부족하면 PRODUCT_OUT_OF_STOCK 예외가 발생한다")
        void decreaseStockByOrder_OutOfStock() {
            // given
            Long productId = 1L;
            Product product = createProduct(productId, 0);
            given(productSupport.findByIdForUpdate(productId)).willReturn(product);

            // when & then
            assertThatThrownBy(() -> productService.decreaseStockByOrder(Map.of(productId, 1L)))
                    .isInstanceOf(ProductException.class);
            assertThat(product.getStock()).isEqualTo(0);
            verify(productRepositoryPort, never()).save(product);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 상품이면 예외가 발생한다")
        void decreaseStockByOrder_ProductNotFound() {
            // given
            Long productId = 999L;
            given(productSupport.findByIdForUpdate(productId))
                    .willThrow(new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> productService.decreaseStockByOrder(Map.of(productId, 1L)))
                    .isInstanceOf(ProductException.class);
        }
    }

    @Nested
    @DisplayName("상품 수정 기능 테스트 (updateProduct)")
    class UpdateProductTests {
        private final Long PRODUCT_ID = 1L;
        private final Long SELLER_ID = 100L;

        @Test
        @DisplayName("상품 수정 성공: 상품의 기본 정보(이름, 설명, 가격)를 수정할 수 있다.")
        void updateProductInfo_Success() {
            // given
            Product product = createProduct(PRODUCT_ID, 10);
            ProductUpdateRequestDto requestDto = new ProductUpdateRequestDto(
                    "수정된 이름", "수정된 설명", 15000, null, null, null, "new-image-key"
            );

            given(productSupport.findById(PRODUCT_ID)).willReturn(product);

            // when
            ProductUpdateResult result = productService.updateProduct(PRODUCT_ID, SELLER_ID, requestDto);

            // then
            assertThat(product.getName()).isEqualTo("수정된 이름");
            assertThat(product.getDescription()).isEqualTo("수정된 설명");
            assertThat(product.getPrice()).isEqualTo(15000);
            verify(productRepositoryPort).save(product);
        }

        @Test
        @DisplayName("재고 수정 성공: CAS 검증을 통과하면 재고를 수정할 수 있다.")
        void updateStock_Success() {
            // given
            Product product = createProduct(PRODUCT_ID, 10);
            ProductUpdateRequestDto requestDto = new ProductUpdateRequestDto(
                    null, null, null, 50, 10, null, null
            );

            given(productSupport.findByIdForUpdate(PRODUCT_ID)).willReturn(product);

            // when
            productService.updateProduct(PRODUCT_ID, SELLER_ID, requestDto);

            // then
            assertThat(product.getStock()).isEqualTo(50);
            verify(productRepositoryPort).save(product);
        }

        @Test
        @DisplayName("재고 수정 실패: 예상 재고가 실제와 다르면 PRODUCT_STOCK_CHANGED 예외가 발생한다.")
        void updateStock_Fail_CasMismatch() {
            // given
            Product product = createProduct(PRODUCT_ID, 10);
            ProductUpdateRequestDto requestDto = new ProductUpdateRequestDto(
                    null, null, null, 50, 5, null, null
            );

            given(productSupport.findByIdForUpdate(PRODUCT_ID)).willReturn(product);

            // when & then
            assertThatThrownBy(() -> productService.updateProduct(PRODUCT_ID, SELLER_ID, requestDto))
                    .isInstanceOf(ProductException.class)
                    .hasMessageContaining(ProductErrorCode.PRODUCT_STOCK_CHANGED.getMessage());
        }

        @Test
        @DisplayName("상태 변경 성공: 진행 중인 펀딩이 없으면 INACTIVE로 변경할 수 있다.")
        void updateStatusToInactive_Success() {
            // given
            Product product = createProduct(PRODUCT_ID, 10);
            ProductUpdateRequestDto requestDto = new ProductUpdateRequestDto(
                    null, null, null, null, null, ProductStatus.INACTIVE, null
            );

            given(productSupport.findById(PRODUCT_ID)).willReturn(product);
            given(fundingClientPort.checkFundingExistsByProductId(PRODUCT_ID)).willReturn(false);

            // when
            productService.updateProduct(PRODUCT_ID, SELLER_ID, requestDto);

            // then
            assertThat(product.getStatus()).isEqualTo(ProductStatus.INACTIVE);
            verify(productRepositoryPort).save(product);
        }

        @Test
        @DisplayName("수정 실패: 삭제된 상품 수정 시 PRODUCT_ALREADY_DELETED 예외가 발생한다.")
        void updateProduct_Fail_AlreadyDeleted() {
            // given
            Product deletedProduct = Product.builder()
                    .id(PRODUCT_ID)
                    .sellerId(SELLER_ID)
                    .name("삭제된 상품")
                    .description("설명")
                    .price(10000)
                    .stock(10)
                    .status(ProductStatus.INACTIVE)
                    .deletedAt(LocalDateTime.of(2025, 1, 15, 10, 0))
                    .build();

            ProductUpdateRequestDto requestDto = new ProductUpdateRequestDto(
                    "수정된 이름", null, null, null, null, null, null
            );

            given(productSupport.findById(PRODUCT_ID)).willReturn(deletedProduct);

            // when & then
            assertThatThrownBy(() -> productService.updateProduct(PRODUCT_ID, SELLER_ID, requestDto))
                    .isInstanceOf(ProductException.class)
                    .hasMessageContaining(ProductErrorCode.PRODUCT_ALREADY_DELETED.getMessage());
            verify(productRepositoryPort, never()).save(deletedProduct);
        }

        @Test
        @DisplayName("상태 변경 실패: 진행 중인 펀딩이 있으면 INACTIVE로 변경 시 예외가 발생한다.")
        void updateStatusToInactive_Fail_DueToFunding() {
            // given
            Product product = createProduct(PRODUCT_ID, 10);
            ProductUpdateRequestDto requestDto = new ProductUpdateRequestDto(
                    null, null, null, null, null, ProductStatus.INACTIVE, null
            );

            given(productSupport.findById(PRODUCT_ID)).willReturn(product);
            given(fundingClientPort.checkFundingExistsByProductId(PRODUCT_ID)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> productService.updateProduct(PRODUCT_ID, SELLER_ID, requestDto))
                    .isInstanceOf(ProductException.class)
                    .hasMessageContaining(ProductErrorCode.CANNOT_STOP_SALE_DUE_TO_ACTIVE_FUNDING.getMessage());
        }
    }

    @Nested
    @DisplayName("나의 상품 검색 기능 테스트 (searchMyProducts)")
    class SearchMyProductsTests {
        private final Long SELLER_ID = 100L;

        @Test
        @DisplayName("deleted=false(기본값): deleted 필드가 false로 command에 전달된다")
        void searchMyProducts_deletedFalse() {
            // given
            MyProductSearchDto searchDto = new MyProductSearchDto();
            Product product = createProduct(1L, 10);

            given(productRepositoryPort.searchMyProducts(eq(SELLER_ID), any(MyProductSearchCommand.class)))
                    .willReturn(new PageImpl<>(List.of(product), PageRequest.of(0, 20), 1));

            // when
            PageResponse<MyProductResult> result = productService.searchMyProducts(SELLER_ID, searchDto);

            // then
            ArgumentCaptor<MyProductSearchCommand> captor = ArgumentCaptor.forClass(MyProductSearchCommand.class);
            verify(productRepositoryPort).searchMyProducts(eq(SELLER_ID), captor.capture());

            assertThat(captor.getValue().isDeleted()).isFalse();
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).deletedAt()).isNull();
        }

        @Test
        @DisplayName("deleted=true: deleted 필드가 true로 command에 전달된다")
        void searchMyProducts_deletedTrue() {
            // given
            MyProductSearchDto searchDto = new MyProductSearchDto();
            searchDto.setDeleted(true);

            Product deletedProduct = Product.builder()
                    .id(2L)
                    .sellerId(SELLER_ID)
                    .name("삭제된 상품")
                    .description("삭제된 상품 설명")
                    .price(10000)
                    .stock(0)
                    .status(ProductStatus.INACTIVE)
                    .deletedAt(LocalDateTime.of(2025, 1, 15, 10, 0))
                    .build();

            given(productRepositoryPort.searchMyProducts(eq(SELLER_ID), any(MyProductSearchCommand.class)))
                    .willReturn(new PageImpl<>(List.of(deletedProduct), PageRequest.of(0, 20), 1));

            // when
            PageResponse<MyProductResult> result = productService.searchMyProducts(SELLER_ID, searchDto);

            // then
            ArgumentCaptor<MyProductSearchCommand> captor = ArgumentCaptor.forClass(MyProductSearchCommand.class);
            verify(productRepositoryPort).searchMyProducts(eq(SELLER_ID), captor.capture());

            assertThat(captor.getValue().isDeleted()).isTrue();
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).deletedAt()).isNotNull();
        }

        @Test
        @DisplayName("deleted=true + status 필터: 두 필터가 모두 command에 전달된다")
        void searchMyProducts_deletedTrueWithStatusFilter() {
            // given
            MyProductSearchDto searchDto = new MyProductSearchDto();
            searchDto.setDeleted(true);
            searchDto.setStatus(ProductStatus.INACTIVE);

            given(productRepositoryPort.searchMyProducts(eq(SELLER_ID), any(MyProductSearchCommand.class)))
                    .willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

            // when
            productService.searchMyProducts(SELLER_ID, searchDto);

            // then
            ArgumentCaptor<MyProductSearchCommand> captor = ArgumentCaptor.forClass(MyProductSearchCommand.class);
            verify(productRepositoryPort).searchMyProducts(eq(SELLER_ID), captor.capture());

            assertThat(captor.getValue().isDeleted()).isTrue();
            assertThat(captor.getValue().getStatus()).isEqualTo(ProductStatus.INACTIVE);
        }
    }

    @Nested
    @DisplayName("만료된 삭제 상품 하드 삭제 기능 테스트 (hardDeleteExpiredProducts)")
    class HardDeleteExpiredProductTests {

        @Test
        @DisplayName("성공: 만료된 상품과 재고 이력이 함께 삭제된다")
        void hardDeleteExpiredProducts_Success() {
            // given
            List<Long> expiredProductIds = List.of(1L, 2L, 3L);
            given(productRepositoryPort.findExpiredDeletedProductIds(any(LocalDateTime.class)))
                    .willReturn(expiredProductIds);
            given(productStockHistoryRepositoryPort.deleteByProductIds(expiredProductIds))
                    .willReturn(5);
            given(productRepositoryPort.hardDeleteExpiredProducts(any(LocalDateTime.class)))
                    .willReturn(3);

            // when
            int deletedCount = productService.hardDeleteExpiredProducts();

            // then
            assertThat(deletedCount).isEqualTo(3);
            verify(productStockHistoryRepositoryPort).deleteByProductIds(expiredProductIds);
            verify(productRepositoryPort).hardDeleteExpiredProducts(any(LocalDateTime.class));
        }

        @Test
        @DisplayName("성공: 만료된 상품이 없으면 0건을 반환하고 삭제를 수행하지 않는다")
        void hardDeleteExpiredProducts_NoExpiredProducts() {
            // given
            given(productRepositoryPort.findExpiredDeletedProductIds(any(LocalDateTime.class)))
                    .willReturn(List.of());

            // when
            int deletedCount = productService.hardDeleteExpiredProducts();

            // then
            assertThat(deletedCount).isEqualTo(0);
            verify(productStockHistoryRepositoryPort, never()).deleteByProductIds(any());
            verify(productRepositoryPort, never()).hardDeleteExpiredProducts(any(LocalDateTime.class));
        }
    }

    private Product createProduct(Long id, int stock) {
        return Product.builder()
                .id(id)
                .sellerId(100L)
                .name("테스트 상품")
                .description("테스트 설명")
                .price(10000)
                .stock(stock)
                .status(ProductStatus.ACTIVE)
                .build();
    }
}
