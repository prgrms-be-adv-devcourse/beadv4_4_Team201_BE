package app.giftify.member.adapter.in.web;

import app.giftify.member.adapter.in.web.dto.SignupRequest;
import app.giftify.member.application.port.in.GetMemberUseCase;
import app.giftify.member.application.port.in.RegisterMemberUseCase;
import app.giftify.member.core.domain.member.Member;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// 사용자의 가입 상태 확인 및 회원가입 API
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final GetMemberUseCase getMemberUseCase;
    private final RegisterMemberUseCase registerMemberUseCase;

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
}
