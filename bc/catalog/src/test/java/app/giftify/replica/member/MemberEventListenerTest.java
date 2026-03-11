package app.giftify.replica.member;

import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.member.MemberUpdatedEvent;
import app.giftify.shared.domain.event.member.SellerNicknameChangedEvent;
import app.giftify.shared.domain.type.MemberRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class MemberEventListenerTest {

    private final MemberSyncUseCase memberSyncUseCase = Mockito.mock(MemberSyncUseCase.class);
    private final EventPublisher eventPublisher = Mockito.mock(EventPublisher.class);
    private final MemberEventListener listener = new MemberEventListener(memberSyncUseCase, eventPublisher);

    @Test
    @DisplayName("SELLER 닉네임 변경 시 레플리카 동기화 후 SellerNicknameChangedEvent를 발행한다")
    void sellerUpdated_publishesSellerNicknameChangedEvent() {
        // given
        Long memberId = 1L;
        String nickname = "새닉네임";
        MemberUpdatedEvent event = new MemberUpdatedEvent(memberId, "auth0|test", nickname, MemberRole.SELLER);

        // when
        listener.handle(event);

        // then
        verify(memberSyncUseCase).syncMember(memberId, nickname);
        ArgumentCaptor<SellerNicknameChangedEvent> captor = ArgumentCaptor.forClass(SellerNicknameChangedEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().getSellerId()).isEqualTo(memberId);
        assertThat(captor.getValue().getNickname()).isEqualTo(nickname);
    }

    @Test
    @DisplayName("BUYER 닉네임 변경 시 레플리카 동기화만 하고 SellerNicknameChangedEvent는 발행하지 않는다")
    void buyerUpdated_doesNotPublishSellerNicknameChangedEvent() {
        // given
        Long memberId = 2L;
        String nickname = "바이어닉네임";
        MemberUpdatedEvent event = new MemberUpdatedEvent(memberId, "auth0|test2", nickname, MemberRole.BUYER);

        // when
        listener.handle(event);

        // then
        verify(memberSyncUseCase).syncMember(memberId, nickname);
        verify(eventPublisher, never()).publish(any(SellerNicknameChangedEvent.class));
    }
}
