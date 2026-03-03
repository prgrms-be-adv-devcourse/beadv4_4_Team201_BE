package app.giftify.friendship.adapter.in.web;

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
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import app.giftify.friendship.application.port.in.AcceptFriendRequestUseCase;
import app.giftify.friendship.application.port.in.FriendInfo;
import app.giftify.friendship.application.port.in.FriendRequestInfo;
import app.giftify.friendship.application.port.in.GetFriendListUseCase;
import app.giftify.friendship.application.port.in.GetFriendRequestsUseCase;
import app.giftify.friendship.application.port.in.RejectFriendRequestUseCase;
import app.giftify.friendship.application.port.in.RemoveFriendUseCase;
import app.giftify.friendship.application.port.in.SendFriendRequestUseCase;
import app.giftify.friendship.domain.Friendship;
import app.giftify.friendship.domain.FriendshipStatus;
import app.giftify.friendship.domain.exception.FriendshipErrorCode;
import app.giftify.friendship.domain.exception.FriendshipException;
import app.giftify.security.common.CurrentMemberId;

@ExtendWith(MockitoExtension.class)
class FriendshipV2ControllerTest {

	private MockMvc mockMvc;

	@Mock
	private SendFriendRequestUseCase sendFriendRequestUseCase;
	@Mock
	private AcceptFriendRequestUseCase acceptFriendRequestUseCase;
	@Mock
	private RejectFriendRequestUseCase rejectFriendRequestUseCase;
	@Mock
	private RemoveFriendUseCase removeFriendUseCase;
	@Mock
	private GetFriendListUseCase getFriendListUseCase;
	@Mock
	private GetFriendRequestsUseCase getFriendRequestsUseCase;

	private static final Long MEMBER_ID = 1L;
	private static final Long RECEIVER_ID = 2L;
	private static final Long FRIENDSHIP_ID = 10L;

	@BeforeEach
	void setUp() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		FriendshipV2Controller controller = new FriendshipV2Controller(
			sendFriendRequestUseCase,
			acceptFriendRequestUseCase,
			rejectFriendRequestUseCase,
			removeFriendUseCase,
			getFriendListUseCase,
			getFriendRequestsUseCase
		);

		mockMvc = MockMvcBuilders
			.standaloneSetup(controller)
			.setControllerAdvice(new FriendshipExceptionHandler())
			.setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
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

	private Friendship createPendingFriendship() {
		return new Friendship(FRIENDSHIP_ID, MEMBER_ID, RECEIVER_ID,
			FriendshipStatus.PENDING, LocalDateTime.of(2025, 1, 1, 12, 0), null);
	}

	private Friendship createAcceptedFriendship() {
		LocalDateTime now = LocalDateTime.of(2025, 1, 2, 12, 0);
		return new Friendship(FRIENDSHIP_ID, MEMBER_ID, RECEIVER_ID,
			FriendshipStatus.ACCEPTED, LocalDateTime.of(2025, 1, 1, 12, 0), now);
	}

	@Nested
	@DisplayName("POST /api/v2/friends/request")
	class SendRequest {

		@Test
		@DisplayName("친구 요청 성공 시 201 Created 반환")
		void success_Returns201() throws Exception {
			// given
			Friendship friendship = createPendingFriendship();
			given(sendFriendRequestUseCase.sendRequest(MEMBER_ID, RECEIVER_ID)).willReturn(friendship);

			// when & then
			mockMvc.perform(post("/api/v2/friends/request")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"receiverId\": 2}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.result").value("SUCCESS"))
				.andExpect(jsonPath("$.data.id").value(FRIENDSHIP_ID))
				.andExpect(jsonPath("$.data.requesterId").value(MEMBER_ID))
				.andExpect(jsonPath("$.data.receiverId").value(RECEIVER_ID))
				.andExpect(jsonPath("$.data.status").value("PENDING"));
		}

