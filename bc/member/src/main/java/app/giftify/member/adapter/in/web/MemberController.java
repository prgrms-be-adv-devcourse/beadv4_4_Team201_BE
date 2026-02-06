package app.giftify.member.adapter.in.web;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.giftify.member.adapter.in.web.dto.MemberUpdateRequest;
import app.giftify.member.adapter.in.web.dto.SignupRequest;
import app.giftify.member.application.port.in.GetMemberUseCase;
import app.giftify.member.application.port.in.RegisterMemberUseCase;
import app.giftify.member.application.port.in.UpdateMemberUseCase;
import app.giftify.member.application.port.in.WithdrawMemberUseCase;
import app.giftify.member.domain.exception.InvalidNicknameException;
import app.giftify.member.domain.exception.MemberNotFoundException;
import app.giftify.member.domain.member.Member;
import app.giftify.member.domain.member.MemberStatus;
import app.giftify.security.common.CurrentAuthSub;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 사용자의 가입 상태 확인 및 회원가입 API
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Validated
@Slf4j
public class MemberController {

    private final GetMemberUseCase getMemberUseCase;
    private final RegisterMemberUseCase registerMemberUseCase;
    private final UpdateMemberUseCase updateMemberUseCase;
    private final WithdrawMemberUseCase withdrawMemberUseCase;

    /**
     * @deprecated V2 API로 이동됨. {@link MemberV2Controller#checkRegistration} 사용.
     */
    @Deprecated(since = "v2", forRemoval = true)
    @GetMapping("/check-registration")
    public ResponseEntity<?> checkRegistration(
            @CurrentAuthSub String authSub
    ) {
        log.debug("[Controller] checkRegistration called with authSub: {}", authSub);
        if (authSub == null) {
            return ResponseEntity.status(401).body(Map.of("message", "인증 정보(JWT)가 누락되었습니다."));
        }

        return getMemberUseCase.getMemberByAuthSub(authSub)
                .map(member -> {
                    log.debug("[Controller] Member found for authSub: {}", authSub);
                    return ResponseEntity.ok().body((Object) member);
                })
                .orElseGet(() -> {
                    log.debug("[Controller] Member NOT found for authSub: {}", authSub);
                    return ResponseEntity.ok().body(Map.of("status", "NOT_REGISTERED"));
                });
    }

    /**
     * @deprecated V2 API로 이동됨. {@link MemberV2Controller#signup} 사용.
     */
    @Deprecated(since = "v2", forRemoval = true)
    @PostMapping("/signup")
    public ResponseEntity<Member> signup(
            @CurrentAuthSub String authSub,
            @RequestBody @Valid SignupRequest request
    ) {
        if (authSub == null) {
            return ResponseEntity.status(401).build();
        }

        Member member = registerMemberUseCase.signup(authSub, request);

        return ResponseEntity.ok(member);
    }

    /**
     * @deprecated V2 API로 이동됨. {@link MemberV2Controller#getMe} 사용.
     */
    @Deprecated(since = "v2", forRemoval = true)
    @GetMapping("/getMyInfo")
    public ResponseEntity<?> getMyInfo(
            @CurrentAuthSub String authSub
    ) {
        if (authSub == null) {
            return ResponseEntity.status(401).build();
        }

        return getMemberUseCase.getMemberByAuthSub(authSub)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new MemberNotFoundException(authSub));
    }

    /**
     * @deprecated V2 API로 이동됨. {@link MemberV2Controller#updateMe} 사용.
     */
    @Deprecated(since = "v2", forRemoval = true)
    @PatchMapping("/updateMyInfo")
    public ResponseEntity<Member> updateMyInfo(
            @CurrentAuthSub String authSub,
            @RequestBody @Valid MemberUpdateRequest request
    ) {
        if (authSub == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<Member> member = getMemberUseCase.getMemberByAuthSub(authSub);
        if (member.isPresent() && member.get().getStatus() != MemberStatus.ACTIVE) {
            return ResponseEntity.status(403).build();
        }

        UpdateMemberUseCase.UpdateCommand command = new UpdateMemberUseCase.UpdateCommand(
                authSub,
                request.password(),
                request.nickname(),
                request.address(),
                request.phoneNum(),
                request.name()
        );

        Member updatedMember = updateMemberUseCase.updateMember(command);

        return ResponseEntity.ok(updatedMember);
    }

    /**
     * @deprecated V2 API로 이동됨. {@link MemberV2Controller#withdraw} 사용.
     */
    @Deprecated(since = "v2", forRemoval = true)
    @DeleteMapping("/withdraw")
    public ResponseEntity<Void> withdraw(
            @CurrentAuthSub String authSub
    ) {
        if (authSub == null) {
            return ResponseEntity.status(401).build();
        }

        withdrawMemberUseCase.withdrawMember(authSub);

        return ResponseEntity.noContent().build();
    }

    /**
     * @deprecated V2 API로 이동됨. {@link MemberV2Controller#checkNickname} 사용.
     */
    @Deprecated(since = "v2", forRemoval = true)
    @GetMapping("/check/nickname")
    public ResponseEntity<?> checkNickname(
            @RequestParam(name = "nickname") @NotBlank String nickname
    ) {
        if (nickname.isBlank()) {
            throw new InvalidNicknameException();
        }

        boolean duplicated = getMemberUseCase.isNicknameDuplicated(nickname);

        return ResponseEntity.ok(
                Map.of("status", duplicated ? "DUPLICATED" : "AVAILABLE")
        );
    }
}
