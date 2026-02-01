package app.giftify.member.adapter.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.giftify.member.application.port.in.GetMemberUseCase;
import app.giftify.member.application.port.in.RegisterMemberUseCase;
import app.giftify.member.domain.member.Member;
import app.giftify.shared.domain.vo.MemberInfo;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/internal/members")
@RequiredArgsConstructor
public class InternalMemberController {

	private final GetMemberUseCase getMemberUseCase;
	private final RegisterMemberUseCase registerMemberUseCase;

	@GetMapping("/by-auth-sub/{authSub}")
	public ResponseEntity<MemberInfo> getByAuthSub(@PathVariable String authSub) {
		return getMemberUseCase.getMemberByAuthSub(authSub)
			.map(member -> MemberInfo.of(
				member.getId(),
				member.getAuthSub(),
				member.getRole(),
				member.getEmail(),
				member.getNickname()
			))
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

	/**
	 * 신규 회원을 생성합니다.
	 * Auth 모듈에서 신규 사용자 로그인 시 호출됩니다.
	 */
	@PostMapping
	public ResponseEntity<MemberInfo> createMember(@RequestBody CreateMemberRequest request) {
		var command = new RegisterMemberUseCase.RegisterCommand(
			request.email(),
			null,  // nickname: null이면 자동 생성
			null,
			null,
			null,
			request.name(),
			request.authSub()
		);

		Member member = registerMemberUseCase.registerMember(command);
		
		return ResponseEntity.status(201).body(MemberInfo.of(
			member.getId(),
			member.getAuthSub(),
			member.getRole(),
			member.getEmail(),
			member.getNickname()
		));
	}

	public record CreateMemberRequest(
		String authSub,
		String email,
		String name
	) {}
}
