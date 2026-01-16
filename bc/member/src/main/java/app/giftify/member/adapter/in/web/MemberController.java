package app.giftify.member.adapter.in.web;

import app.giftify.member.adapter.in.web.dto.MemberUpdateRequest;
import app.giftify.member.adapter.in.web.dto.SignupRequest;
import app.giftify.member.application.port.in.GetMemberUseCase;
import app.giftify.member.application.port.in.RegisterMemberUseCase;
import app.giftify.member.application.port.in.UpdateMemberUseCase;
import app.giftify.member.application.port.in.WithdrawMemberUseCase;
import app.giftify.member.core.domain.exception.MemberNotFoundException;
import app.giftify.member.core.domain.member.Member;
import app.giftify.member.core.domain.member.MemberStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

// 사용자의 가입 상태 확인 및 회원가입 API
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final GetMemberUseCase getMemberUseCase;
    private final RegisterMemberUseCase registerMemberUseCase;
    private final UpdateMemberUseCase updateMemberUseCase;
    private final WithdrawMemberUseCase withdrawMemberUseCase;

    // Auth0 인증 정보(JWT)를 기반으로 가입여부 확인
    @GetMapping("/check-registration")
    public ResponseEntity<?> checkRegistration(
            @AuthenticationPrincipal Jwt jwt
    ) {
        if (jwt == null) {
            return ResponseEntity.status(401).body(Map.of("message", "인증 정보(JWT)가 누락되었습니다."));
        }

        String authSub = jwt.getSubject();

        return getMemberUseCase.getMemberByAuthSub(authSub)
                .map(member -> ResponseEntity.ok().body((Object) member))
                .orElseGet(() -> ResponseEntity.ok().body(Map.of("status", "NOT_REGISTERED")));
    }

    // 신규 회원 가입 (추가 정보 입력)
    @PostMapping("/signup")
    public ResponseEntity<Member> signup(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid SignupRequest request
    ) {
        if (jwt == null) {
            return ResponseEntity.status(401).build();
        }

        RegisterMemberUseCase.RegisterCommand command = new RegisterMemberUseCase.RegisterCommand(
                jwt.getClaimAsString("email"),
                jwt.getSubject(),
                request.nickname(),
                request.birthday(),
                request.address(),
                request.phoneNum(),
                request.name()
        );

        Member member = registerMemberUseCase.registerMember(command);
        return ResponseEntity.ok(member);
    }

    // 내 정보 조회
    @GetMapping("/getMyInfo")
    public ResponseEntity<?> getMyInfo(
            @AuthenticationPrincipal Jwt jwt
    ) {
        String authSub = jwt.getSubject();

        return getMemberUseCase.getMemberByAuthSub(authSub)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new MemberNotFoundException(authSub));
    }

    // 회원 정보 수정
    @PatchMapping("/updateMyInfo")
    public ResponseEntity<Member> updateMyInfo(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid MemberUpdateRequest request
    ) {
        String authSub = jwt.getSubject();

        Optional<Member> member = getMemberUseCase.getMemberByAuthSub(authSub);
        if (member.isPresent() && isNotActive(member.get())) {
            return ResponseEntity.status(401).build();
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
    // TODO: 탈퇴(WITHDRAW로 상태 변경) -> 언제까지 가지고 있을지 정책 생각하기(ref.인스타그램)
    @DeleteMapping("/withdraw")
    public ResponseEntity<Void> withdraw(
            @AuthenticationPrincipal Jwt jwt
    ) {
        String authSub = jwt.getSubject();

        withdrawMemberUseCase.withdrawMember(authSub);

        return ResponseEntity.noContent().build();
    }

    private boolean isNotActive(Member member) {
        return member.getStatus() == MemberStatus.ACTIVE;
    }
}
