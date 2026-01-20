package app.giftify.shared.domain.vo;

import app.giftify.shared.domain.type.MemberRole;

/**
 * 인증된 사용자의 핵심 정보를 담는 레코드. 식별자가 있어 엄밀히 VO가 아니긴 하지만 활용처를 생각해보면 VO 느낌이고 비슷해서 일단 ./vo 에 배치
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
}
