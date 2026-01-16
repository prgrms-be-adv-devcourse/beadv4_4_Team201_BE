package app.giftify.member.application.service;

import app.giftify.member.application.port.in.RegisterMemberUseCase;
import app.giftify.member.application.port.out.MemberEventSpringPublisher;
import app.giftify.member.application.port.out.MemberRepositoryPort;
import app.giftify.member.core.domain.member.Member;
import app.giftify.member.core.exception.DuplicateMemberException;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepositoryPort memberRepositoryPort;

    @Mock
    private MemberEventSpringPublisher memberEventSpringPublisher;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("[회원 가입] 성공")
    void registerMember_Success() {
        // given
        RegisterMemberUseCase.RegisterCommand command = new RegisterMemberUseCase.RegisterCommand(
                "test@example.com",
                "auth0|12345",
                "tester",
                LocalDate.of(1990, 1, 1),
                "Seoul",
                1012345678L,
                "Hong"
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
        verify(memberEventSpringPublisher).publishMemberRegistered(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("[회원 가입] 이미 가입된 회원인 경우 예외 발생")
    void registerMember_DuplicateMember() {
        // given
        RegisterMemberUseCase.RegisterCommand command = new RegisterMemberUseCase.RegisterCommand(
                "test@example.com",
                "auth0|12345",
                "tester",
                LocalDate.of(1990, 1, 1),
                "Seoul",
                1012345678L,
                "Hong"
        );

        given(memberRepositoryPort.findByAuthSub(command.authSub()))
                .willReturn(Optional.of(Member.builder().build()));

        // when & then
        assertThatThrownBy(() -> memberService.registerMember(command))
                .isInstanceOf(DuplicateMemberException.class);
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
        verify(memberEventSpringPublisher).publishMemberLoggedIn(anyLong(), anyString(), anyString());
    }
}
