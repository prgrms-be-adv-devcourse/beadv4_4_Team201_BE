package app.giftify.member.application.service;

import app.giftify.member.adapter.in.web.requestDto.member.SignupRequest;
import app.giftify.member.adapter.out.jpa.entity.member.PreSignup;
import app.giftify.member.application.port.in.member.RegisterMemberUseCase;
import app.giftify.member.application.port.in.member.UpdateMemberUseCase;
import app.giftify.member.application.port.out.member.MemberRepositoryPort;
import app.giftify.member.application.port.out.member.PreSignupPort;
import app.giftify.member.application.service.member.MemberService;
import app.giftify.member.core.domain.exception.member.DuplicateMemberException;
import app.giftify.member.core.domain.member.Member;
import app.giftify.member.core.domain.member.MemberStatus;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.member.MemberSignedEvent;
import app.giftify.shared.domain.event.member.MemberUpdatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepositoryPort memberRepositoryPort;

    @Mock
    private PreSignupPort preSignupPort;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("[회원 가입] 성공 - registerMember")
    void registerMember_Success() {
        // given
        RegisterMemberUseCase.RegisterCommand command = new RegisterMemberUseCase.RegisterCommand(
                "test@example.com",
                "tester",
                LocalDate.of(1990, 1, 1),
                "Seoul",
                "01012345678",
                "Hong",
                "auth0|12345"
        );

        Member savedMember = Member.builder()
                .id(1L)
                .email(command.email())
                .authSub(command.authSub())
                .nickname(command.nickname())
                .build();

        given(memberRepositoryPort.findByAuthSub(command.authSub())).willReturn(Optional.empty());
        given(memberRepositoryPort.save(any(Member.class))).willReturn(savedMember);

        // when
        Member result = memberService.registerMember(command);

        // then
        assertThat(result.getEmail()).isEqualTo(command.email());
        assertThat(result.getNickname()).isEqualTo(command.nickname());
        verify(memberRepositoryPort).save(any(Member.class));
        verify(eventPublisher).publish(any(MemberSignedEvent.class));
    }

    @Test
    @DisplayName("[회원 가입] 이미 가입된 회원인 경우 예외 발생")
    void registerMember_DuplicateMember() {
        // given
        RegisterMemberUseCase.RegisterCommand command = new RegisterMemberUseCase.RegisterCommand(
                "test@example.com",
                "tester",
                LocalDate.of(1990, 1, 1),
                "Seoul",
                "01012345678",
                "Hong",
                "auth0|12345"
        );

        given(memberRepositoryPort.findByAuthSub(command.authSub()))
                .willReturn(Optional.of(Member.builder().build()));

        // when & then
        assertThatThrownBy(() -> memberService.registerMember(command))
                .isInstanceOf(DuplicateMemberException.class);
    }

    @Test
    @DisplayName("[회원 가입] 임시 정보를 통한 가입 성공 - signup")
    void signup_Success() {
        // given
        String authSub = "auth0|12345";
        SignupRequest request = new SignupRequest(
                LocalDate.of(1995, 1, 1),
                "Seoul Gangnam",
                "010-1111-2222"
        );

        PreSignup preSignup = new PreSignup(authSub, "pre@example.com", "Hong", "preNick");

        given(preSignupPort.findByAuthSub(authSub)).willReturn(Optional.of(preSignup));
        given(memberRepositoryPort.findByAuthSub(authSub)).willReturn(Optional.empty());
        given(memberRepositoryPort.save(any(Member.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        Member result = memberService.signup(authSub, request);

        // then
        assertThat(result.getAuthSub()).isEqualTo(authSub);
        assertThat(result.getEmail()).isEqualTo("pre@example.com");
        assertThat(result.getNickname()).isEqualTo("preNick");
        assertThat(result.getBirthday()).isEqualTo(request.birthday());
        verify(preSignupPort).deleteByAuthSub(authSub);
    }

    @Test
    @DisplayName("[이메일 존재 확인] 이메일이 존재하는 경우 true")
    void existsByEmail_True() {
        // given
        String email = "test@example.com";
        given(memberRepositoryPort.findByEmail(email)).willReturn(Optional.of(Member.builder().build()));

        // when
        boolean result = memberService.existsByEmail(email);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("[이메일 존재 확인] 이메일이 존재하지 않는 경우 false")
    void existsByEmail_False() {
        // given
        String email = "notfound@example.com";
        given(memberRepositoryPort.findByEmail(email)).willReturn(Optional.empty());

        // when
        boolean result = memberService.existsByEmail(email);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("[닉네임 중복 확인] 이미 존재하는 닉네임인 경우 true")
    void isNicknameDuplicated_True() {
        // given
        String nickname = "duplicated";
        given(memberRepositoryPort.findByNickname(nickname)).willReturn(Optional.of(Member.builder().build()));

        // when
        boolean result = memberService.isNicknameDuplicated(nickname);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("[닉네임 중복 확인] 존재하지 않는 닉네임인 경우 false")
    void isNicknameDuplicated_False() {
        // given
        String nickname = "unique";
        given(memberRepositoryPort.findByNickname(nickname)).willReturn(Optional.empty());

        // when
        boolean result = memberService.isNicknameDuplicated(nickname);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("[회원 조회] AuthSub으로 가입된 회원 조회 성공")
    void getMemberByAuthSub_Success() {
        // given
        String authSub = "auth0|12345";
        Member member = Member.builder()
                .id(1L)
                .authSub(authSub)
                .email("test@example.com")
                .build();

        given(memberRepositoryPort.findByAuthSub(authSub)).willReturn(Optional.of(member));

        // when
        Optional<Member> result = memberService.getMemberByAuthSub(authSub);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getAuthSub()).isEqualTo(authSub);
        verify(memberRepositoryPort).findByAuthSub(authSub);
    }

    @Test
    @DisplayName("[회원 수정] 성공")
    void updateMember_Success() {
        // given
        String authSub = "auth0|12345";
        UpdateMemberUseCase.UpdateCommand command = new UpdateMemberUseCase.UpdateCommand(
                authSub,
                "1234",
                "newNick",
                "New Address",
                "01011112222",
                "New Name"
        );
        Member member = Member.builder()
                .authSub(authSub)
                .nickname("oldNick")
                .name("oldName")
                .build();

        given(memberRepositoryPort.findByAuthSub(authSub)).willReturn(Optional.of(member));
        given(memberRepositoryPort.save(any(Member.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        Member result = memberService.updateMember(command);

        // then
        assertThat(result.getNickname()).isEqualTo("newNick");
        assertThat(result.getAddress()).isEqualTo("New Address");
        assertThat(result.getName()).isEqualTo("New Name");
        verify(memberRepositoryPort).save(any(Member.class));
        verify(eventPublisher).publish(any(MemberUpdatedEvent.class));
    }

    @Test
    @DisplayName("[회원 탈퇴] 성공")
    void withdrawMember_Success() {
        // given
        String authSub = "auth0|12345";
        Member member = Member.builder()
                .authSub(authSub)
                .status(MemberStatus.ACTIVE)
                .build();

        given(memberRepositoryPort.findByAuthSub(authSub)).willReturn(Optional.of(member));
        given(memberRepositoryPort.save(any(Member.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        memberService.withdrawMember(authSub);

        // then
        assertThat(member.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
        verify(memberRepositoryPort).save(member);
    }
}
