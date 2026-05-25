package app.giftify.friendship.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import app.giftify.friendship.application.port.in.FriendInfo;
import app.giftify.friendship.application.port.in.FriendRequestInfo;
import app.giftify.friendship.application.port.out.FriendshipRepositoryPort;
import app.giftify.friendship.domain.Friendship;
import app.giftify.friendship.domain.FriendshipStatus;
import app.giftify.friendship.domain.exception.FriendshipErrorCode;
import app.giftify.friendship.domain.exception.FriendshipException;
import app.giftify.member.application.port.out.MemberRepositoryPort;
import app.giftify.member.domain.member.Member;
import app.giftify.member.domain.member.MemberStatus;
import app.giftify.support.common.event.EventPublisher;

@ExtendWith(MockitoExtension.class)
class FriendshipServiceTest {

    @Mock FriendshipRepositoryPort friendshipRepository;
    @Mock MemberRepositoryPort memberRepository;
    @Mock EventPublisher eventPublisher;
    @InjectMocks FriendshipService service;

    @Test
    void sendRequest_성공() {
        given(memberRepository.findById(2L)).willReturn(Optional.of(dummyMember(2L)));
        given(friendshipRepository.existsByMemberPairAndStatusIn(eq(1L), eq(2L), any()))
                .willReturn(false);
        given(friendshipRepository.save(any())).willAnswer(inv -> {
            Friendship f = inv.getArgument(0);
            return new Friendship(100L, f.getRequesterId(), f.getReceiverId(),
                    f.getStatus(), f.getCreatedAt(), f.getAcceptedAt());
        });

        Friendship result = service.sendRequest(1L, 2L);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getStatus()).isEqualTo(FriendshipStatus.PENDING);
        then(eventPublisher).should().publish(any());
    }

    @Test
    void sendRequest_대상회원없으면_예외() {
        given(memberRepository.findById(2L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.sendRequest(1L, 2L))
                .isInstanceOf(FriendshipException.class);
    }

    @Test
    void sendRequest_중복관계_예외() {
        given(memberRepository.findById(2L)).willReturn(Optional.of(dummyMember(2L)));
        given(friendshipRepository.existsByMemberPairAndStatusIn(eq(1L), eq(2L), any()))
                .willReturn(true);

        assertThatThrownBy(() -> service.sendRequest(1L, 2L))
                .isInstanceOf(FriendshipException.class);
    }

    @Test
    void accept_성공() {
        Friendship pending = Friendship.create(1L, 2L);
        Friendship withId = new Friendship(10L, 1L, 2L,
                FriendshipStatus.PENDING, pending.getCreatedAt(), null);
        given(friendshipRepository.findById(10L)).willReturn(Optional.of(withId));
        given(friendshipRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        Friendship result = service.accept(10L, 2L);

        assertThat(result.getStatus()).isEqualTo(FriendshipStatus.ACCEPTED);
        then(eventPublisher).should().publish(any());
    }

    @Test
    void reject_성공() {
        Friendship withId = new Friendship(10L, 1L, 2L,
                FriendshipStatus.PENDING, null, null);
        given(friendshipRepository.findById(10L)).willReturn(Optional.of(withId));
        given(friendshipRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        service.reject(10L, 2L);

        then(friendshipRepository).should().save(any());
    }

    @Test
    void remove_성공() {
        Friendship withId = new Friendship(10L, 1L, 2L,
                FriendshipStatus.ACCEPTED, null, null);
        given(friendshipRepository.findById(10L)).willReturn(Optional.of(withId));

        service.remove(10L, 1L);

        then(friendshipRepository).should().delete(any());
    }

    @Test
    void remove_당사자아닌경우_예외() {
        Friendship withId = new Friendship(10L, 1L, 2L,
                FriendshipStatus.ACCEPTED, null, null);
        given(friendshipRepository.findById(10L)).willReturn(Optional.of(withId));

        assertThatThrownBy(() -> service.remove(10L, 3L))
                .isInstanceOf(FriendshipException.class);
    }

    @Test
    void getFriends_Member조회포함_FriendInfo반환() {
        Friendship friendship = new Friendship(10L, 1L, 2L,
                FriendshipStatus.ACCEPTED, LocalDateTime.now(), LocalDateTime.now());
        given(friendshipRepository.findAllByMemberIdAndStatus(1L, FriendshipStatus.ACCEPTED))
                .willReturn(List.of(friendship));
        given(memberRepository.findAllByIds(List.of(2L)))
                .willReturn(List.of(dummyMember(2L)));

        List<FriendInfo> result = service.getFriends(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).friendshipId()).isEqualTo(10L);
        assertThat(result.get(0).memberId()).isEqualTo(2L);
        assertThat(result.get(0).nickname()).isEqualTo("test");
    }

    @Test
    void getReceivedRequests_Member조회포함_FriendRequestInfo반환() {
        Friendship friendship = new Friendship(10L, 3L, 1L,
                FriendshipStatus.PENDING, LocalDateTime.now(), null);
        given(friendshipRepository.findAllByReceiverIdAndStatus(1L, FriendshipStatus.PENDING))
                .willReturn(List.of(friendship));
        given(memberRepository.findAllByIds(List.of(3L)))
                .willReturn(List.of(dummyMember(3L)));

        List<FriendRequestInfo> result = service.getReceivedRequests(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).friendshipId()).isEqualTo(10L);
        assertThat(result.get(0).requesterId()).isEqualTo(3L);
    }

    @Test
    void sendRequest_탈퇴한회원에게요청_예외() {
        Member withdrawn = Member.builder().id(2L)
                .email("test@test.com").nickname("test")
                .authSub("auth0|test").status(MemberStatus.WITHDRAWN).build();
        given(memberRepository.findById(2L)).willReturn(Optional.of(withdrawn));

        assertThatThrownBy(() -> service.sendRequest(1L, 2L))
                .isInstanceOf(FriendshipException.class)
                .satisfies(ex -> assertThat(((FriendshipException) ex).getErrorCode())
                        .isEqualTo(FriendshipErrorCode.WITHDRAWN_MEMBER));
    }

    @Test
    void getFriends_WITHDRAWN회원제외() {
        Friendship friendship = new Friendship(10L, 1L, 2L,
                FriendshipStatus.ACCEPTED, LocalDateTime.now(), LocalDateTime.now());
        given(friendshipRepository.findAllByMemberIdAndStatus(1L, FriendshipStatus.ACCEPTED))
                .willReturn(List.of(friendship));
        Member withdrawn = Member.builder().id(2L)
                .email("w@test.com").nickname("withdrawn")
                .authSub("auth0|w").status(MemberStatus.WITHDRAWN).build();
        given(memberRepository.findAllByIds(List.of(2L)))
                .willReturn(List.of(withdrawn));

        List<FriendInfo> result = service.getFriends(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void getReceivedRequests_WITHDRAWN요청자제외() {
        Friendship friendship = new Friendship(10L, 3L, 1L,
                FriendshipStatus.PENDING, LocalDateTime.now(), null);
        given(friendshipRepository.findAllByReceiverIdAndStatus(1L, FriendshipStatus.PENDING))
                .willReturn(List.of(friendship));
        Member withdrawn = Member.builder().id(3L)
                .email("w@test.com").nickname("withdrawn")
                .authSub("auth0|w").status(MemberStatus.WITHDRAWN).build();
        given(memberRepository.findAllByIds(List.of(3L)))
                .willReturn(List.of(withdrawn));

        List<FriendRequestInfo> result = service.getReceivedRequests(1L);

        assertThat(result).isEmpty();
    }

    private Member dummyMember(Long id) {
        return Member.builder().id(id)
                .email("test@test.com").nickname("test")
                .authSub("auth0|test").build();
    }
}
