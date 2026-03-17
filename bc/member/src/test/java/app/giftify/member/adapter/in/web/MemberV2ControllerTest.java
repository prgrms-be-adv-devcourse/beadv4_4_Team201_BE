package app.giftify.member.adapter.in.web;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.datatype.jsr310.JavaTimeModule;

import app.giftify.member.application.port.in.GetMemberUseCase;
import app.giftify.member.application.port.in.RegisterMemberUseCase;
import app.giftify.member.application.port.in.UpdateMemberUseCase;
import app.giftify.member.application.port.in.WithdrawMemberUseCase;
import app.giftify.member.domain.exception.MemberNotFoundException;
import app.giftify.member.domain.member.Member;
import app.giftify.member.domain.member.MemberStatus;
import app.giftify.security.common.CurrentAuthSub;
import app.giftify.security.common.CurrentMemberId;

@WebMvcTest(MemberV2Controller.class)
class MemberV2ControllerTest {

	/**
	 * 테스트용 예외 핸들러.
	 * MemberExceptionHandler가 MemberController에만 적용되므로 별도 정의.
	 */
	@org.springframework.web.bind.annotation.RestControllerAdvice
	static class TestExceptionHandler {
		@org.springframework.web.bind.annotation.ExceptionHandler(MemberNotFoundException.class)
		public org.springframework.http.ResponseEntity<?> handleNotFound(MemberNotFoundException e) {
			return org.springframework.http.ResponseEntity.status(404).build();
		}
	}

	private MockMvc mockMvc;

	@MockitoBean
	private GetMemberUseCase getMemberUseCase;

	@MockitoBean
	private RegisterMemberUseCase registerMemberUseCase;

	@MockitoBean
	private UpdateMemberUseCase updateMemberUseCase;

	@MockitoBean
	private WithdrawMemberUseCase withdrawMemberUseCase;

	private static final String AUTH_SUB = "auth0|12345";
	private static final Long MEMBER_ID = 1L;

	@BeforeEach
	void setUp() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		mockMvc = MockMvcBuilders
			.standaloneSetup(new MemberV2Controller(getMemberUseCase, registerMemberUseCase, updateMemberUseCase, withdrawMemberUseCase))
			.setControllerAdvice(new TestExceptionHandler())
			.setMessageConverters(
				new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(mapper))
			.setCustomArgumentResolvers(
				// @CurrentAuthSub resolver
				new HandlerMethodArgumentResolver() {
					@Override
					public boolean supportsParameter(MethodParameter parameter) {
						return parameter.hasParameterAnnotation(CurrentAuthSub.class);
					}

					@Override
					public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
						NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
						return AUTH_SUB;
					}
				},
				// @CurrentMemberId resolver
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

	@Nested
	@DisplayName("GET /api/v2/members/me")
	class Given_GetMe {

		@Nested
		@DisplayName("When 회원이 존재하는 경우")
		class When_MemberExists {

			@Test
			@DisplayName("Then MemberResponse DTO로 반환하며 민감 정보 제외")
			void Then_ReturnsMemberResponseWithoutSensitiveInfo() throws Exception {
				// given
				Member member = Member.builder()
					.id(1L)
					.email("test@example.com")
					.nickname("테스터")
					.birthday(LocalDate.of(1990, 1, 1))
					.address("서울시 강남구")
					.phoneNum("010-1234-5678")
					.name("홍길동")
					.status(MemberStatus.ACTIVE)
					.authSub(AUTH_SUB)
					.build();

				given(getMemberUseCase.getMemberById(MEMBER_ID)).willReturn(Optional.of(member));

				// when & then
				mockMvc.perform(get("/api/v2/members/me"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.result").value("SUCCESS"))
					.andExpect(jsonPath("$.data.id").value(1))
					.andExpect(jsonPath("$.data.email").value("test@example.com"))
					.andExpect(jsonPath("$.data.nickname").value("테스터"))
					.andExpect(jsonPath("$.data.birthday").value("1990-01-01"))
					.andExpect(jsonPath("$.data.address").value("서울시 강남구"))
					.andExpect(jsonPath("$.data.phoneNum").value("010-1234-5678"))
					.andExpect(jsonPath("$.data.name").value("홍길동"))
					.andExpect(jsonPath("$.data.status").value("ACTIVE"));
			}
		}

		@Nested
		@DisplayName("When 회원이 존재하지 않는 경우")
		class When_MemberNotExists {

			@Test
			@DisplayName("Then 404 에러 반환")
			void Then_Returns404() throws Exception {
				// given
				given(getMemberUseCase.getMemberById(MEMBER_ID)).willReturn(Optional.empty());

				// when & then
				mockMvc.perform(get("/api/v2/members/me"))
					.andExpect(status().isNotFound());
			}
		}
	}

	@Nested
	@DisplayName("PATCH /api/v2/members/me")
	class Given_UpdateMe {

		@Nested
		@DisplayName("When 활성 회원이 정보 수정 요청")
		class When_ActiveMemberUpdates {

