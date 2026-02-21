package app.giftify.friendship.domain;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import app.giftify.friendship.domain.exception.FriendshipException;

class FriendshipTest {

    @Test
    void create_성공() {
        Friendship friendship = Friendship.create(1L, 2L);

        assertThat(friendship.getRequesterId()).isEqualTo(1L);
        assertThat(friendship.getReceiverId()).isEqualTo(2L);
        assertThat(friendship.getStatus()).isEqualTo(app.giftify.shared.domain.type.FriendshipStatus.PENDING);
        assertThat(friendship.getAcceptedAt()).isNull();
    }

    @Test
    void create_자기자신_요청시_예외() {
        assertThatThrownBy(() -> Friendship.create(1L, 1L))
                .isInstanceOf(FriendshipException.class);
    }

    @Test
    void accept_receiver가_수락() {
        Friendship friendship = Friendship.create(1L, 2L);

        friendship.accept(2L);

        assertThat(friendship.getStatus()).isEqualTo(app.giftify.shared.domain.type.FriendshipStatus.ACCEPTED);
        assertThat(friendship.getAcceptedAt()).isNotNull();
    }

    @Test
    void accept_requester가_수락시_예외() {
        Friendship friendship = Friendship.create(1L, 2L);

        assertThatThrownBy(() -> friendship.accept(1L))
                .isInstanceOf(FriendshipException.class);
    }

    @Test
    void accept_이미_수락된_요청_재수락시_예외() {
        Friendship friendship = Friendship.create(1L, 2L);
        friendship.accept(2L);

        assertThatThrownBy(() -> friendship.accept(2L))
                .isInstanceOf(FriendshipException.class);
    }

    @Test
    void reject_receiver가_거절() {
        Friendship friendship = Friendship.create(1L, 2L);

        friendship.reject(2L);

        assertThat(friendship.getStatus()).isEqualTo(app.giftify.shared.domain.type.FriendshipStatus.REJECTED);
    }

    @Test
    void reject_requester가_거절시_예외() {
        Friendship friendship = Friendship.create(1L, 2L);

        assertThatThrownBy(() -> friendship.reject(1L))
                .isInstanceOf(FriendshipException.class);
    }

    @Test
    void validateMember_당사자가_아닌_경우_예외() {
        Friendship friendship = Friendship.create(1L, 2L);

        assertThatThrownBy(() -> friendship.validateMember(3L))
                .isInstanceOf(FriendshipException.class);
    }

    @Test
    void getFriendId_requester_관점() {
        Friendship friendship = Friendship.create(1L, 2L);

        assertThat(friendship.getFriendId(1L)).isEqualTo(2L);
    }

    @Test
    void getFriendId_receiver_관점() {
        Friendship friendship = Friendship.create(1L, 2L);

        assertThat(friendship.getFriendId(2L)).isEqualTo(1L);
    }
}
