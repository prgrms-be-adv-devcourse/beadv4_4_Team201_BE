package app.giftify.friendship.adapter.in.web;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import app.giftify.friendship.adapter.in.web.dto.*;
import app.giftify.friendship.application.port.in.*;
import app.giftify.friendship.domain.Friendship;
import app.giftify.member.application.port.out.MemberRepositoryPort;
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
        return ResponseEntity.ok(RsData.success(null));
    }

    @DeleteMapping("/api/v2/friends/{friendshipId}")
    public ResponseEntity<RsData<Void>> remove(
            @CurrentMemberId Long memberId,
            @PathVariable Long friendshipId) {
        removeFriendUseCase.remove(friendshipId, memberId);
        return ResponseEntity.ok(RsData.success(null));
    }

    @GetMapping("/api/v2/members/{memberId}/friends")
    public ResponseEntity<RsData<List<FriendResponse>>> getFriends(
            @CurrentMemberId Long currentMemberId,
            @PathVariable Long memberId) {
        List<Friendship> friendships = getFriendListUseCase.getFriends(memberId);
        List<Long> friendIds = friendships.stream()
                .map(f -> f.getFriendId(memberId))
                .toList();
        Map<Long, Member> memberMap = memberRepository.findAllByIds(friendIds).stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));
        List<FriendResponse> friends = friendIds.stream()
                .map(memberMap::get)
                .map(FriendResponse::from)
                .toList();
        return ResponseEntity.ok(RsData.success(friends));
    }

    @GetMapping("/api/v2/friends/requests")
    public ResponseEntity<RsData<List<FriendRequestResponse>>> getReceivedRequests(
            @CurrentMemberId Long memberId) {
        List<Friendship> requests = getFriendRequestsUseCase.getReceivedRequests(memberId);
        List<Long> requesterIds = requests.stream()
                .map(Friendship::getRequesterId)
                .toList();
        Map<Long, Member> memberMap = memberRepository.findAllByIds(requesterIds).stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));
        List<FriendRequestResponse> responses = requests.stream()
                .map(f -> FriendRequestResponse.of(f, memberMap.get(f.getRequesterId())))
                .toList();
        return ResponseEntity.ok(RsData.success(responses));
    }
}
