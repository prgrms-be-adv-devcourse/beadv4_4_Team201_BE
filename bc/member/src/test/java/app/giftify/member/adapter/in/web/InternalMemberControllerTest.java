package app.giftify.member.adapter.in.web;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import app.giftify.member.adapter.in.web.controller.InternalMemberController;
import app.giftify.member.application.port.in.GetMemberUseCase;
import app.giftify.member.core.domain.member.Member;
import app.giftify.member.core.domain.member.MemberStatus;
import app.giftify.shared.domain.type.MemberRole;

@WebMvcTest(InternalMemberController.class)
class InternalMemberControllerTest {

	private MockMvc mockMvc;

	@MockBean
	private GetMemberUseCase getMemberUseCase;

	private static final String AUTH_SUB = "auth0|12345";

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders
			.standaloneSetup(new InternalMemberController(getMemberUseCase))
			.build();
	}

	@Test
	@DisplayName("[내부 API] authSub로 회원 조회 - 존재하는 회원")
	void getByAuthSub_Found() throws Exception {
		// given
		Member member = Member.builder()
			.id(1L)
			.email("test@example.com")
			.authSub(AUTH_SUB)
			.nickname("tester")
			.role(MemberRole.SELLER)
			.status(MemberStatus.ACTIVE)
			.build();
		given(getMemberUseCase.getMemberByAuthSub(AUTH_SUB)).willReturn(Optional.of(member));

		// when & then
		mockMvc.perform(get("/api/internal/members/by-auth-sub/{authSub}", AUTH_SUB))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.memberId").value(1L))
			.andExpect(jsonPath("$.authSub").value(AUTH_SUB))
			.andExpect(jsonPath("$.role").value("SELLER"))
			.andExpect(jsonPath("$.email").value("test@example.com"))
			.andExpect(jsonPath("$.nickname").value("tester"));
	}

	@Test
	@DisplayName("[내부 API] authSub로 회원 조회 - 존재하지 않는 회원")
	void getByAuthSub_NotFound() throws Exception {
		// given
		given(getMemberUseCase.getMemberByAuthSub(AUTH_SUB)).willReturn(Optional.empty());

		// when & then
		mockMvc.perform(get("/api/internal/members/by-auth-sub/{authSub}", AUTH_SUB))
			.andExpect(status().isNotFound());
	}
}
