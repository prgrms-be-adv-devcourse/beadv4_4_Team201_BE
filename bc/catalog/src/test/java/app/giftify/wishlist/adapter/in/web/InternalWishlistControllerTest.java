package app.giftify.wishlist.adapter.in.web;

import app.giftify.product.adapter.inbound.web.handler.ProductExceptionHandler;
import app.giftify.product.domain.exception.ProductNotActiveException;
import app.giftify.product.domain.exception.ProductOutOfStockException;
import app.giftify.shared.domain.vo.WishlistItemSnapshot;
import app.giftify.wishlist.adapter.in.web.controller.InternalWishlistController;
import app.giftify.wishlist.adapter.in.web.exceptionHandler.WishlistExceptionHandler;
import app.giftify.wishlist.application.port.in.GetWishlistItemSnapshotUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalWishlistController.class)
public class InternalWishlistControllerTest {

    private MockMvc mockMvc;

    @MockitoBean
    private GetWishlistItemSnapshotUseCase getWishlistItemSnapshotUseCase;

    @BeforeEach
    void setUp() {
        getWishlistItemSnapshotUseCase = Mockito.mock(GetWishlistItemSnapshotUseCase.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new InternalWishlistController(getWishlistItemSnapshotUseCase))
                .setControllerAdvice(new WishlistExceptionHandler(), new ProductExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("위시리스트 아이템 스냅샷 조회 성공")
    void getSnapshot_Success() throws Exception {
        // given
        List<Long> wishlistItemIds = List.of(1L, 2L);
        Map<Long, WishlistItemSnapshot> snapshots = Map.of(
                1L, new WishlistItemSnapshot(1L, 100L, "테스트 상품1", 10000, 5L, 77L, "products/100/crowcanyon-mug1.jpg"),
                2L, new WishlistItemSnapshot(2L, 101L, "테스트 상품2", 20000, 5L, 77L, "products/101/crowcanyon-mug2.jpg")
        );
        given(getWishlistItemSnapshotUseCase.getSnapshotList(wishlistItemIds)).willReturn(snapshots);

        // when & then
        mockMvc.perform(post("/api/internal/wishlist/items/snapshots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1, 2]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.1.originalWishlistItemId").value(1))
                .andExpect(jsonPath("$.1.productId").value(100))
                .andExpect(jsonPath("$.1.productName").value("테스트 상품1"))
                .andExpect(jsonPath("$.1.productPrice").value(10000))
                .andExpect(jsonPath("$.1.sellerId").value(5))
                .andExpect(jsonPath("$.1.wishlistOwnerId").value(77))
                .andExpect(jsonPath("$.2.originalWishlistItemId").value(2))
                .andExpect(jsonPath("$.2.productName").value("테스트 상품2"))
                .andExpect(jsonPath("$.2.imageKey").value("products/101/crowcanyon-mug2.jpg"));
    }

    @Test
    @DisplayName("존재하지 않는 위시리스트 아이템 스냅샷 조회 시 예외 발생")
    void getSnapshot_NotFound() throws Exception {
        // given
        List<Long> wishlistItemIds = List.of(999L);
        given(getWishlistItemSnapshotUseCase.getSnapshotList(wishlistItemIds))
                .willThrow(new app.giftify.wishlist.core.domain.exception.WishlistItemNotFoundException());

        // when & then
        mockMvc.perform(post("/api/internal/wishlist/items/snapshots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[999]"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("W102"))
                .andExpect(jsonPath("$.message").value("위시리스트 아이템을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("스냅샷 조회 실패 - 상품 판매 상태 아님")
    void getSnapshot_ProductNotActive() throws Exception {
        // given
        List<Long> wishlistItemIds = List.of(1L, 2L, 3L);
        given(getWishlistItemSnapshotUseCase.getSnapshotList(wishlistItemIds))
                .willThrow(new ProductNotActiveException(101L));

        // when & then
        mockMvc.perform(post("/api/internal/wishlist/items/snapshots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1, 2, 3]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("P207"))
                .andExpect(jsonPath("$.message").value("판매 중인 상품이 아닙니다. (productId: 101)"));
    }

    @Test
    @DisplayName("스냅샷 조회 실패 - 상품 재고 없음")
    void getSnapshot_ProductOutOfStock() throws Exception {
        // given
        List<Long> wishlistItemIds = List.of(1L, 2L, 3L);
        given(getWishlistItemSnapshotUseCase.getSnapshotList(wishlistItemIds))
                .willThrow(new ProductOutOfStockException(101L));

        // when & then
        mockMvc.perform(post("/api/internal/wishlist/items/snapshots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1, 2, 3]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("P201"))
                .andExpect(jsonPath("$.message").value("상품 재고가 부족합니다. (productId: 101)"));
    }
}
