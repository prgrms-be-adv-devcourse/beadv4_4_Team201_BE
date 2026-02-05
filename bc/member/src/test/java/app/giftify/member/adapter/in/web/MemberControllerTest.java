package app.giftify.member.adapter.in.web;

import app.giftify.member.adapter.in.web.dto.MemberUpdateRequest;
import app.giftify.member.adapter.in.web.dto.SignupRequest;
import app.giftify.member.application.port.in.GetMemberUseCase;
import app.giftify.member.application.port.in.RegisterMemberUseCase;
import app.giftify.member.application.port.in.UpdateMemberUseCase;
import app.giftify.member.application.port.in.WithdrawMemberUseCase;
import app.giftify.member.domain.exception.DuplicateMemberException;
import app.giftify.member.domain.exception.MemberErrorCode;
import app.giftify.member.domain.member.Member;
import app.giftify.member.domain.member.MemberStatus;
import app.giftify.security.common.CurrentAuthSub;
import app.giftify.shared.domain.type.MemberRole;
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

import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GetMemberUseCase getMemberUseCase;

    @MockitoBean
    private RegisterMemberUseCase registerMemberUseCase;

    @MockitoBean
    private UpdateMemberUseCase updateMemberUseCase;

    @MockitoBean
    private WithdrawMemberUseCase withdrawMemberUseCase;

    private static final String AUTH_SUB = "auth0|12345";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new MemberController(getMemberUseCase, registerMemberUseCase, updateMemberUseCase, withdrawMemberUseCase))
                .setControllerAdvice(new MemberExceptionHandler())
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.hasParameterAnnotation(CurrentAuthSub.class);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                        return AUTH_SUB;
                    }
                })
                .build();
    }

    @Test
    @DisplayName("[가입 상태 확인] 가입되지 않은 사용자")
    void checkRegistration_NotRegistered() throws Exception {
        // given
        given(getMemberUseCase.getMemberByAuthSub(AUTH_SUB)).willReturn(Optional.empty());

        // when & then
        mockMvc.perform(get("/api/members/check-registration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NOT_REGISTERED"));
    }

    @Test
    @DisplayName("[가입 상태 확인] 가입된 사용자")
    void checkRegistration_Registered() throws Exception {
        // given
        Member member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .authSub(AUTH_SUB)
                .nickname("tester")
                .role(MemberRole.BUYER)
                .status(MemberStatus.ACTIVE)
                .build();
        given(getMemberUseCase.getMemberByAuthSub(AUTH_SUB)).willReturn(Optional.of(member));

        // when & then
        mockMvc.perform(get("/api/members/check-registration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("tester"));
    }

    @Test
    @DisplayName("[회원 가입] 성공")
    void signup_Success() throws Exception {
        // given
        String email = "test@example.com";
        SignupRequest request = new SignupRequest(
                LocalDate.of(1990, 1, 1),
                "Seoul, Korea",
                "01012345678"
        );

        Member savedMember = Member.builder()
                .id(1L)
                .email(email)
                .authSub(AUTH_SUB)
                .nickname("preNick")
                .birthday(request.birthday())
                .address(request.address())
                .phoneNum(request.phoneNum())
                .name("preName")
                .role(MemberRole.BUYER)
                .status(MemberStatus.ACTIVE)
                .build();

        given(registerMemberUseCase.signup(eq(AUTH_SUB), any(SignupRequest.class)))
                .willReturn(savedMember);

        // when & then
        mockMvc.perform(post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.authSub").value(AUTH_SUB));
    }

    @Test
    @DisplayName("[회원 가입] 모든 필드가 Optional이므로 빈 값으로도 가입 성공")
    void signupWithEmptyFields() throws Exception {
        // given
        SignupRequest request = new SignupRequest(null, null, null);
        Member mockMember = Member.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("행복한고양이1234")
                .authSub("auth0|test")
                .build();

        given(registerMemberUseCase.signup(any(), any())).willReturn(mockMember);

        // when & then
        mockMvc.perform(post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())  // 200 성공
                .andExpect(jsonPath("$.nickname").exists());
    }

    @Test
    @DisplayName("[예외처리] DuplicateMemberException 발생 시 400 에러 및 메시지 확인")
    void handleDuplicateMemberException() throws Exception {
        // given
        SignupRequest request = new SignupRequest(
                LocalDate.of(1990, 1, 1),
                "Seoul",
                "01012345678"
        );
        given(registerMemberUseCase.signup(any(), any())).willThrow(new DuplicateMemberException("test@example.com"));

        // when & then
        mockMvc.perform(post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(MemberErrorCode.DUPLICATE_MEMBER.getStatusCode()))
                .andExpect(jsonPath("$.code").value("M201"))
                .andExpect(jsonPath("$.message", containsString("이미 가입된 이메일입니다")));
    }

    @Test
    @DisplayName("[내 정보 조회] 성공")
    void getMyInfo_Success() throws Exception {
        // given
        Member member = Member.builder()
                .authSub(AUTH_SUB)
                .nickname("tester")
                .build();
        given(getMemberUseCase.getMemberByAuthSub(AUTH_SUB)).willReturn(Optional.of(member));

        // when & then
        mockMvc.perform(get("/api/members/getMyInfo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("tester"));
    }

    @Test
    @DisplayName("[회원 수정] 성공")
    void updateMyInfo_Success() throws Exception {
        // given
        MemberUpdateRequest request = new MemberUpdateRequest("1234", "newNick", "New Address", "01011112222", "New Name");
        Member updatedMember = Member.builder()
                .authSub(AUTH_SUB)
                .nickname("newNick")
                .build();

        given(getMemberUseCase.getMemberByAuthSub(AUTH_SUB)).willReturn(Optional.of(Member.builder().status(MemberStatus.ACTIVE).build()));
        given(updateMemberUseCase.updateMember(any())).willReturn(updatedMember);

        // when & then
        mockMvc.perform(patch("/api/members/updateMyInfo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("newNick"));
    }

    @Test
    @DisplayName("[회원 탈퇴] 성공")
    void withdraw_Success() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/members/withdraw"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("[닉네임 중복 확인] 이미 존재하는 닉네임")
    void checkNickname_Duplicated() throws Exception {
        // given
        String nickname = "duplicated";
        given(getMemberUseCase.isNicknameDuplicated(nickname)).willReturn(true);

        // when & then
        mockMvc.perform(get("/api/members/check/nickname")
                        .param("nickname", nickname))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DUPLICATED"));
    }

    @Test
    @DisplayName("[닉네임 중복 확인] 사용 가능한 닉네임")
    void checkNickname_Available() throws Exception {
        // given
        String nickname = "unique";
        given(getMemberUseCase.isNicknameDuplicated(nickname)).willReturn(false);

        // when & then
        mockMvc.perform(get("/api/members/check/nickname")
                        .param("nickname", nickname))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    @DisplayName("[닉네임 중복 확인] 닉네임이 공백인 경우")
    void checkNickname_Blank() throws Exception {
        // given
        String nickname = "";

        // when & then
        mockMvc.perform(get("/api/members/check/nickname")
                        .param("nickname", nickname))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }
}
