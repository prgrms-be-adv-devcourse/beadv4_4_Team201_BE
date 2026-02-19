package app.giftify.friendship.adapter.in.web;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import app.giftify.friendship.adapter.in.web.dto.*;
import app.giftify.friendship.application.port.in.*;
import app.giftify.friendship.domain.Friendship;
import app.giftify.security.common.CurrentMemberId;
import app.giftify.shared.api.response.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Validated
public class FriendshipV2Controller implements FriendshipV2ApiSpec {

    private final SendFriendRequestUseCase sendFriendRequestUseCase;
    private final AcceptFriendRequestUseCase acceptFriendRequestUseCase;
    private final RejectFriendRequestUseCase rejectFriendRequestUseCase;
    private final RemoveFriendUseCase removeFriendUseCase;
    private final GetFriendListUseCase getFriendListUseCase;
    private final GetFriendRequestsUseCase getFriendRequestsUseCase;

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

    @GetMapping("/api/v2/friends")
    public ResponseEntity<RsData<List<FriendResponse>>> getFriends(
            @CurrentMemberId Long memberId) {
        List<FriendResponse> responses = getFriendListUseCase.getFriends(memberId).stream()
                .map(FriendResponse::from)
                .toList();
        return ResponseEntity.ok(RsData.success(responses));
    }

    @GetMapping("/api/v2/friends/requests")
    public ResponseEntity<RsData<List<FriendRequestResponse>>> getReceivedRequests(
            @CurrentMemberId Long memberId) {
        List<FriendRequestResponse> responses = getFriendRequestsUseCase.getReceivedRequests(memberId).stream()
                .map(FriendRequestResponse::from)
                .toList();
        return ResponseEntity.ok(RsData.success(responses));
    }
}
