package app.giftify.wishlist.adapter.in.web;

import app.giftify.product.domain.ProductCategory;
import app.giftify.security.common.CurrentMemberId;
import app.giftify.shared.api.paging.Page;
import app.giftify.wishlist.adapter.in.web.controller.WishlistController;
import app.giftify.wishlist.adapter.in.web.exceptionHandler.WishlistExceptionHandler;
import app.giftify.wishlist.adapter.in.web.requestDto.UpdateWishlistSettingsRequest;
import app.giftify.wishlist.application.port.in.GetWishlistUseCase;
import app.giftify.wishlist.application.port.in.UpdateWishlistSettingsUseCase;
import app.giftify.wishlist.application.port.in.WishlistItemDetail;
import app.giftify.wishlist.application.port.in.WishlistOverview;
import app.giftify.wishlist.core.domain.Visibility;
import app.giftify.wishlist.core.domain.Wishlist;
import app.giftify.wishlist.core.domain.WishlistItem;
import app.giftify.wishlist.core.domain.WishlistItemStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WishlistController.class)
class WishlistControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GetWishlistUseCase getWishlistUseCase;

    @MockitoBean
    private UpdateWishlistSettingsUseCase updateWishlistSettingsUseCase;

    private static final Long MEMBER_ID = 10L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new WishlistController(updateWishlistSettingsUseCase, getWishlistUseCase))
                .setControllerAdvice(new WishlistExceptionHandler())
                .setCustomArgumentResolvers(
                        new HandlerMethodArgumentResolver() {
                            @Override
                            public boolean supportsParameter(MethodParameter parameter) {
                                return parameter.hasParameterAnnotation(CurrentMemberId.class);
                            }

                            @Override
                            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                                return MEMBER_ID;
                            }
                        }
                )
                .build();
    }

    @Test
    @DisplayName("내 위시리스트 조회 성공 - 위시리스트 정보 + 닉네임 + 아이템 목록")
    void getMyWishlist_Success() throws Exception {
        // given
        Wishlist wishlist = Wishlist.builder()
                .id(1L)
                .memberId(MEMBER_ID)
                .visibility(Visibility.PUBLIC)
                .build();

        WishlistItem item = WishlistItem.builder()
                .id(1L)
                .wishlistId(1L)
                .productId(100L)
                .wishlistItemStatus(WishlistItemStatus.PENDING)
                .build();
        WishlistItemDetail detail = new WishlistItemDetail(item, "테스트 상품", 10000, "img.jpg", false, true, "판매자닉네임", ProductCategory.BEAUTY);

        WishlistOverview overview = new WishlistOverview(
                wishlist, "테스트유저", Page.of(List.of(detail), 1));

        given(getWishlistUseCase.getMyWishlistOverview(eq(MEMBER_ID), any())).willReturn(overview);

        // when & then
        mockMvc.perform(get("/api/v2/wishlists/me")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visibility").value("PUBLIC"))
                .andExpect(jsonPath("$.nickname").value("테스트유저"))
                .andExpect(jsonPath("$.items.content").isArray())
                .andExpect(jsonPath("$.items.content[0].productId").value(100))
                .andExpect(jsonPath("$.items.content[0].productName").value("테스트 상품"))
                .andExpect(jsonPath("$.items.content[0].category").value("BEAUTY"));
    }

    @Test
    @DisplayName("내 위시리스트 조회 - 아이템이 없을 때 빈 목록 반환")
    void getMyWishlist_EmptyItems() throws Exception {
        // given
        Wishlist wishlist = Wishlist.builder()
                .id(1L)
                .memberId(MEMBER_ID)
                .visibility(Visibility.PUBLIC)
                .build();

        WishlistOverview overview = new WishlistOverview(
                wishlist, "테스트유저", Page.of(Collections.emptyList(), 0));

        given(getWishlistUseCase.getMyWishlistOverview(eq(MEMBER_ID), any())).willReturn(overview);

        // when & then
        mockMvc.perform(get("/api/v2/wishlists/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visibility").value("PUBLIC"))
                .andExpect(jsonPath("$.items.content").isArray())
                .andExpect(jsonPath("$.items.content").isEmpty());
    }

    @Test
    @DisplayName("위시리스트 설정 변경 성공")
    void updateSettings_Success() throws Exception {
        // given
        UpdateWishlistSettingsRequest request = new UpdateWishlistSettingsRequest("PRIVATE");
        Wishlist updatedWishlist = Wishlist.builder()
                .memberId(MEMBER_ID)
                .visibility(Visibility.PRIVATE)
                .build();

        given(updateWishlistSettingsUseCase.updateSettings(any())).willReturn(updatedWishlist);

        // when & then
        mockMvc.perform(patch("/api/v2/wishlists/me/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visibility").value("PRIVATE"));
    }
}
