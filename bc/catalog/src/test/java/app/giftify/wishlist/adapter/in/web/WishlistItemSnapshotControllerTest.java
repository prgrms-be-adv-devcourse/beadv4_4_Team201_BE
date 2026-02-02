package app.giftify.wishlist.adapter.in.web;

import app.giftify.security.common.CurrentMemberId;
import app.giftify.shared.domain.vo.WishlistItemSnapshot;
import app.giftify.wishlist.adapter.in.web.controller.WishlistItemSnapshotController;
import app.giftify.wishlist.adapter.in.web.exceptionHandler.WishlistExceptionHandler;
import app.giftify.wishlist.application.port.in.GetWishlistItemSnapshotUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.MethodParameter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WishlistItemSnapshotControllerTest.class)
public class WishlistItemSnapshotControllerTest {

    private MockMvc mockMvc;

    private GetWishlistItemSnapshotUseCase getWishlistItemSnapshotUseCase;

    private static final Long MEMBER_ID = 10L;

    @BeforeEach
    void setUp() {
        getWishlistItemSnapshotUseCase = Mockito.mock(GetWishlistItemSnapshotUseCase.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new WishlistItemSnapshotController(getWishlistItemSnapshotUseCase))
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
    @DisplayName("위시리스트 아이템 스냅샷 조회 성공")
    void getSnapshot_Success() throws Exception {
        // given
        Long wishlistItemId = 1L;
        WishlistItemSnapshot snapshot = new WishlistItemSnapshot(
                wishlistItemId,
                100L,
                "테스트 상품",
                10000,
                5L
        );
        given(getWishlistItemSnapshotUseCase.getSnapshot(wishlistItemId)).willReturn(snapshot);

        // when & then
        mockMvc.perform(get("/api/internal/wishlist/items/{wishlistItemId}/snapshot", wishlistItemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalWishlistItemId").value(wishlistItemId))
                .andExpect(jsonPath("$.productId").value(100))
                .andExpect(jsonPath("$.productName").value("테스트 상품"))
                .andExpect(jsonPath("$.productPrice").value(10000))
                .andExpect(jsonPath("$.sellerId").value(5));
    }

    @Test
    @DisplayName("존재하지 않는 위시리스트 아이템 스냅샷 조회 시 예외 발생")
    void getSnapshot_NotFound() throws Exception {
        // given
        Long wishlistItemId = 999L;
        given(getWishlistItemSnapshotUseCase.getSnapshot(wishlistItemId))
                .willThrow(new app.giftify.wishlist.core.domain.exception.WishlistItemNotFoundException());

        // when & then
        mockMvc.perform(get("/api/internal/wishlist/items/{wishlistItemId}/snapshot", wishlistItemId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("W102"));
    }
}
