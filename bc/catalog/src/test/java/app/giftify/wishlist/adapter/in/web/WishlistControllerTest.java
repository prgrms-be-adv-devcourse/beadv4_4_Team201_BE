package app.giftify.wishlist.adapter.in.web;

import app.giftify.security.common.CurrentMemberId;
import app.giftify.wishlist.adapter.in.web.controller.WishlistController;
import app.giftify.wishlist.adapter.in.web.exceptionHandler.WishlistExceptionHandler;
import app.giftify.wishlist.adapter.in.web.requestDto.UpdateWishlistSettingsRequest;
import app.giftify.wishlist.application.port.in.GetWishlistUseCase;
import app.giftify.wishlist.application.port.in.UpdateWishlistSettingsUseCase;
import app.giftify.wishlist.core.domain.Visibility;
import app.giftify.wishlist.core.domain.Wishlist;
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

import static org.mockito.ArgumentMatchers.any;
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
    @DisplayName("내 위시리스트 조회 성공")
    void getMyInfo_Success() throws Exception {
        // given
        Wishlist wishlist = Wishlist.builder()
                .memberId(10L)
                .visibility(Visibility.PUBLIC)
                .build();
        given(getWishlistUseCase.getOrCreateWishlistByMemberId(10L)).willReturn(wishlist);

        // when & then
        mockMvc.perform(get("/api/wishlist/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visibility").value("PUBLIC"));
    }

    @Test
    @DisplayName("위시리스트 설정 변경 성공")
    void updateSettings_Success() throws Exception {
        // given
        UpdateWishlistSettingsRequest request = new UpdateWishlistSettingsRequest("PRIVATE");
        Wishlist existingWishlist = Wishlist.builder()
                .memberId(MEMBER_ID)
                .visibility(Visibility.PUBLIC)
                .build();
        Wishlist updatedWishlist = Wishlist.builder()
                .memberId(MEMBER_ID)
                .visibility(Visibility.PRIVATE)
                .build();

        given(getWishlistUseCase.getOrCreateWishlistByMemberId(MEMBER_ID)).willReturn(existingWishlist);
        given(updateWishlistSettingsUseCase.updateSettings(any())).willReturn(updatedWishlist);

        // when & then
        mockMvc.perform(patch("/api/wishlist/me/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visibility").value("PRIVATE"));
    }
}
