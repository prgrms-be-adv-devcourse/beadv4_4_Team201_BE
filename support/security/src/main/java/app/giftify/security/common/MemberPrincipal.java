package app.giftify.security.common;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import app.giftify.support.common.security.MemberRole;
import app.giftify.support.common.security.MemberInfo;

/**
 * Spring Security의 UserDetails를 구현한 인증 Principal.
 *
 * <p>{@link MemberInfo}를 래핑하여 Spring Security와 통합.
 * {@code @AuthenticationPrincipal}로 컨트롤러에 주입 가능.</p>
 *
 * <p>사용 예시:</p>
 * <pre>{@code
 * @GetMapping("/profile")
 * public ResponseEntity<?> profile(@AuthenticationPrincipal MemberPrincipal principal) {
 *     Long memberId = principal.memberId();
 *     // ...
 * }
 * }</pre>
 */
public record MemberPrincipal(
	MemberInfo memberInfo
) implements UserDetails {

	public static MemberPrincipal from(MemberInfo info) {
		return new MemberPrincipal(info);
	}

	/**
	 * 미가입 사용자용 MemberPrincipal 생성.
	 * Auth0 인증은 완료했지만 서비스 회원가입은 하지 않은 상태.
	 *
	 * @param authSub Auth0 고유 식별자 (JWT subject)
	 * @return memberId가 null인 MemberPrincipal
	 */
	public static MemberPrincipal forUnregistered(String authSub) {
		return new MemberPrincipal(MemberInfo.forUnregistered(authSub));
	}

	/**
	 * 가입된 회원인지 확인.
	 *
	 * @return memberId가 존재하면 true
	 */
	public boolean isRegistered() {
		return memberInfo.isRegistered();
	}

	// ===== 편의 메서드 - MemberInfo 필드 직접 접근 =====

	public Long memberId() {
		return memberInfo.memberId();
	}

	public String authSub() {
		return memberInfo.authSub();
	}

	public MemberRole role() {
		return memberInfo.role();
	}

	public String email() {
		return memberInfo.email();
	}

	public String nickname() {
		return memberInfo.nickname();
	}

	// ===== UserDetails 구현 =====

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		MemberRole role = memberInfo.role();
		if (role == null) {
			return List.of();
		}
		return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}

	@Override
	public String getPassword() {
		return null;  // Auth0 JWT 기반, 비밀번호 불필요
	}

	@Override
	public String getUsername() {
		return memberInfo.authSub();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}
