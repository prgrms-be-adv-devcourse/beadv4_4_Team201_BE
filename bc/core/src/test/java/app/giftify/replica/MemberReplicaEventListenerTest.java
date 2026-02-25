package app.giftify.replica;

import app.giftify.shared.domain.event.member.MemberSignedEvent;
import app.giftify.shared.domain.event.member.MemberUpdatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberReplicaEventListener 테스트")
class MemberReplicaEventListenerTest {

    @InjectMocks
    private MemberReplicaEventListener listener;

    @Mock
    private MemberReplicaSyncUseCase memberReplicaSyncUseCase;

    @Test
    @DisplayName("회원가입 이벤트 수신 시 회원 레플리카 동기화")
    void handleMemberSignedEvent() {
        Long memberId = 1L;
        String nickname = "테스트유저";
        MemberSignedEvent event = new MemberSignedEvent(memberId, "auth0|user1", nickname);

        listener.handle(event);

        verify(memberReplicaSyncUseCase).syncMember(memberId, nickname);
    }

    @Test
    @DisplayName("회원정보 수정 이벤트 수신 시 회원 레플리카 동기화")
    void handleMemberUpdatedEvent() {
        Long memberId = 1L;
        String nickname = "수정된닉네임";
        MemberUpdatedEvent event = new MemberUpdatedEvent(memberId, "auth0|user1", nickname);

        listener.handle(event);

        verify(memberReplicaSyncUseCase).syncMember(memberId, nickname);
    }
}
