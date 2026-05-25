package app.giftify.security.test;

import app.giftify.security.common.MemberAuthenticationToken;
import app.giftify.security.common.MemberPrincipal;
import app.giftify.support.common.security.MemberRole;
import app.giftify.support.common.security.MemberInfo;

/**
 * 테스트용 Member 객체 생성 유틸리티.
 *
 * <p>테스트에서 일관된 테스트 픽스처를 생성하기 위해 사용합니다.</p>
 */
public class TestMemberFactory {

	public static final Long DEFAULT_MEMBER_ID = 1L;
	public static final String DEFAULT_AUTH_SUB = "auth0|test123";
	public static final String DEFAULT_EMAIL = "test@example.com";
	public static final String DEFAULT_NICKNAME = "tester";

	/**
	 * MemberInfo를 생성합니다.
	 */
	public static MemberInfo createMemberInfo(Long memberId, MemberRole role) {
		return MemberInfo.of(
			memberId,
			DEFAULT_AUTH_SUB,
			role,
			DEFAULT_EMAIL,
			DEFAULT_NICKNAME
		);
	}

	/**
	 * MemberInfo를 생성합니다 (authSub 지정).
	 */
	public static MemberInfo createMemberInfo(Long memberId, String authSub, MemberRole role) {
		return MemberInfo.of(
			memberId,
			authSub,
			role,
			DEFAULT_EMAIL,
			DEFAULT_NICKNAME
		);
	}

	/**
	 * 전체 필드를 지정하여 MemberInfo를 생성합니다.
	 */
	public static MemberInfo createMemberInfo(Long memberId, String authSub, MemberRole role,
		String email, String nickname) {
		return MemberInfo.of(memberId, authSub, role, email, nickname);
	}

	/**
	 * MemberPrincipal을 생성합니다.
	 */
	public static MemberPrincipal createMemberPrincipal(Long memberId, MemberRole role) {
		return MemberPrincipal.from(createMemberInfo(memberId, role));
	}

	/**
	 * MemberPrincipal을 생성합니다 (authSub 지정).
	 */
	public static MemberPrincipal createMemberPrincipal(Long memberId, String authSub, MemberRole role) {
		return MemberPrincipal.from(createMemberInfo(memberId, authSub, role));
	}

	/**
	 * MemberAuthenticationToken을 생성합니다.
	 */
	public static MemberAuthenticationToken createAuthToken(Long memberId, MemberRole role) {
		return new MemberAuthenticationToken(createMemberPrincipal(memberId, role));
	}

	/**
	 * MemberAuthenticationToken을 생성합니다 (authSub 지정).
	 */
	public static MemberAuthenticationToken createAuthToken(Long memberId, String authSub, MemberRole role) {
		return new MemberAuthenticationToken(createMemberPrincipal(memberId, authSub, role));
	}

	/**
	 * 기본 BUYER 역할의 MemberPrincipal을 생성합니다.
	 */
	public static MemberPrincipal createBuyer() {
		return createMemberPrincipal(DEFAULT_MEMBER_ID, MemberRole.BUYER);
	}

	/**
	 * 기본 SELLER 역할의 MemberPrincipal을 생성합니다.
	 */
	public static MemberPrincipal createSeller() {
		return createMemberPrincipal(DEFAULT_MEMBER_ID, MemberRole.SELLER);
	}

	/**
	 * 기본 ADMIN 역할의 MemberPrincipal을 생성합니다.
	 */
	public static MemberPrincipal createAdmin() {
		return createMemberPrincipal(DEFAULT_MEMBER_ID, MemberRole.ADMIN);
	}
}
