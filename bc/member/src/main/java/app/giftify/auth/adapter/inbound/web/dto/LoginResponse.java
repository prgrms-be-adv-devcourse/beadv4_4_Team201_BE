package app.giftify.auth.adapter.inbound.web.dto;

import app.giftify.shared.domain.vo.MemberInfo;

/**
 * POST /api/auth/login 응답 DTO.
 *
 * @param isNewUser 신규 사용자 여부 (true면 온보딩 필요)
 * @param authSub   Auth0 고유 식별자
 * @param email     사용자 이메일
 * @param name      사용자 이름
 * @param member    회원 정보 (신규 사용자면 null)
 */
public record LoginResponse(
	boolean isNewUser,
	String authSub,
	String email,
	String name,
	MemberInfo member
) {
	/**
	 * 신규 사용자용 응답 생성
	 */
	public static LoginResponse newUser(String authSub, String email, String name) {
		return new LoginResponse(true, authSub, email, name, null);
	}

	/**
	 * 기존 회원용 응답 생성
	 */
	public static LoginResponse existingMember(MemberInfo member) {
		return new LoginResponse(
			false,
			member.authSub(),
			member.email(),
			member.nickname(),  // MemberInfo에 name이 없어 nickname 사용
			member
		);
	}
}
