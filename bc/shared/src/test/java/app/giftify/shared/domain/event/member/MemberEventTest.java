package app.giftify.shared.domain.event.member;

import app.giftify.shared.domain.type.MemberRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberEventTest {

    @Test
    @DisplayName("[이벤트] MemberSignedEvent 생성 및 Getter 검증")
    void memberSignedEventTest() {
        // given
        Long memberId = 1L;
        String authSub = "auth0|123";
        String nickname = "tester";

        // when
        MemberSignedEvent event = new MemberSignedEvent(memberId, authSub, nickname);

        // then
        assertThat(event.getMemberId()).isEqualTo(memberId);
        assertThat(event.getAuthSub()).isEqualTo(authSub);
        assertThat(event.getNickname()).isEqualTo(nickname);
        assertThat(event.toString()).contains("memberId=1", "authSub='auth0|123'", "nickname='tester'");
    }

    @Test
    @DisplayName("[이벤트] MemberUpdatedEvent 생성 및 Getter 검증")
    void memberUpdatedEventTest() {
        // given
        Long memberId = 1L;
        String authSub = "auth0|123";
        String nickname = "newTester";
        MemberRole role = MemberRole.BUYER;

        // when
        MemberUpdatedEvent event = new MemberUpdatedEvent(memberId, authSub, nickname, role);

        // then
        assertThat(event.getMemberId()).isEqualTo(memberId);
        assertThat(event.getAuthSub()).isEqualTo(authSub);
        assertThat(event.getNickname()).isEqualTo(nickname);
        assertThat(event.getRole()).isEqualTo(role);
        assertThat(event.toString()).contains("memberId=1", "authSub='auth0|123'", "nickname='newTester'");
    }
}
