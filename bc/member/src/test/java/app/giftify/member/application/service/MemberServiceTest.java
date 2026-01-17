package app.giftify.member.application.service;

import app.giftify.member.application.port.in.RegisterMemberUseCase;
import app.giftify.member.application.port.in.UpdateMemberUseCase;
import app.giftify.member.application.port.out.MemberRepositoryPort;
import app.giftify.member.core.domain.exception.DuplicateMemberException;
import app.giftify.member.core.domain.member.Member;
import app.giftify.member.core.domain.member.MemberStatus;
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
                "1012345678L",
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
                "1012345678L",
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
                "01011112222L",
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
