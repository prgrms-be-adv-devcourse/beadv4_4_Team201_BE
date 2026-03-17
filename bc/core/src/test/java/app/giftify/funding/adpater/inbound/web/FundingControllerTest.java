package app.giftify.funding.adpater.inbound.web;

import app.giftify.funding.adpater.inbound.dto.FundingCompleteResponseDto;
import app.giftify.funding.application.FundingFacade;
import app.giftify.shared.domain.type.FundingStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("FundingController 테스트")
class FundingControllerTest {

	private MockMvc mockMvc;

	@Mock
	private FundingFacade fundingFacade;

	private static final Long FUNDING_ID = 1L;

	@BeforeEach
	void setUp() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		FundingController controller = new FundingController(fundingFacade);

		mockMvc = MockMvcBuilders
			.standaloneSetup(controller)
			.setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
			.build();
	}

	private FundingCompleteResponseDto dummyCompleteResponse(FundingStatus status) {
		return new FundingCompleteResponseDto(FUNDING_ID, 10L, status, LocalDateTime.of(2025, 6, 1, 12, 0));
	}

	@Nested
	@DisplayName("PUT /api/v2/fundings/{id}/close")
	class CloseFunding {

		@Test
		@DisplayName("closeFunding 메서드에 @PreAuthorize ADMIN 어노테이션이 존재한다")
		void hasPreAuthorizeAdmin() throws NoSuchMethodException {
			Method method = FundingController.class.getDeclaredMethod("closeFunding", Long.class);
			PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

			assertThat(annotation).isNotNull();
			assertThat(annotation.value()).contains("ADMIN");
		}

		@Test
		@DisplayName("펀딩 종료 성공 시 200 OK 반환")
		void success_Returns200() throws Exception {
			given(fundingFacade.closeFunding(FUNDING_ID))
				.willReturn(dummyCompleteResponse(FundingStatus.CLOSED));

			mockMvc.perform(put("/api/v2/fundings/{id}/close", FUNDING_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.result").value("SUCCESS"))
				.andExpect(jsonPath("$.data.fundingId").value(FUNDING_ID))
				.andExpect(jsonPath("$.data.status").value("CLOSED"));
		}
	}

	@Nested
	@DisplayName("PUT /api/v2/fundings/{id}/expire")
	class ExpireFunding {

		@Test
		@DisplayName("expireFunding 메서드에 @PreAuthorize ADMIN 어노테이션이 존재한다")
		void hasPreAuthorizeAdmin() throws NoSuchMethodException {
			Method method = FundingController.class.getDeclaredMethod("expireFunding", Long.class);
			PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

			assertThat(annotation).isNotNull();
			assertThat(annotation.value()).contains("ADMIN");
		}

		@Test
		@DisplayName("펀딩 만료 처리 성공 시 200 OK 반환")
		void success_Returns200() throws Exception {
			given(fundingFacade.expireFunding(FUNDING_ID))
				.willReturn(dummyCompleteResponse(FundingStatus.EXPIRED));

			mockMvc.perform(put("/api/v2/fundings/{id}/expire", FUNDING_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.result").value("SUCCESS"))
				.andExpect(jsonPath("$.data.fundingId").value(FUNDING_ID))
				.andExpect(jsonPath("$.data.status").value("EXPIRED"));
		}
	}
}