		@Test
		@DisplayName("자기 자신에게 요청 시 400 반환 (ExceptionHandler 검증)")
		void selfRequest_Returns400() throws Exception {
			// given
			given(sendFriendRequestUseCase.sendRequest(MEMBER_ID, MEMBER_ID))
				.willThrow(new FriendshipException(FriendshipErrorCode.SELF_FRIEND_REQUEST));

			// when & then
			mockMvc.perform(post("/api/v2/friends/request")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"receiverId\": 1}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errorCode").value("F001"));
		}

		@Test
		@DisplayName("receiverId 누락 시 400 Validation Error 반환")
		void missingReceiverId_Returns400() throws Exception {
			mockMvc.perform(post("/api/v2/friends/request")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{}"))
				.andExpect(status().isBadRequest());
		}
	}

	@Nested
	@DisplayName("POST /api/v2/friends/{id}/accept")
	class Accept {

		@Test
		@DisplayName("친구 요청 수락 성공 시 200 OK 반환")
		void success_Returns200() throws Exception {
			// given
			Friendship friendship = createAcceptedFriendship();
			given(acceptFriendRequestUseCase.accept(FRIENDSHIP_ID, MEMBER_ID)).willReturn(friendship);

			// when & then
			mockMvc.perform(post("/api/v2/friends/{friendshipId}/accept", FRIENDSHIP_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.result").value("SUCCESS"))
				.andExpect(jsonPath("$.data.status").value("ACCEPTED"))
				.andExpect(jsonPath("$.data.acceptedAt").exists());
		}
	}

	@Nested
	@DisplayName("POST /api/v2/friends/{id}/reject")
	class Reject {

		@Test
		@DisplayName("친구 요청 거절 성공 시 200 OK + RsData 반환")
		void success_Returns200() throws Exception {
			// given
			willDoNothing().given(rejectFriendRequestUseCase).reject(FRIENDSHIP_ID, MEMBER_ID);

			// when & then
			mockMvc.perform(post("/api/v2/friends/{friendshipId}/reject", FRIENDSHIP_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.result").value("SUCCESS"));
		}
	}

	@Nested
	@DisplayName("DELETE /api/v2/friends/{id}")
	class Remove {

		@Test
		@DisplayName("친구 삭제 성공 시 200 OK + RsData 반환")
		void success_Returns200() throws Exception {
			// given
			willDoNothing().given(removeFriendUseCase).remove(FRIENDSHIP_ID, MEMBER_ID);

			// when & then
			mockMvc.perform(delete("/api/v2/friends/{friendshipId}", FRIENDSHIP_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.result").value("SUCCESS"));
		}

		@Test
		@DisplayName("존재하지 않는 친구 관계 삭제 시 404 반환")
		void notFound_Returns404() throws Exception {
			// given
			willThrow(new FriendshipException(FriendshipErrorCode.FRIENDSHIP_NOT_FOUND))
				.given(removeFriendUseCase).remove(FRIENDSHIP_ID, MEMBER_ID);

			// when & then
			mockMvc.perform(delete("/api/v2/friends/{friendshipId}", FRIENDSHIP_ID))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.errorCode").value("F003"));
		}
	}

	@Nested
	@DisplayName("GET /api/v2/friends")
	class GetFriends {

		@Test
		@DisplayName("내 친구 목록 조회 성공 시 200 OK + friendshipId 포함")
		void success_Returns200WithFriendshipId() throws Exception {
			// given
			given(getFriendListUseCase.getFriends(MEMBER_ID))
				.willReturn(List.of(new FriendInfo(FRIENDSHIP_ID, RECEIVER_ID, "친구")));

			// when & then
			mockMvc.perform(get("/api/v2/friends"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.result").value("SUCCESS"))
				.andExpect(jsonPath("$.data[0].friendshipId").value(FRIENDSHIP_ID))
				.andExpect(jsonPath("$.data[0].id").value(RECEIVER_ID))
				.andExpect(jsonPath("$.data[0].nickname").value("친구"));
		}
	}

	@Nested
	@DisplayName("GET /api/v2/friends/requests")
	class GetReceivedRequests {

		@Test
		@DisplayName("받은 친구 요청 목록 조회 성공 시 200 OK 반환")
		void success_Returns200() throws Exception {
			// given
			given(getFriendRequestsUseCase.getReceivedRequests(MEMBER_ID))
				.willReturn(List.of(new FriendRequestInfo(
					FRIENDSHIP_ID, MEMBER_ID, "요청자", LocalDateTime.of(2025, 1, 1, 12, 0))));

			// when & then
			mockMvc.perform(get("/api/v2/friends/requests"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.result").value("SUCCESS"))
				.andExpect(jsonPath("$.data[0].friendshipId").value(FRIENDSHIP_ID))
				.andExpect(jsonPath("$.data[0].requester.nickname").value("요청자"));
		}
	}
}
