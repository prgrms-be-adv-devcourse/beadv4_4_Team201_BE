package app.giftify.member.domain.domain.member;

import app.giftify.member.domain.exception.MemberStatusException;
import app.giftify.member.domain.member.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberTest {

    @Test
    @DisplayName("[도메인] Member 생성 성공")
    void createMemberSuccess() {
        // given
        String email = "test@test.com";
        String nickname = "tester";
        String authSub = "auth0|123";

        // when
        Member member = Member.create(email, nickname, LocalDate.of(1990, 1, 1), "Seoul", "01012345678", "Hong", authSub);

        // then
        assertThat(member.getEmail()).isEqualTo(email);
        assertThat(member.getNickname()).isEqualTo(nickname);
        assertThat(member.getAuthSub()).isEqualTo(authSub);
        assertThat(member.isActive()).isTrue();
    }

    @Test
    @DisplayName("[도메인] Member 생성 실패 - 필수값 누락")
    void createMemberFail() {
        assertThatThrownBy(() -> Member.create(null, "nick", LocalDate.now(), "addr", "phone", "name", "sub"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Member.create("email", null, LocalDate.now(), "addr", "phone", "name", "sub"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Member.create("email", "nick", LocalDate.now(), "addr", "phone", "name", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("[도메인] Member 정보 수정")
    void updateInfo() {
        // given
        Member member = Member.create("e", "n", LocalDate.now(), "a", "p", "name", "s");

        // when
        member.updateInfo("newNick", "newPass", "newAddr", "newPhone", "newName");

        // then
        assertThat(member.getNickname()).isEqualTo("newNick");
        assertThat(member.getPassword()).isEqualTo("newPass");
        assertThat(member.getAddress()).isEqualTo("newAddr");
        assertThat(member.getPhoneNum()).isEqualTo("newPhone");
        assertThat(member.getName()).isEqualTo("newName");
    }

    @Test
    @DisplayName("[도메인] Member 탈퇴 및 상태 확인")
    void withdrawAndStatus() {
        // given
        Member member = Member.create("e", "n", LocalDate.now(), "a", "p", "name", "s");

        // when
        member.withdraw();

        // then
        assertThat(member.isActive()).isFalse();
        assertThatThrownBy(member::validateActiveStatus).isInstanceOf(MemberStatusException.class);
    }
}
