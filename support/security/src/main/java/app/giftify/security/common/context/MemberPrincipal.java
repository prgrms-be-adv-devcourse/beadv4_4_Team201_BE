package app.giftify.security.common.context;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import app.giftify.shared.domain.type.MemberRole;

/**
 * 인증된 사용자의 주요 정보를 담는 레코드.
 * JWT 검증 후 MemberPrincipalFilter에서 생성되어 ThreadLocal에 저장됨.
 *
 * <p>Spring Security의 Principal 개념을 차용하여,
 * 내부 시스템에서 필요한 회원 정보를 함께 제공합니다.</p>
 *
 * @param memberId 내부 회원 ID (DB PK)
 * @param authSub  Auth0 고유 식별자 (JWT subject)
 * @param role     회원 역할 (BUYER, SELLER, ADMIN)
 * @param email    회원 이메일
 * @param nickname 회원 닉네임
 */
public record MemberPrincipal(
	Long memberId,
	String authSub,
	MemberRole role,
	String email,
	String nickname
) {
	/**
	 * {@code @PreAuthorize("hasRole('SELLER')")} 같은 표현식에서 사용됨.
	 *
	 * @return 회원의 권한 목록 (ROLE_ 접두사 포함)
	 */
	public Collection<GrantedAuthority> getAuthorities() {
		if (role == null) {
			return List.of();
		}
		return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}
}
