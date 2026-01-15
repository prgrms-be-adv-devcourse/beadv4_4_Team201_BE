package app.giftify.member.adapter.in.web;

import app.giftify.member.adapter.in.web.dto.SignupRequest;
import app.giftify.member.application.port.in.GetMemberUseCase;
import app.giftify.member.application.port.in.RegisterMemberUseCase;
import app.giftify.member.core.domain.member.Member;
import app.giftify.member.core.domain.member.MemberRole;
import app.giftify.member.core.domain.member.MemberStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GetMemberUseCase getMemberUseCase;

    @MockBean
    private RegisterMemberUseCase registerMemberUseCase;

    @Test
    @DisplayName("[가입 상태 확인] 가입되지 않은 사용자")
    void checkRegistration_NotRegistered() throws Exception {
        // given
        String authSub = "auth0|12345";
        given(getMemberUseCase.getMemberByAuthSub(authSub)).willReturn(Optional.empty());

        // when & then
        mockMvc.perform(get("/api/members/check-registration")
                        .with(jwt().jwt(builder -> builder.subject(authSub))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NOT_REGISTERED"));
    }

    @Test
    @DisplayName("[가입 상태 확인] 가입된 사용자")
    void checkRegistration_Registered() throws Exception {
        // given
        String authSub = "auth0|12345";
        Member member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .authSub(authSub)
                .nickname("tester")
                .role(MemberRole.BUYER)
                .status(MemberStatus.ACTIVE)
                .build();
        given(getMemberUseCase.getMemberByAuthSub(authSub)).willReturn(Optional.of(member));

        // when & then
        mockMvc.perform(get("/api/members/check-registration")
                        .with(jwt().jwt(builder -> builder.subject(authSub))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("tester"));
    }

    @Test
    @DisplayName("[회원 가입] 성공")
    void signup_Success() throws Exception {
        // given
        String authSub = "auth0|12345";
        String email = "test@example.com";
        SignupRequest request = new SignupRequest(
                "tester",
                LocalDate.of(1990, 1, 1),
                "Seoul, Korea",
                1012345678L,
                "Hong Gil Dong"
        );

        Member savedMember = Member.builder()
                .id(1L)
                .email(email)
                .authSub(authSub)
                .nickname(request.nickname())
                .birthday(request.birthday())
                .address(request.address())
                .phoneNum(request.phoneNum())
                .name(request.name())
                .role(MemberRole.BUYER)
                .status(MemberStatus.ACTIVE)
                .build();

        given(registerMemberUseCase.registerMember(any(RegisterMemberUseCase.RegisterCommand.class)))
                .willReturn(savedMember);

        // when & then
        mockMvc.perform(post("/api/members/signup")
                        .with(jwt().jwt(builder -> builder.subject(authSub).claim("email", email)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.nickname").value(request.nickname()));
    }

    @Test
    @DisplayName("[회원 가입] 필수 값 누락 시 400 에러")
    void signup_ValidationFailed() throws Exception {
        // given
        String authSub = "auth0|12345";
        SignupRequest request = new SignupRequest(
                "", // nickname blank
                null, // birthday null
                "Seoul, Korea",
                1012345678L,
                "Hong Gil Dong"
        );

        // when & then
        mockMvc.perform(post("/api/members/signup")
                        .with(jwt().jwt(builder -> builder.subject(authSub)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("인증 정보 없이 접근 시 401 에러")
    void checkRegistration_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/members/check-registration"))
                .andExpect(status().isUnauthorized());
    }
}
