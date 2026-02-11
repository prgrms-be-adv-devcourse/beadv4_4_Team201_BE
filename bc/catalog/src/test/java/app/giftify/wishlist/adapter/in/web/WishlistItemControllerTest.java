package app.giftify.wishlist.adapter.in.web;

import app.giftify.security.common.CurrentMemberId;
import app.giftify.wishlist.adapter.in.web.controller.WishlistItemController;
import app.giftify.wishlist.adapter.in.web.exceptionHandler.WishlistExceptionHandler;
import app.giftify.wishlist.application.port.in.AddWishlistItemUseCase;
import app.giftify.wishlist.application.port.in.GetWishlistItemUseCase;
import app.giftify.wishlist.application.port.in.RemoveWishlistItemUseCase;
import app.giftify.wishlist.core.domain.WishlistItem;
import app.giftify.wishlist.core.domain.WishlistItemStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.MethodParameter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WishlistItemController.class)
class WishlistItemControllerTest {

    private MockMvc mockMvc;

    @MockitoBean
    private AddWishlistItemUseCase addWishlistItemUseCase;

    @MockitoBean
    private GetWishlistItemUseCase getWishlistItemUseCase;

    @MockitoBean
    private RemoveWishlistItemUseCase removeWishlistItemUseCase;

    private static final Long MEMBER_ID = 10L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new WishlistItemController(addWishlistItemUseCase, getWishlistItemUseCase, removeWishlistItemUseCase))
                .setControllerAdvice(new WishlistExceptionHandler())
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.hasParameterAnnotation(CurrentMemberId.class);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                        return MEMBER_ID;
                    }
                })
                .build();
    }

    @Test
    @DisplayName("내 위시리스트 아이템 전체 조회")
    void getAllProducts_Success() throws Exception {
        // given
        WishlistItem item = WishlistItem.builder()
                .id(1L)
                .wishlistId(1L)
                .productId(100L)
                .wishlistItemStatus(WishlistItemStatus.PENDING)
                .build();
        given(getWishlistItemUseCase.getWishlistItems(MEMBER_ID)).willReturn(List.of(item));

        // when & then
        mockMvc.perform(get("/api/v2/wishlists/items/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(100));
    }

    @Test
    @DisplayName("내 위시리스트 아이템 전체 조회 - 빈 목록")
    void getAllProducts_EmptyList() throws Exception {
        // given
        given(getWishlistItemUseCase.getWishlistItems(MEMBER_ID)).willReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/v2/wishlists/items/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("특정 상품의 위시리스트 포함 여부 확인")
    void isExistProduct_Success() throws Exception {
        // given
        given(getWishlistItemUseCase.isItemExists(any(), any())).willReturn(true);

        // when & then
        mockMvc.perform(get("/api/v2/wishlists/items/me/check")
                        .queryParam("productId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @DisplayName("위시리스트 아이템 삭제 성공")
    void removeProduct_Success() throws Exception {
        // given
        // any() matcher to avoid any subtle type mismatch during initial debug

        // when & then
        mockMvc.perform(delete("/api/v2/wishlists/items/remove")
                        .queryParam("productId", "100"))
                .andExpect(status().isNoContent());

        verify(removeWishlistItemUseCase).removeWishlistItem(any());
    }

    @Test
    @DisplayName("위시리스트 아이템 추가 성공")
    void addProduct_Success() throws Exception {
        // given
        Long productId = 100L;

        WishlistItem item = WishlistItem.builder()
                .id(1L)
                .wishlistId(10L)
                .productId(productId)
                .wishlistItemStatus(WishlistItemStatus.PENDING)
                .build();
        given(addWishlistItemUseCase.addWishlistItem(anyLong(), any())).willReturn(item);

        // when & then
        mockMvc.perform(post("/api/v2/wishlists/items/add")
                        .queryParam("productId", String.valueOf(productId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.productId").value(100))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(addWishlistItemUseCase).addWishlistItem(anyLong(), any());
    }

    @Test
    @DisplayName("판매 중이 아닌 상품 추가 시 실패")
    void addProduct_Fail_ProductNotOnSale() throws Exception {
        // given
        Long productId = 100L;
        given(addWishlistItemUseCase.addWishlistItem(anyLong(), any()))
                .willThrow(new app.giftify.wishlist.core.domain.exception.ProductNotOnSaleException(productId));

        // when & then
        mockMvc.perform(post("/api/v2/wishlists/items/add")
                        .queryParam("productId", String.valueOf(productId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("W103"))
                .andExpect(jsonPath("$.message").value("현재 판매 중이지 않은 상품입니다. (productId: 100)"));
    }
}
