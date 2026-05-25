package app.giftify.support.common.security;

/**
 * 인증된 사용자의 핵심 정보를 담는 레코드. 식별자가 있어 엄밀히 VO가 아니긴 하지만 활용처를 생각해보면 VO 느낌이고 비슷해서 일단 ./vo 에 배치
 *
 * @param memberId 내부 회원 ID (DB PK)
 * @param authSub  Auth0 고유 식별자 (JWT subject)
 * @param role     회원 역할 (BUYER, SELLER, ADMIN)
 * @param email    회원 이메일
 * @param nickname 회원 닉네임
 */
public record MemberInfo(
	Long memberId,
	String authSub,
	MemberRole role,
	String email,
	String nickname
) {
	public static MemberInfo of(Long memberId,
		String authSub,
		MemberRole role,
		String email,
		String nickname
	) {
		return new MemberInfo(
			memberId, authSub, role, email, nickname
		);
	}

	/**
	 * 미가입 사용자용 MemberInfo 생성.
	 * Auth0 인증은 완료했지만 서비스 회원가입은 하지 않은 상태.
	 *
	 * @param authSub Auth0 고유 식별자 (JWT subject)
	 * @return memberId가 null인 MemberInfo
	 */
	public static MemberInfo forUnregistered(String authSub) {
		return new MemberInfo(null, authSub, null, null, null);
	}

	/**
	 * 가입된 회원인지 확인.
	 *
	 * @return memberId가 존재하면 true
	 */
	public boolean isRegistered() {
		return memberId != null;
	}
}
