package app.giftify.member.adapter.in.web;

import app.giftify.member.adapter.in.web.dto.SignupRequest;
import app.giftify.member.application.port.in.GetMemberUseCase;
import app.giftify.member.application.port.in.RegisterMemberUseCase;
import app.giftify.member.core.domain.member.Member;
import app.giftify.shared.api.response.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * [1단계: 가입 여부 확인 및 회원가입 - 컨트롤러]
 * 모든 응답은 공통 응답 규격(CommonResponse)을 따릅니다.
 */
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final GetMemberUseCase getMemberUseCase;
    private final RegisterMemberUseCase registerMemberUseCase;

    /**
     * Auth0 인증 정보(JWT)를 기반으로 가입 여부 확인
     */
    @GetMapping("/check-registration")
    public ResponseEntity<CommonResponse<?>> checkRegistration(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(401)
                    .body(CommonResponse.fail("인증 정보가 누락되었습니다.", "UNAUTHORIZED"));
        }

        String authSub = jwt.getSubject();
        Optional<Member> member = getMemberUseCase.getMemberByAuthSub(authSub);

        if (member.isPresent()) {
            return ResponseEntity.ok(CommonResponse.success(member.get()));
        } else {
            return ResponseEntity.ok(CommonResponse.success("NOT_REGISTERED", "미가입 상태입니다."));
        }
    }

    /**
     * 신규 회원 가입
     */
    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<?>> signup(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid SignupRequest request
    ) {
        if (jwt == null) {
            return ResponseEntity.status(401)
                    .body(CommonResponse.fail("인증 정보가 누락되었습니다.", "UNAUTHORIZED"));
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
        return ResponseEntity.ok(CommonResponse.success(member, "회원가입이 완료되었습니다."));
    }
}
