package app.giftify.cart.readmodel;

import app.giftify.member.domain.event.MemberSignedEvent;
import app.giftify.member.domain.event.MemberUpdatedEvent;
import app.giftify.support.common.security.MemberRole;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class MemberViewSyncListenerTest {

    private final MemberViewRepository repository = mock(MemberViewRepository.class);
    private final MemberViewSyncListener listener = new MemberViewSyncListener(repository);

    @Test
    @DisplayName("MemberSignedEvent 수신 시 신규 MemberView 가 저장된다")
    void newMember_inserts_view() {
        Long memberId = 1L;
        String nickname = "신규";
        given(repository.findById(memberId)).willReturn(Optional.empty());

        listener.on(new MemberSignedEvent(memberId, "auth0|new", nickname));

        ArgumentCaptor<MemberView> captor = ArgumentCaptor.forClass(MemberView.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(memberId);
        assertThat(captor.getValue().getNickname()).isEqualTo(nickname);
    }

    @Test
    @DisplayName("MemberUpdatedEvent (SELLER) 수신 시 view 만 갱신되고 추가 이벤트는 발행되지 않는다")
    void sellerUpdated_only_updates_view() {
        Long memberId = 2L;
        MemberView existing = new MemberView(memberId, "이전");
        given(repository.findById(memberId)).willReturn(Optional.of(existing));

        listener.on(new MemberUpdatedEvent(memberId, "auth0|x", "신규", MemberRole.SELLER));

        assertThat(existing.getNickname()).isEqualTo("신규");
        verify(repository, never()).save(existing);
    }

    @Test
    @DisplayName("MemberUpdatedEvent (BUYER) 수신 시 기존 view 가 갱신된다")
    void buyerUpdated_updates_view() {
        Long memberId = 3L;
        MemberView existing = new MemberView(memberId, "이전");
        given(repository.findById(memberId)).willReturn(Optional.of(existing));

        listener.on(new MemberUpdatedEvent(memberId, "auth0|y", "신규", MemberRole.BUYER));

        assertThat(existing.getNickname()).isEqualTo("신규");
        verify(repository, never()).save(existing);
    }
}
