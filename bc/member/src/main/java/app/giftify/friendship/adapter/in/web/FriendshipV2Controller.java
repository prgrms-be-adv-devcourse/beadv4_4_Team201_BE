package app.giftify.friendship.adapter.in.web;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import app.giftify.friendship.adapter.in.web.dto.*;
import app.giftify.friendship.application.port.in.*;
import app.giftify.friendship.domain.Friendship;
import app.giftify.member.application.port.out.MemberRepositoryPort;
import app.giftify.member.domain.exception.MemberNotFoundException;
import app.giftify.member.domain.member.Member;
import app.giftify.security.common.CurrentMemberId;
import app.giftify.shared.api.response.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Validated
public class FriendshipV2Controller {

    private final SendFriendRequestUseCase sendFriendRequestUseCase;
    private final AcceptFriendRequestUseCase acceptFriendRequestUseCase;
    private final RejectFriendRequestUseCase rejectFriendRequestUseCase;
    private final RemoveFriendUseCase removeFriendUseCase;
    private final GetFriendListUseCase getFriendListUseCase;
    private final GetFriendRequestsUseCase getFriendRequestsUseCase;
    private final MemberRepositoryPort memberRepository;

    @PostMapping("/api/v2/friends/request")
    public ResponseEntity<RsData<FriendshipResponse>> sendRequest(
            @CurrentMemberId Long memberId,
            @RequestBody @Valid SendFriendRequestDto request) {
        Friendship friendship = sendFriendRequestUseCase.sendRequest(memberId, request.receiverId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RsData.success(FriendshipResponse.from(friendship)));
    }

    @PostMapping("/api/v2/friends/{friendshipId}/accept")
    public ResponseEntity<RsData<FriendshipResponse>> accept(
            @CurrentMemberId Long memberId,
            @PathVariable Long friendshipId) {
        Friendship friendship = acceptFriendRequestUseCase.accept(friendshipId, memberId);
        return ResponseEntity.ok(RsData.success(FriendshipResponse.from(friendship)));
    }

    @PostMapping("/api/v2/friends/{friendshipId}/reject")
    public ResponseEntity<RsData<Void>> reject(
            @CurrentMemberId Long memberId,
            @PathVariable Long friendshipId) {
        rejectFriendRequestUseCase.reject(friendshipId, memberId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/v2/friends/{friendshipId}")
    public ResponseEntity<RsData<Void>> remove(
            @CurrentMemberId Long memberId,
            @PathVariable Long friendshipId) {
        removeFriendUseCase.remove(friendshipId, memberId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v2/members/{memberId}/friends")
    public ResponseEntity<RsData<List<FriendResponse>>> getFriends(
            @PathVariable Long memberId) {
        List<Friendship> friendships = getFriendListUseCase.getFriends(memberId);
        List<FriendResponse> friends = friendships.stream()
                .map(f -> {
                    Long friendId = f.getFriendId(memberId);
                    Member friend = memberRepository.findById(friendId)
                            .orElseThrow(() -> new MemberNotFoundException(friendId));
                    return FriendResponse.from(friend);
                })
                .toList();
        return ResponseEntity.ok(RsData.success(friends));
    }

    @GetMapping("/api/v2/friends/requests")
    public ResponseEntity<RsData<List<FriendRequestResponse>>> getReceivedRequests(
            @CurrentMemberId Long memberId) {
        List<Friendship> requests = getFriendRequestsUseCase.getReceivedRequests(memberId);
        List<FriendRequestResponse> responses = requests.stream()
                .map(f -> {
                    Member requester = memberRepository.findById(f.getRequesterId())
                            .orElseThrow(() -> new MemberNotFoundException(f.getRequesterId()));
                    return FriendRequestResponse.of(f, requester);
                })
                .toList();
        return ResponseEntity.ok(RsData.success(responses));
    }
}
