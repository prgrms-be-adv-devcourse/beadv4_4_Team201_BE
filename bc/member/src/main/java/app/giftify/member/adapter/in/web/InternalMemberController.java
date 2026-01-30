package app.giftify.member.adapter.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.giftify.member.application.port.in.GetMemberUseCase;
import app.giftify.shared.domain.vo.MemberInfo;
import lombok.RequiredArgsConstructor;

/**
 * 내부 서비스 간 통신을 위한 Member API.
 * 외부 공개 API와 분리하여 내부 계약 관리.
 */
@RestController
@RequestMapping("/api/internal/members")
@RequiredArgsConstructor
public class InternalMemberController {

	private final GetMemberUseCase getMemberUseCase;

	@GetMapping("/by-auth-sub/{authSub}")
	public ResponseEntity<MemberInfo> getByAuthSub(@PathVariable("authSub") String authSub) {
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
}