			@Test
			@DisplayName("Then 수정된 MemberResponse 반환")
			void Then_ReturnsUpdatedMemberResponse() throws Exception {
				// given
				Member existingMember = Member.builder()
					.id(1L)
					.status(MemberStatus.ACTIVE)
					.authSub(AUTH_SUB)
					.build();

				Member updatedMember = Member.builder()
					.id(1L)
					.email("test@example.com")
					.nickname("새닉네임")
					.address("부산시 해운대구")
					.phoneNum("010-9999-8888")
					.name("김철수")
					.status(MemberStatus.ACTIVE)
					.authSub(AUTH_SUB)
					.build();

				given(getMemberUseCase.getMemberById(MEMBER_ID)).willReturn(Optional.of(existingMember));
				given(updateMemberUseCase.updateMember(any())).willReturn(updatedMember);

				String requestBody = """
					{
						"nickname": "새닉네임",
						"address": "부산시 해운대구",
						"phoneNum": "010-9999-8888",
						"name": "김철수"
					}
					""";

				// when & then
				mockMvc.perform(patch("/api/v2/members/me")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.result").value("SUCCESS"))
					.andExpect(jsonPath("$.data.nickname").value("새닉네임"))
					.andExpect(jsonPath("$.data.address").value("부산시 해운대구"))
					.andExpect(jsonPath("$.data.phoneNum").value("010-9999-8888"))
					.andExpect(jsonPath("$.data.name").value("김철수"));

				verify(updateMemberUseCase).updateMember(any(UpdateMemberUseCase.UpdateCommand.class));
			}
		}

		@Nested
		@DisplayName("When 탈퇴한 회원이 수정 요청")
		class When_WithdrawnMemberUpdates {

			@Test
			@DisplayName("Then 403 Forbidden 반환")
			void Then_Returns403() throws Exception {
				// given
				Member withdrawnMember = Member.builder()
					.id(1L)
					.status(MemberStatus.WITHDRAWN)
					.authSub(AUTH_SUB)
					.build();

				given(getMemberUseCase.getMemberById(MEMBER_ID)).willReturn(Optional.of(withdrawnMember));

				String requestBody = """
					{
						"nickname": "새닉네임"
					}
					""";

				// when & then
				mockMvc.perform(patch("/api/v2/members/me")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
					.andExpect(status().isForbidden());

				verify(updateMemberUseCase, never()).updateMember(any());
			}
		}

		@Nested
		@DisplayName("When 부분 필드만 수정 요청")
		class When_PartialUpdate {

			@Test
			@DisplayName("Then 요청한 필드만 수정")
			void Then_UpdatesOnlyRequestedFields() throws Exception {
				// given
				Member existingMember = Member.builder()
					.id(1L)
					.nickname("기존닉네임")
					.address("기존주소")
					.status(MemberStatus.ACTIVE)
					.authSub(AUTH_SUB)
					.build();

				Member updatedMember = Member.builder()
					.id(1L)
					.nickname("새닉네임")
					.address("기존주소")
					.status(MemberStatus.ACTIVE)
					.authSub(AUTH_SUB)
					.build();

				given(getMemberUseCase.getMemberById(MEMBER_ID)).willReturn(Optional.of(existingMember));
				given(updateMemberUseCase.updateMember(any())).willReturn(updatedMember);

				String requestBody = """
					{
						"nickname": "새닉네임"
					}
					""";

				// when & then
				mockMvc.perform(patch("/api/v2/members/me")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.result").value("SUCCESS"))
					.andExpect(jsonPath("$.data.nickname").value("새닉네임"));
			}
		}
	}

	@Nested
	@DisplayName("DELETE /api/v2/members/me")
	class Given_Withdraw {

		@Nested
		@DisplayName("When 회원 탈퇴 요청")
		class When_WithdrawRequest {

			@Test
			@DisplayName("Then 204 No Content 반환")
			void Then_Returns204() throws Exception {
				// given
				Member member = Member.builder()
					.id(MEMBER_ID)
					.authSub(AUTH_SUB)
					.build();

				given(getMemberUseCase.getMemberById(MEMBER_ID)).willReturn(Optional.of(member));
				willDoNothing().given(withdrawMemberUseCase).withdrawMember(AUTH_SUB);

				// when & then
				mockMvc.perform(delete("/api/v2/members/me"))
					.andExpect(status().isNoContent());

				verify(withdrawMemberUseCase).withdrawMember(AUTH_SUB);
			}
		}

		@Nested
		@DisplayName("When 존재하지 않는 회원 탈퇴 요청")
		class When_MemberNotFound {

			@Test
			@DisplayName("Then 404 Not Found 반환")
			void Then_Returns404() throws Exception {
				// given
				given(getMemberUseCase.getMemberById(MEMBER_ID)).willReturn(Optional.empty());

				// when & then
				mockMvc.perform(delete("/api/v2/members/me"))
					.andExpect(status().isNotFound());
			}
		}
	}
}
