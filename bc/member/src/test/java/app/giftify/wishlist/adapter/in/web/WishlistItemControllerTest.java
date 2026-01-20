package app.giftify.wishlist.adapter.in.web;

import app.giftify.member.adapter.in.web.exceptionHandler.MemberExceptionHandler;
import app.giftify.security.common.context.AuthenticatedMember;
import app.giftify.wishlist.adapter.in.web.controller.WishlistItemController;
import app.giftify.wishlist.application.port.in.AddWishlistItemUseCase;
import app.giftify.wishlist.application.port.in.GetWishlistItemUseCase;
import app.giftify.wishlist.application.port.in.RemoveWishlistItemUseCase;
import app.giftify.wishlist.core.domain.ItemStatus;
import app.giftify.wishlist.core.domain.WishlistItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.MethodParameter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WishlistItemController.class)
class WishlistItemControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private AddWishlistItemUseCase addWishlistItemUseCase;

    @MockBean
    private GetWishlistItemUseCase getWishlistItemUseCase;

    @MockBean
    private RemoveWishlistItemUseCase removeWishlistItemUseCase;

    private static final String AUTH_SUB = "auth0|12345";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new WishlistItemController(addWishlistItemUseCase, getWishlistItemUseCase, removeWishlistItemUseCase))
                .setControllerAdvice(new MemberExceptionHandler())
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.hasParameterAnnotation(AuthenticatedMember.class) && parameter.getParameterType().equals(String.class);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                        return AUTH_SUB;
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
                .authSub(AUTH_SUB)
                .productId(100L)
                .itemStatus(ItemStatus.ACTIVE)
                .build();
        given(getWishlistItemUseCase.getWishlistItems(AUTH_SUB)).willReturn(List.of(item));

        // when & then
        mockMvc.perform(get("/api/wishlist/item/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(100));
    }

    @Test
    @DisplayName("특정 상품의 위시리스트 포함 여부 확인")
    void isExistProduct_Success() throws Exception {
        // given
        given(getWishlistItemUseCase.isItemExists(any(), any())).willReturn(true);

        // when & then
        mockMvc.perform(get("/api/wishlist/item/me/check")
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
        mockMvc.perform(delete("/api/wishlist/item/remove")
                        .queryParam("productId", "100"))
                .andExpect(status().isNoContent());

        verify(removeWishlistItemUseCase).removeWishlistItem(any());
    }
}
