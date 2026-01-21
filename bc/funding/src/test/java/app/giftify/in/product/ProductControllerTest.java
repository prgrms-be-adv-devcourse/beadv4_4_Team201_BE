package app.giftify.in.product;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import app.giftify.app.product.ProductFacade;
import app.giftify.security.common.CurrentMemberId;
import app.giftify.shared.api.paging.PageResponse;

/**
 * ProductController 테스트.
 *
 * <p>이 테스트는 @CurrentMemberId 어노테이션을 통한 memberId 주입을 검증합니다.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController - @CurrentMemberId 주입 테스트")
class ProductControllerTest {

	private MockMvc mockMvc;
	private ObjectMapper objectMapper;

	@Mock
	private ProductFacade productFacade;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
	}

	private MockMvc createMockMvc(Long memberId) {
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setObjectMapper(objectMapper);

		return MockMvcBuilders
			.standaloneSetup(new ProductController(productFacade))
			.setMessageConverters(converter)
			.setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
				@Override
				public boolean supportsParameter(MethodParameter parameter) {
					return parameter.hasParameterAnnotation(CurrentMemberId.class);
				}

				@Override
				public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
					NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
					return memberId;
				}
			})
			.build();
	}

	@Nested
	@DisplayName("상품 등록 - @CurrentMemberId 주입")
	class CreateProduct {

		@Test
		@DisplayName("@CurrentMemberId로 memberId가 FundingMember에 올바르게 전달된다")
		void memberIdIsInjectedToFundingMember() throws Exception {
			// given
			Long expectedMemberId = 123L;
			mockMvc = createMockMvc(expectedMemberId);

			ProductCreateRequestDto request = new ProductCreateRequestDto(
				"테스트 상품", "상품 설명입니다", 10000, 100
			);

			ProductDto response = new ProductDto(
				1L, "판매자", "테스트 상품", "상품 설명입니다", 10000, LocalDateTime.now()
			);

			given(productFacade.createProduct(any(), any())).willReturn(response);

			// when
			mockMvc.perform(post("/api/products")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated());

			// then - verify memberId was correctly passed to FundingMember
			then(productFacade).should().createProduct(
				argThat(seller -> seller.getId().equals(expectedMemberId)),
				any()
			);
		}

		@Test
		@DisplayName("다른 memberId 값도 정확히 전달된다")
		void differentMemberIdIsAlsoInjected() throws Exception {
			// given
			Long expectedMemberId = 456L;
			mockMvc = createMockMvc(expectedMemberId);

			ProductCreateRequestDto request = new ProductCreateRequestDto(
				"테스트 상품", "상품 설명입니다", 10000, 100
			);

			ProductDto response = new ProductDto(
				1L, "판매자", "테스트 상품", "상품 설명입니다", 10000, LocalDateTime.now()
			);

			given(productFacade.createProduct(any(), any())).willReturn(response);

			// when
			mockMvc.perform(post("/api/products")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated());

			// then
			then(productFacade).should().createProduct(
				argThat(seller -> seller.getId().equals(expectedMemberId)),
				any()
			);
		}
	}


	@Nested
	@DisplayName("내 상품 조회 - @CurrentMemberId 주입")
	class SearchMyProducts {

		@Test
		@DisplayName("@CurrentMemberId로 sellerId가 정확히 전달된다")
		void sellerIdIsPassedToSearchMyProducts() throws Exception {
			// given
			Long sellerId = 321L;
			mockMvc = createMockMvc(sellerId);

			PageResponse<ProductDto> emptyResponse = PageResponse.of(List.of(), 0, 20, 0);
			given(productFacade.searchMyProducts(eq(sellerId), any())).willReturn(emptyResponse);

			// when
			mockMvc.perform(get("/api/products/my"))
				.andExpect(status().isOk());

			// then - verify sellerId was correctly passed
			then(productFacade).should().searchMyProducts(eq(sellerId), any());
		}
	}
}
