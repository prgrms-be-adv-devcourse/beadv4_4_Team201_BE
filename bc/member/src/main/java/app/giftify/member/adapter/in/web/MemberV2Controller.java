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

import app.giftify.member.adapter.in.web.dto.MemberResponse;
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

/**
 * Member API v2.
 */
@Slf4j
@RestController
@RequestMapping("/api/v2/members")
@RequiredArgsConstructor
@Validated
public class MemberV2Controller implements MemberV2Api {

    private final GetMemberUseCase getMemberUseCase;
    private final RegisterMemberUseCase registerMemberUseCase;
    private final UpdateMemberUseCase updateMemberUseCase;
    private final WithdrawMemberUseCase withdrawMemberUseCase;

    /**
     * 내 정보 조회.
     */
    @Override
    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getMe(
            @CurrentAuthSub String authSub
    ) {
        if (authSub == null) {
            return ResponseEntity.status(401).build();
        }

        return getMemberUseCase.getMemberByAuthSub(authSub)
                .map(member -> ResponseEntity.ok(MemberResponse.from(member)))
                .orElseThrow(() -> new MemberNotFoundException(authSub));
    }

    /**
     * 회원 정보 수정.
     */
    @Override
    @PatchMapping("/me")
    public ResponseEntity<MemberResponse> updateMe(
            @CurrentAuthSub String authSub,
            @RequestBody @Valid MemberUpdateRequest request
    ) {
        if (authSub == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<Member> memberOpt = getMemberUseCase.getMemberByAuthSub(authSub);
        if (memberOpt.isPresent() && memberOpt.get().getStatus() != MemberStatus.ACTIVE) {
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
        return ResponseEntity.ok(MemberResponse.from(updatedMember));
    }

    /**
     * 회원가입 (추가 정보 입력).
     */
    @Override
    @PostMapping("/signup")
    public ResponseEntity<MemberResponse> signup(
            @CurrentAuthSub String authSub,
            @RequestBody @Valid SignupRequest request
    ) {
        if (authSub == null) {
            return ResponseEntity.status(401).build();
        }

        // 이미 가입된 회원인지 확인
        if (getMemberUseCase.getMemberByAuthSub(authSub).isPresent()) {
            return ResponseEntity.status(409).build();
        }

        Member member = registerMemberUseCase.signup(authSub, request);
        return ResponseEntity.ok(MemberResponse.from(member));
    }

    /**
     * 회원 탈퇴.
     */
    @Override
    @DeleteMapping("/me")
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
     * 가입 여부 확인.
     */
    @Override
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
                    return ResponseEntity.ok().body((Object) MemberResponse.from(member));
                })
                .orElseGet(() -> {
                    log.debug("[Controller] Member NOT found for authSub: {}", authSub);
                    return ResponseEntity.ok().body(Map.of("status", "NOT_REGISTERED"));
                });
    }

    /**
     * 닉네임 중복 확인.
     */
    @Override
    @GetMapping("/check/nickname")
    public ResponseEntity<?> checkNickname(
            @RequestParam(name = "nickname") @NotBlank String nickname
    ) {
        if (nickname.isBlank()) {
            throw new InvalidNicknameException();
        }

        boolean duplicated = getMemberUseCase.isNicknameDuplicated(nickname);
        return ResponseEntity.ok(Map.of("status", duplicated ? "DUPLICATED" : "AVAILABLE"));
    }
}
