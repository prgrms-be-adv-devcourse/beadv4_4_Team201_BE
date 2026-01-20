package app.giftify.member.adapter.in.web.controller;

import app.giftify.member.adapter.in.web.requestDto.MemberUpdateRequest;
import app.giftify.member.adapter.in.web.requestDto.SignupRequest;
import app.giftify.member.application.port.in.GetMemberUseCase;
import app.giftify.member.application.port.in.RegisterMemberUseCase;
import app.giftify.member.application.port.in.UpdateMemberUseCase;
import app.giftify.member.application.port.in.WithdrawMemberUseCase;
import app.giftify.member.core.domain.Member;
import app.giftify.member.core.domain.MemberStatus;
import app.giftify.member.core.domain.exception.InvalidNicknameException;
import app.giftify.member.core.domain.exception.MemberNotFoundException;
import app.giftify.security.common.context.AuthenticatedMember;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

// 사용자의 가입 상태 확인 및 회원가입 API
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Validated
public class MemberController {

    private final GetMemberUseCase getMemberUseCase;
    private final RegisterMemberUseCase registerMemberUseCase;
    private final UpdateMemberUseCase updateMemberUseCase;
    private final WithdrawMemberUseCase withdrawMemberUseCase;

    // Auth0 인증 정보(JWT)를 기반으로 가입여부 확인
    @GetMapping("/check-registration")
    public ResponseEntity<?> checkRegistration(
            @AuthenticatedMember String authSub
    ) {
        if (authSub == null) {
            return ResponseEntity.status(401).body(Map.of("message", "인증 정보(JWT)가 누락되었습니다."));
        }

        return getMemberUseCase.getMemberByAuthSub(authSub)
                .map(member -> ResponseEntity.ok().body((Object) member))
                .orElseGet(() -> ResponseEntity.ok().body(Map.of("status", "NOT_REGISTERED")));
    }

    // 신규 회원 가입 (추가 정보 입력)
    @PostMapping("/signup")
    public ResponseEntity<Member> signup(
            @AuthenticatedMember String authSub,
            @RequestBody @Valid SignupRequest request
    ) {
        if (authSub == null) {
            return ResponseEntity.status(401).build();
        }

        Member member = registerMemberUseCase.signup(authSub, request);

        return ResponseEntity.ok(member);
    }

    // 내 정보 조회
    @GetMapping("/getMyInfo")
    public ResponseEntity<?> getMyInfo(
            @AuthenticatedMember String authSub
    ) {
        if (authSub == null) {
            return ResponseEntity.status(401).build();
        }

        return getMemberUseCase.getMemberByAuthSub(authSub)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new MemberNotFoundException(authSub));
    }

    // 회원 정보 수정
    @PatchMapping("/updateMyInfo")
    public ResponseEntity<Member> updateMyInfo(
            @AuthenticatedMember String authSub,
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

    // 회원 탈퇴
    @DeleteMapping("/withdraw")
    public ResponseEntity<Void> withdraw(
            @AuthenticatedMember String authSub
    ) {
        if (authSub == null) {
            return ResponseEntity.status(401).build();
        }

        withdrawMemberUseCase.withdrawMember(authSub);

        return ResponseEntity.noContent().build();
    }

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
