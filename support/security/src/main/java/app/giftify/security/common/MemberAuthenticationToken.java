package app.giftify.security.common;

import java.util.Objects;

import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * MemberPrincipal 기반의 인증 토큰.
 *
 * <p>
 * Auth0 JWT 검증 이후 {@code MemberPrincipalFilter}에서 생성되어
 * {@code SecurityContext}에 저장된다.
 * </p>
 *
 * <p>
 * {@code @PreAuthorize}와 연동되어 역할(Role) 기반 인가에 사용된다.
 * </p>
 *
 * <h2>구현 구조</h2>
 * <ul>
 *   <li>
 *     {@link org.springframework.security.authentication.AbstractAuthenticationToken} 상속
 *   </li>
 *   <li>
 *     {@code getAuthorities()} :
 *     {@code MemberPrincipal}로부터 Role 기반 권한 제공
 *   </li>
 *   <li>
 *     {@code setAuthenticated(true)} :
 *     JWT 검증이 완료된 상태이므로 즉시 인증 완료로 설정
 *   </li>
 *   <li>
 *     {@code @PreAuthorize("hasRole('SELLER')")} 등에서
 *     {@code authorities} 자동 검사
 *   </li>
 * </ul>
 */
public class MemberAuthenticationToken extends AbstractAuthenticationToken {

	private final MemberPrincipal principal;

	public MemberAuthenticationToken(MemberPrincipal principal) {
		super(principal.getAuthorities());
		this.principal = principal;
		setAuthenticated(true);  // 이미 JWT로 검증된 상태
	}

	@Override
	public Object getCredentials() {
		return null;  // JWT 기반 인증, credentials 불필요
	}

	@Override
	public MemberPrincipal getPrincipal() {
		return principal;
	}

	@Override
	public final boolean equals(Object object) {
		if (!(object instanceof MemberAuthenticationToken that))
			return false;
		if (!super.equals(object))
			return false;

		return Objects.equals(principal, that.principal);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + Objects.hashCode(principal);
		return result;
	}
}
